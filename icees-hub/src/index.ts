import { Client, Events, GatewayIntentBits, MessageFlags, type Interaction } from 'discord.js';
import { handleAutocomplete, handleCommand } from './commands/handle';
import { handleButton, handleModal } from './components/handle';
import { loadConfig } from './config';
import { explainDiscordError, UserFacingError } from './lib/errors';
import { TICKET_CONFIRM_CLOSE_ID } from './lib/ids';

const config = loadConfig();

const client = new Client({
  intents: [GatewayIntentBits.Guilds],
});

client.once(Events.ClientReady, (readyClient) => {
  console.log(`icee's Hub is online as ${readyClient.user.tag}.`);
});

client.on(Events.InteractionCreate, (interaction) => {
  void dispatch(interaction).catch((error: unknown) => {
    if (!(error instanceof UserFacingError)) logError(error);
    return sendError(interaction, error);
  });
});

async function dispatch(interaction: Interaction): Promise<void> {
  if (interaction.isAutocomplete()) {
    await handleAutocomplete(interaction, config);
    return;
  }
  if (interaction.isChatInputCommand()) {
    await handleCommand(interaction, config);
    return;
  }
  if (interaction.isButton()) {
    await handleButton(interaction, config);
    return;
  }
  if (interaction.isModalSubmit()) {
    await handleModal(interaction, config);
  }
}

function actionFor(interaction: Interaction): string {
  if (interaction.isButton() || interaction.isModalSubmit()) {
    if (interaction.customId === TICKET_CONFIRM_CLOSE_ID) return 'delete that ticket';
    if (interaction.customId.startsWith('ticket')) return 'open that ticket';
    if (interaction.customId.startsWith('rules')) return 'assign the member role';
  }
  if (interaction.isChatInputCommand()) {
    const sub = interaction.options.getSubcommand(false);
    const key = sub ? `${interaction.commandName}:${sub}` : interaction.commandName;
    const actions: Record<string, string> = {
      rules: 'post the rules',
      ticket: 'post the ticket panel',
      'emoji:add': 'add that emoji',
      'emoji:list': 'list emojis',
      'emoji:remove': 'remove that emoji',
      'sticker:add': 'add that sticker',
      'sticker:list': 'list stickers',
      'sticker:remove': 'remove that sticker',
      'soundboard:add': 'add that sound',
      'soundboard:list': 'list sounds',
      'soundboard:remove': 'remove that sound',
    };
    return actions[key] ?? 'finish that';
  }
  return 'finish that';
}

async function sendError(interaction: Interaction, error: unknown): Promise<void> {
  if (interaction.isAutocomplete() || !interaction.isRepliable()) return;
  const content = explainDiscordError(error, actionFor(interaction));
  try {
    if (interaction.deferred && !interaction.replied) {
      await interaction.editReply({ content });
      return;
    }
    if (interaction.replied || interaction.deferred) {
      await interaction.followUp({ content, flags: MessageFlags.Ephemeral });
      return;
    }
    await interaction.reply({ content, flags: MessageFlags.Ephemeral });
  } catch {
    console.error('Failed to send an error reply.');
  }
}

function logError(error: unknown): void {
  if (error instanceof Error) {
    console.error(error.message);
    return;
  }
  console.error('Unknown interaction error');
}

client.login(config.discordToken).catch((error: unknown) => {
  const message = error instanceof Error ? error.message : 'unknown error';
  console.error(`Failed to log in. Check DISCORD_TOKEN. ${message}`);
  process.exit(1);
});
