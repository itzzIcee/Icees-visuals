import {
  ChannelType,
  EmbedBuilder,
  MessageFlags,
  PermissionFlagsBits,
  type ButtonInteraction,
  type ModalSubmitInteraction,
  type TextChannel,
} from 'discord.js';
import type { Config } from '../config';
import { closeTicketRow, confirmCloseRow, ticketReasonModal } from './widgets';
import { memberHasRole } from '../lib/access';
import { UserFacingError } from '../lib/errors';
import { auditReason } from '../lib/format';
import {
  RULES_AGREE_ID,
  TICKET_CANCEL_CLOSE_ID,
  TICKET_CLOSE_DELAY_MS,
  TICKET_CLOSE_ID,
  TICKET_CONFIRM_CLOSE_ID,
  TICKET_OPEN_ID,
  TICKET_REASON_ID,
} from '../lib/ids';
import { findOpenTicket, openerFromTopic, ticketChannelName, ticketTopic } from '../lib/tickets';
import { assertTicketReason } from '../lib/validate';

const openingTickets = new Set<string>();

export async function handleButton(interaction: ButtonInteraction, config: Config): Promise<void> {
  switch (interaction.customId) {
    case RULES_AGREE_ID:
      await agreeToRules(interaction, config);
      return;
    case TICKET_OPEN_ID:
      if (!interaction.guild) throw new UserFacingError('Open a ticket from a server channel.');
      await interaction.showModal(ticketReasonModal());
      return;
    case TICKET_CLOSE_ID:
      await askToClose(interaction, config);
      return;
    case TICKET_CONFIRM_CLOSE_ID:
      await confirmClose(interaction, config);
      return;
    case TICKET_CANCEL_CLOSE_ID:
      await interaction.update({ content: 'Close cancelled. The ticket is still open.', components: [] });
      return;
    default:
      throw new UserFacingError('That button is no longer available.');
  }
}

export async function handleModal(interaction: ModalSubmitInteraction, config: Config): Promise<void> {
  if (interaction.customId !== TICKET_REASON_ID) {
    throw new UserFacingError('That form is no longer available.');
  }
  if (!interaction.guild) throw new UserFacingError('Open a ticket from a server channel.');
  const reason = assertTicketReason(interaction.fields.getTextInputValue('reason'));
  const key = `${interaction.guild.id}:${interaction.user.id}`;
  if (openingTickets.has(key)) {
    throw new UserFacingError('You already have a ticket opening. Wait a moment.');
  }
  openingTickets.add(key);
  try {
    await interaction.deferReply({ flags: MessageFlags.Ephemeral });
    await createTicket(interaction, config, reason);
  } finally {
    openingTickets.delete(key);
  }
}

async function agreeToRules(interaction: ButtonInteraction, config: Config): Promise<void> {
  if (!interaction.guild) throw new UserFacingError('Use this button in a server.');
  if (memberHasRole(interaction.member, config.rulesRoleId)) {
    throw new UserFacingError('You already have the member role.');
  }
  await interaction.deferReply({ flags: MessageFlags.Ephemeral });
  const role = await interaction.guild.roles.fetch(config.rulesRoleId);
  if (!role) throw new UserFacingError('The member role is missing. Ask an admin to check RULES_ROLE_ID.');
  if (role.managed) {
    throw new UserFacingError('That member role is managed by an integration, so I cannot assign it.');
  }
  const me = interaction.guild.members.me ?? (await interaction.guild.members.fetchMe());
  if (role.position >= me.roles.highest.position) {
    throw new UserFacingError("I cannot assign that role. Move the icee's Hub role above the member role.");
  }
  await interaction.guild.members.addRole({
    user: interaction.user.id,
    role,
    reason: auditReason(interaction.user.tag, 'Rules accepted'),
  });
  await interaction.editReply('You agreed to the rules and now have the member role.');
}

async function askToClose(interaction: ButtonInteraction, config: Config): Promise<void> {
  const channel = await requireTicketChannel(interaction);
  assertCanClose(interaction, channel, config);
  await interaction.reply({
    content: 'Delete this ticket channel? This cannot be undone.',
    components: [confirmCloseRow()],
  });
}

async function confirmClose(interaction: ButtonInteraction, config: Config): Promise<void> {
  const channel = await requireTicketChannel(interaction);
  assertCanClose(interaction, channel, config);
  if (!channel.deletable) {
    throw new UserFacingError('I cannot delete this channel. Grant me Manage Channels.');
  }
  await interaction.update({ content: 'Deleting this ticket in 5 seconds.', components: [] });
  const reason = auditReason(interaction.user.tag, 'Ticket closed');
  setTimeout(() => {
    void channel.delete(reason).catch((error: unknown) => {
      const message = error instanceof Error ? error.message : 'unknown error';
      console.error(`Failed to delete ticket channel ${channel.id}: ${message}`);
    });
  }, TICKET_CLOSE_DELAY_MS);
}

async function createTicket(interaction: ModalSubmitInteraction, config: Config, reason: string): Promise<void> {
  const guild = interaction.guild;
  if (!guild) throw new UserFacingError('Open a ticket from a server channel.');
  const botId = interaction.client.user?.id;
  if (!botId) throw new UserFacingError('I am still starting. Try again in a moment.');
  const staffRole = await guild.roles.fetch(config.staffRoleId);
  if (!staffRole) throw new UserFacingError('The staff role is missing. Ask an admin to check STAFF_ROLE_ID.');
  if (staffRole.id === guild.roles.everyone.id) {
    throw new UserFacingError('STAFF_ROLE_ID cannot be @everyone. Choose a staff role.');
  }
  await guild.channels.fetch();
  const existing = findOpenTicket(guild, interaction.user.id);
  if (existing) {
    await interaction.editReply(`You already have an open ticket: ${existing}. Close it before opening another.`);
    return;
  }

  const memberView = [
    PermissionFlagsBits.ViewChannel,
    PermissionFlagsBits.SendMessages,
    PermissionFlagsBits.ReadMessageHistory,
    PermissionFlagsBits.AttachFiles,
    PermissionFlagsBits.EmbedLinks,
  ];
  let created: TextChannel | null = null;
  try {
    const channel = await guild.channels.create({
      name: ticketChannelName(interaction.user.username, interaction.user.id),
      type: ChannelType.GuildText,
      topic: ticketTopic(interaction.user.id, reason),
      permissionOverwrites: [
        { id: guild.roles.everyone.id, deny: [PermissionFlagsBits.ViewChannel] },
        { id: interaction.user.id, allow: memberView },
        { id: staffRole.id, allow: [...memberView, PermissionFlagsBits.ManageMessages] },
        {
          id: botId,
          allow: [...memberView, PermissionFlagsBits.ManageChannels, PermissionFlagsBits.ManageMessages],
        },
      ],
      reason: auditReason(interaction.user.tag, 'Ticket opened'),
    });
    if (channel.type !== ChannelType.GuildText) {
      await channel.delete('Ticket setup failed').catch(() => undefined);
      throw new UserFacingError('I could not open a text channel for this ticket.');
    }
    created = channel;
    await channel.send({
      content: `${interaction.user} opened a ticket.`,
      embeds: [
        new EmbedBuilder()
          .setColor(0x5865f2)
          .setTitle('Ticket')
          .setDescription(reason)
          .setFooter({
            text: 'The opener or a staff member can close this ticket. Closing deletes the channel.',
          }),
      ],
      components: [closeTicketRow()],
      allowedMentions: { users: [interaction.user.id] },
    });
  } catch (error) {
    if (created) await created.delete('Ticket setup failed').catch(() => undefined);
    throw error;
  }
  await interaction.editReply(`Opened your ticket: ${created}.`);
}

async function requireTicketChannel(interaction: ButtonInteraction): Promise<TextChannel> {
  if (!interaction.guild) throw new UserFacingError('Use this button in a server ticket channel.');
  const channel = await interaction.guild.channels.fetch(interaction.channelId);
  if (!channel || channel.type !== ChannelType.GuildText || !openerFromTopic(channel.topic)) {
    throw new UserFacingError('This is not a ticket channel.');
  }
  return channel;
}

function assertCanClose(interaction: ButtonInteraction, channel: TextChannel, config: Config): void {
  const openerId = openerFromTopic(channel.topic);
  const allowed = interaction.user.id === openerId || memberHasRole(interaction.member, config.staffRoleId);
  if (!allowed) {
    throw new UserFacingError('Only the person who opened this ticket, or a staff member, can close it.');
  }
}
