import {
  ChannelType,
  EmbedBuilder,
  MessageFlags,
  PermissionFlagsBits,
  type AutocompleteInteraction,
  type ChatInputCommandInteraction,
  type Guild,
  type NewsChannel,
  type TextChannel,
} from 'discord.js';
import type { Config } from '../config';
import { agreeRow, openTicketRow } from '../components/widgets';
import {
  canManageExpressions,
  canPostPanels,
  EXPRESSIONS_MESSAGE,
  STAFF_PANEL_MESSAGE,
} from '../lib/access';
import {
  downloadPublicFile,
  prepareEmoji,
  prepareSound,
  prepareSticker,
  tooLargeMessage,
} from '../lib/assets';
import { UserFacingError } from '../lib/errors';
import { auditReason, countLabel, fitLines, uploaderLabel } from '../lib/format';
import { EMOJI_MAX_BYTES, SOUND_MAX_BYTES, STICKER_MAX_BYTES } from '../lib/ids';
import { loadRules, rulesEmbed } from '../lib/rules';
import {
  assertEmojiName,
  assertSnowflake,
  assertSoundName,
  assertStickerDescription,
  assertStickerName,
  assertStickerTag,
  volumeFraction,
} from '../lib/validate';

type Postable = TextChannel | NewsChannel;

export async function handleAutocomplete(interaction: AutocompleteInteraction, config: Config): Promise<void> {
  try {
    if (
      !interaction.guild ||
      !canManageExpressions(interaction.memberPermissions, interaction.member, config.staffRoleId)
    ) {
      await interaction.respond([]);
      return;
    }
    const query = interaction.options.getFocused().toLowerCase();
    const botId = interaction.client.user?.id ?? '';
    const choices = await choicesFor(interaction, query, botId);
    await interaction.respond(choices.slice(0, 25));
  } catch (error) {
    console.error(`Autocomplete failed: ${error instanceof Error ? error.message : 'unknown error'}`);
    if (!interaction.responded) await interaction.respond([]).catch(() => undefined);
  }
}

export async function handleCommand(interaction: ChatInputCommandInteraction, config: Config): Promise<void> {
  switch (interaction.commandName) {
    case 'rules':
      await postRules(interaction, config);
      return;
    case 'ticket':
      await postTicketPanel(interaction, config);
      return;
    case 'emoji':
      await handleEmoji(interaction, config);
      return;
    case 'sticker':
      await handleSticker(interaction, config);
      return;
    case 'soundboard':
      await handleSoundboard(interaction, config);
      return;
    default:
      throw new UserFacingError('Unknown command.');
  }
}

async function postRules(interaction: ChatInputCommandInteraction, config: Config): Promise<void> {
  requirePanels(interaction, config);
  await interaction.deferReply({ flags: MessageFlags.Ephemeral });
  const guild = await requireGuild(interaction);
  const channel = await resolvePostChannel(interaction, guild);
  const embed = rulesEmbed(loadRules());
  await channel.send({ embeds: [embed], components: [agreeRow()] });
  await interaction.editReply(`Posted the rules in ${channel}.`);
}

async function postTicketPanel(interaction: ChatInputCommandInteraction, config: Config): Promise<void> {
  requirePanels(interaction, config);
  await interaction.deferReply({ flags: MessageFlags.Ephemeral });
  const guild = await requireGuild(interaction);
  const staffRole = await guild.roles.fetch(config.staffRoleId);
  if (!staffRole) {
    throw new UserFacingError('The staff role is missing. Ask an admin to check STAFF_ROLE_ID.');
  }
  const channel = await resolvePostChannel(interaction, guild);
  const embed = new EmbedBuilder()
    .setColor(0x5865f2)
    .setTitle('Support tickets')
    .setDescription(
      'Need help from the staff? Tap **Open a ticket**, tell us the topic, and a private channel will open for you. You can have one open ticket at a time.',
    );
  await channel.send({ embeds: [embed], components: [openTicketRow()] });
  await interaction.editReply(`Posted the ticket panel in ${channel}.`);
}

async function handleEmoji(interaction: ChatInputCommandInteraction, config: Config): Promise<void> {
  requireExpressions(interaction, config);
  const subcommand = interaction.options.getSubcommand();
  if (subcommand === 'add') assertEmojiName(interaction.options.getString('name', true));
  await interaction.deferReply({ flags: MessageFlags.Ephemeral });
  const guild = await requireGuild(interaction);
  if (subcommand === 'add') {
    const name = assertEmojiName(interaction.options.getString('name', true));
    const data = await readEmojiSource(interaction);
    const prepared = prepareEmoji(data);
    const existing = (await guild.emojis.fetch()).find(
      (emoji) => emoji.name?.toLowerCase() === name.toLowerCase(),
    );
    if (existing) throw new UserFacingError(`An emoji named \`${name}\` already exists.`);
    const created = await guild.emojis.create({
      attachment: prepared.dataUri,
      name,
      reason: auditReason(interaction.user.tag, 'Emoji added'),
    });
    await interaction.editReply(
      prepared.animated
        ? `Added animated emoji ${created} as \`:${created.name}:\`.`
        : `Added emoji ${created} as \`:${created.name}:\`.`,
    );
    return;
  }
  if (subcommand === 'list') {
    const botId = interaction.client.user.id;
    const emojis = [...(await guild.emojis.fetch()).values()].sort(byBotThenName(botId));
    const lines = emojis.map((emoji) => {
      const notes = [uploaderLabel(emoji.author?.id, botId, emoji.author?.tag)];
      if (emoji.animated) notes.push('animated');
      if (emoji.managed) notes.push('managed');
      return `${emoji} \`:${emoji.name ?? 'emoji'}:\` — \`${emoji.id}\` — ${notes.join(', ')}`;
    });
    await interaction.editReply({
      embeds: [
        listEmbed(
          'Custom emojis',
          lines,
          'This server has no custom emojis yet. Add one with `/emoji add`.',
          ['emoji', 'emojis'],
        ),
      ],
    });
    return;
  }
  if (subcommand === 'remove') {
    const id = assertSnowflake(interaction.options.getString('emoji', true), 'emoji');
    const emoji = await guild.emojis.fetch(id);
    if (emoji.managed) {
      throw new UserFacingError('That emoji is managed by an integration, so I cannot remove it.');
    }
    const name = emoji.name ?? 'emoji';
    await emoji.delete(auditReason(interaction.user.tag, 'Emoji removed'));
    await interaction.editReply(`Removed emoji \`:${name}:\`.`);
    return;
  }
  throw new UserFacingError('Unknown emoji command.');
}

async function handleSticker(interaction: ChatInputCommandInteraction, config: Config): Promise<void> {
  requireExpressions(interaction, config);
  const subcommand = interaction.options.getSubcommand();
  if (subcommand === 'add') {
    assertStickerName(interaction.options.getString('name', true));
    assertStickerDescription(interaction.options.getString('description', true));
    assertStickerTag(interaction.options.getString('tags', true));
  }
  await interaction.deferReply({ flags: MessageFlags.Ephemeral });
  const guild = await requireGuild(interaction);
  if (subcommand === 'add') {
    const name = assertStickerName(interaction.options.getString('name', true));
    const description = assertStickerDescription(interaction.options.getString('description', true));
    const tags = assertStickerTag(interaction.options.getString('tags', true));
    const file = prepareSticker(await readAttachment(interaction, 'file', STICKER_MAX_BYTES, 'Custom stickers'));
    const created = await guild.stickers.create({
      file: { attachment: file.data, name: file.filename },
      name,
      description,
      tags,
      reason: auditReason(interaction.user.tag, 'Sticker added'),
    });
    await interaction.editReply(`Added sticker **${created.name}**.`);
    return;
  }
  if (subcommand === 'list') {
    const botId = interaction.client.user.id;
    const stickers = [...(await guild.stickers.fetch()).values()].sort((a, b) => a.name.localeCompare(b.name));
    const lines = stickers.map((sticker) => {
      const who = uploaderLabel(sticker.user?.id, botId, sticker.user?.tag);
      const description = sticker.description ? ` — ${sticker.description}` : '';
      const unavailable = sticker.available === false ? ' — unavailable until this server boosts again' : '';
      return `**${sticker.name}** — \`${sticker.id}\` — ${who}${description}${unavailable}`;
    });
    await interaction.editReply({
      embeds: [
        listEmbed(
          'Custom stickers',
          lines,
          'This server has no custom stickers yet. Add one with `/sticker add`.',
          ['sticker', 'stickers'],
        ),
      ],
    });
    return;
  }
  if (subcommand === 'remove') {
    const id = assertSnowflake(interaction.options.getString('sticker', true), 'sticker');
    const sticker = await guild.stickers.fetch(id);
    const name = sticker.name;
    await sticker.delete(auditReason(interaction.user.tag, 'Sticker removed'));
    await interaction.editReply(`Removed sticker **${name}**.`);
    return;
  }
  throw new UserFacingError('Unknown sticker command.');
}

async function handleSoundboard(interaction: ChatInputCommandInteraction, config: Config): Promise<void> {
  requireExpressions(interaction, config);
  const subcommand = interaction.options.getSubcommand();
  if (subcommand === 'add') {
    assertSoundName(interaction.options.getString('name', true));
    volumeFraction(interaction.options.getInteger('volume', true));
  }
  await interaction.deferReply({ flags: MessageFlags.Ephemeral });
  const guild = await requireGuild(interaction);
  if (subcommand === 'add') {
    const name = assertSoundName(interaction.options.getString('name', true));
    const volume = volumeFraction(interaction.options.getInteger('volume', true));
    const file = prepareSound(await readAttachment(interaction, 'file', SOUND_MAX_BYTES, 'Soundboard sounds'));
    const created = await guild.soundboardSounds.create({
      file: file.data,
      name,
      volume,
      contentType: file.contentType,
      reason: auditReason(interaction.user.tag, 'Sound added'),
    });
    await interaction.editReply(`Added soundboard sound **${created.name}** at volume ${Math.round(volume * 100)}.`);
    return;
  }
  if (subcommand === 'list') {
    const botId = interaction.client.user.id;
    const sounds = [...(await guild.soundboardSounds.fetch()).values()].sort((a, b) => a.name.localeCompare(b.name));
    const lines = sounds.map((sound) => {
      const who = uploaderLabel(sound.user?.id, botId, sound.user?.tag);
      const unavailable = sound.available ? '' : ' — unavailable until this server boosts again';
      return `**${sound.name}** — \`${sound.soundId}\` — volume ${Math.round(sound.volume * 100)} — ${who}${unavailable}`;
    });
    await interaction.editReply({
      embeds: [
        listEmbed(
          'Soundboard sounds',
          lines,
          'This server has no custom soundboard sounds yet. Add one with `/soundboard add`.',
          ['sound', 'sounds'],
        ),
      ],
    });
    return;
  }
  if (subcommand === 'remove') {
    const id = assertSnowflake(interaction.options.getString('sound', true), 'sound');
    const sounds = await guild.soundboardSounds.fetch();
    const sound = sounds.get(id);
    if (!sound) throw new UserFacingError('That sound does not exist in this server.');
    const name = sound.name;
    await sound.delete(auditReason(interaction.user.tag, 'Sound removed'));
    await interaction.editReply(`Removed soundboard sound **${name}**.`);
    return;
  }
  throw new UserFacingError('Unknown soundboard command.');
}

function requirePanels(interaction: ChatInputCommandInteraction, config: Config): void {
  if (!canPostPanels(interaction.memberPermissions, interaction.member, config.staffRoleId)) {
    throw new UserFacingError(STAFF_PANEL_MESSAGE);
  }
}

function requireExpressions(interaction: ChatInputCommandInteraction, config: Config): void {
  if (!canManageExpressions(interaction.memberPermissions, interaction.member, config.staffRoleId)) {
    throw new UserFacingError(EXPRESSIONS_MESSAGE);
  }
}

async function requireGuild(interaction: ChatInputCommandInteraction): Promise<Guild> {
  if (interaction.guild) return interaction.guild;
  if (interaction.guildId) return interaction.client.guilds.fetch(interaction.guildId);
  throw new UserFacingError('Use this command in a server.');
}

async function resolvePostChannel(interaction: ChatInputCommandInteraction, guild: Guild): Promise<Postable> {
  const selected = interaction.options.getChannel('channel');
  const id = selected?.id ?? interaction.channelId;
  if (!id) throw new UserFacingError('Choose a server text channel.');
  const channel = await guild.channels.fetch(id);
  if (!channel || (channel.type !== ChannelType.GuildText && channel.type !== ChannelType.GuildAnnouncement)) {
    throw new UserFacingError('Choose a server text channel.');
  }
  const me = guild.members.me ?? (await guild.members.fetchMe());
  const perms = channel.permissionsFor(me);
  const needed = [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages, PermissionFlagsBits.EmbedLinks];
  if (!perms?.has(needed)) {
    throw new UserFacingError(
      'I cannot post embeds in that channel. I need View Channel, Send Messages, and Embed Links there.',
    );
  }
  return channel;
}

async function readEmojiSource(interaction: ChatInputCommandInteraction): Promise<Buffer> {
  const attachment = interaction.options.getAttachment('image');
  const url = interaction.options.getString('url')?.trim() || null;
  if (!attachment && !url) {
    throw new UserFacingError('Attach an image or provide an https URL.');
  }
  if (attachment && url) {
    throw new UserFacingError('Attach an image or provide a URL, not both.');
  }
  if (attachment) {
    if (attachment.size > EMOJI_MAX_BYTES) {
      throw new UserFacingError(tooLargeMessage('Custom emojis', EMOJI_MAX_BYTES));
    }
    return downloadPublicFile(attachment.url, EMOJI_MAX_BYTES, 'Custom emojis');
  }
  return downloadPublicFile(url!, EMOJI_MAX_BYTES, 'Custom emojis');
}

async function readAttachment(
  interaction: ChatInputCommandInteraction,
  option: string,
  maxBytes: number,
  label: string,
): Promise<Buffer> {
  const attachment = interaction.options.getAttachment(option, true);
  if (attachment.size > maxBytes) throw new UserFacingError(tooLargeMessage(label, maxBytes));
  return downloadPublicFile(attachment.url, maxBytes, label);
}

function listEmbed(title: string, lines: string[], empty: string, noun: [string, string]): EmbedBuilder {
  if (lines.length === 0) {
    return new EmbedBuilder().setColor(0x5865f2).setTitle(title).setDescription(empty);
  }
  const fitted = fitLines(lines);
  const footer =
    fitted.hidden > 0
      ? `Showing ${lines.length - fitted.hidden} of ${lines.length}. Use the remove command to search the rest.`
      : countLabel(lines.length, noun[0], noun[1]);
  return new EmbedBuilder().setColor(0x5865f2).setTitle(title).setDescription(fitted.text).setFooter({ text: footer });
}

function byBotThenName(botId: string) {
  return (a: { name: string | null; author: { id: string } | null }, b: { name: string | null; author: { id: string } | null }) => {
    const aBot = a.author?.id === botId ? 0 : 1;
    const bBot = b.author?.id === botId ? 0 : 1;
    if (aBot !== bBot) return aBot - bBot;
    return (a.name ?? '').localeCompare(b.name ?? '');
  };
}

async function choicesFor(
  interaction: AutocompleteInteraction,
  query: string,
  botId: string,
): Promise<{ name: string; value: string }[]> {
  const guild = interaction.guild;
  if (!guild) return [];
  if (interaction.commandName === 'emoji') {
    const emojis = guild.emojis.cache.size > 0 ? guild.emojis.cache : await guild.emojis.fetch();
    return [...emojis.values()]
      .filter((emoji) => matches(emoji.name ?? '', emoji.id, query))
      .sort(byBotThenName(botId))
      .map((emoji) => ({
        name: clip(`${emoji.name ?? 'emoji'}${emoji.animated ? ' (animated)' : ''}`),
        value: emoji.id,
      }));
  }
  if (interaction.commandName === 'sticker') {
    const stickers = guild.stickers.cache.size > 0 ? guild.stickers.cache : await guild.stickers.fetch();
    return [...stickers.values()]
      .filter((sticker) => matches(sticker.name, sticker.id, query))
      .sort((a, b) => Number(b.user?.id === botId) - Number(a.user?.id === botId) || a.name.localeCompare(b.name))
      .map((sticker) => ({ name: clip(sticker.name), value: sticker.id }));
  }
  if (interaction.commandName === 'soundboard') {
    const sounds = await guild.soundboardSounds.fetch();
    return [...sounds.values()]
      .filter((sound) => matches(sound.name, sound.soundId, query))
      .sort((a, b) => Number(b.user?.id === botId) - Number(a.user?.id === botId) || a.name.localeCompare(b.name))
      .map((sound) => ({ name: clip(sound.name), value: sound.soundId }));
  }
  return [];
}

function matches(name: string, id: string, query: string): boolean {
  if (!query) return true;
  return name.toLowerCase().includes(query) || id.startsWith(query);
}

function clip(value: string): string {
  return value.length <= 100 ? value : `${value.slice(0, 99)}…`;
}
