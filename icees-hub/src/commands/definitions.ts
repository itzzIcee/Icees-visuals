import { ChannelType, InteractionContextType, SlashCommandBuilder } from 'discord.js';

export const rulesCommand = new SlashCommandBuilder()
  .setName('rules')
  .setDescription('Post the server rules and an I agree button')
  .setContexts(InteractionContextType.Guild)
  .addChannelOption((option) =>
    option
      .setName('channel')
      .setDescription('Channel to post in. Defaults to the current channel.')
      .addChannelTypes(ChannelType.GuildText, ChannelType.GuildAnnouncement),
  );

export const ticketCommand = new SlashCommandBuilder()
  .setName('ticket')
  .setDescription('Post the support ticket panel')
  .setContexts(InteractionContextType.Guild)
  .addChannelOption((option) =>
    option
      .setName('channel')
      .setDescription('Channel to post in. Defaults to the current channel.')
      .addChannelTypes(ChannelType.GuildText, ChannelType.GuildAnnouncement),
  );

export const emojiCommand = new SlashCommandBuilder()
  .setName('emoji')
  .setDescription('Add, list, or remove custom emojis')
  .setContexts(InteractionContextType.Guild)
  .addSubcommand((subcommand) =>
    subcommand
      .setName('add')
      .setDescription('Add a custom emoji from an image or a URL')
      .addStringOption((option) =>
        option
          .setName('name')
          .setDescription('Letters, numbers, and underscores, 2-32 characters')
          .setRequired(true)
          .setMinLength(2)
          .setMaxLength(32),
      )
      .addAttachmentOption((option) =>
        option.setName('image').setDescription('PNG, JPEG, or GIF, up to 256 KiB').setRequired(false),
      )
      .addStringOption((option) =>
        option
          .setName('url')
          .setDescription('https link to a PNG, JPEG, or GIF, up to 256 KiB')
          .setRequired(false)
          .setMaxLength(2000),
      ),
  )
  .addSubcommand((subcommand) => subcommand.setName('list').setDescription("List this server's custom emojis"))
  .addSubcommand((subcommand) =>
    subcommand
      .setName('remove')
      .setDescription('Remove a custom emoji')
      .addStringOption((option) =>
        option
          .setName('emoji')
          .setDescription('Emoji to remove')
          .setRequired(true)
          .setAutocomplete(true),
      ),
  );

export const stickerCommand = new SlashCommandBuilder()
  .setName('sticker')
  .setDescription('Add, list, or remove custom stickers')
  .setContexts(InteractionContextType.Guild)
  .addSubcommand((subcommand) =>
    subcommand
      .setName('add')
      .setDescription('Add a custom sticker')
      .addStringOption((option) =>
        option.setName('name').setDescription('Sticker name, 2-30 characters').setRequired(true).setMaxLength(30),
      )
      .addStringOption((option) =>
        option
          .setName('description')
          .setDescription('Short description, 2-100 characters')
          .setRequired(true)
          .setMaxLength(100),
      )
      .addStringOption((option) =>
        option
          .setName('tags')
          .setDescription('A standard emoji or its name, such as wave')
          .setRequired(true)
          .setMaxLength(32),
      )
      .addAttachmentOption((option) =>
        option
          .setName('file')
          .setDescription('PNG, APNG, GIF, or Lottie JSON, up to 512 KiB')
          .setRequired(true),
      ),
  )
  .addSubcommand((subcommand) => subcommand.setName('list').setDescription("List this server's custom stickers"))
  .addSubcommand((subcommand) =>
    subcommand
      .setName('remove')
      .setDescription('Remove a custom sticker')
      .addStringOption((option) =>
        option
          .setName('sticker')
          .setDescription('Sticker to remove')
          .setRequired(true)
          .setAutocomplete(true),
      ),
  );

export const soundboardCommand = new SlashCommandBuilder()
  .setName('soundboard')
  .setDescription('Add, list, or remove soundboard sounds')
  .setContexts(InteractionContextType.Guild)
  .addSubcommand((subcommand) =>
    subcommand
      .setName('add')
      .setDescription('Add a soundboard sound')
      .addStringOption((option) =>
        option.setName('name').setDescription('Sound name, 2-32 characters').setRequired(true).setMaxLength(32),
      )
      .addIntegerOption((option) =>
        option
          .setName('volume')
          .setDescription('Volume from 0 to 100. 100 is full volume.')
          .setRequired(true)
          .setMinValue(0)
          .setMaxValue(100),
      )
      .addAttachmentOption((option) =>
        option
          .setName('file')
          .setDescription('MP3 or Ogg file, up to 512 KiB and 5.2 seconds')
          .setRequired(true),
      ),
  )
  .addSubcommand((subcommand) =>
    subcommand.setName('list').setDescription("List this server's soundboard sounds"),
  )
  .addSubcommand((subcommand) =>
    subcommand
      .setName('remove')
      .setDescription('Remove a soundboard sound')
      .addStringOption((option) =>
        option
          .setName('sound')
          .setDescription('Sound to remove')
          .setRequired(true)
          .setAutocomplete(true),
      ),
  );

export const commands = [rulesCommand, ticketCommand, emojiCommand, stickerCommand, soundboardCommand];

export function commandJSON() {
  return commands.map((command) => command.toJSON());
}
