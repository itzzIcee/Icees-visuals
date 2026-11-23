import {
  ActionRowBuilder,
  ButtonBuilder,
  ButtonStyle,
  ModalBuilder,
  TextInputBuilder,
  TextInputStyle,
} from 'discord.js';
import {
  RULES_AGREE_ID,
  TICKET_CANCEL_CLOSE_ID,
  TICKET_CLOSE_ID,
  TICKET_CONFIRM_CLOSE_ID,
  TICKET_OPEN_ID,
  TICKET_REASON_ID,
} from '../lib/ids';

export function agreeRow(): ActionRowBuilder<ButtonBuilder> {
  return new ActionRowBuilder<ButtonBuilder>().addComponents(
    new ButtonBuilder().setCustomId(RULES_AGREE_ID).setLabel('I agree').setStyle(ButtonStyle.Success),
  );
}

export function openTicketRow(): ActionRowBuilder<ButtonBuilder> {
  return new ActionRowBuilder<ButtonBuilder>().addComponents(
    new ButtonBuilder().setCustomId(TICKET_OPEN_ID).setLabel('Open a ticket').setStyle(ButtonStyle.Primary),
  );
}

export function closeTicketRow(): ActionRowBuilder<ButtonBuilder> {
  return new ActionRowBuilder<ButtonBuilder>().addComponents(
    new ButtonBuilder().setCustomId(TICKET_CLOSE_ID).setLabel('Close ticket').setStyle(ButtonStyle.Danger),
  );
}

export function confirmCloseRow(): ActionRowBuilder<ButtonBuilder> {
  return new ActionRowBuilder<ButtonBuilder>().addComponents(
    new ButtonBuilder()
      .setCustomId(TICKET_CONFIRM_CLOSE_ID)
      .setLabel('Delete ticket')
      .setStyle(ButtonStyle.Danger),
    new ButtonBuilder().setCustomId(TICKET_CANCEL_CLOSE_ID).setLabel('Cancel').setStyle(ButtonStyle.Secondary),
  );
}

export function ticketReasonModal(): ModalBuilder {
  return new ModalBuilder().setCustomId(TICKET_REASON_ID).setTitle('Open a ticket').addComponents(
    new ActionRowBuilder<TextInputBuilder>().addComponents(
      new TextInputBuilder()
        .setCustomId('reason')
        .setLabel('Topic or reason')
        .setStyle(TextInputStyle.Short)
        .setRequired(true)
        .setMinLength(3)
        .setMaxLength(100)
        .setPlaceholder('What do you need help with?'),
    ),
  );
}
