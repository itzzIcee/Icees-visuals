import { REST, Routes } from 'discord.js';
import { commandJSON } from './commands/definitions';
import { loadRegisterConfig } from './config';

async function main(): Promise<void> {
  const globalRegister = process.argv.includes('--global');
  const config = loadRegisterConfig(globalRegister);
  const body = commandJSON();
  const rest = new REST({ version: '10' }).setToken(config.discordToken);
  if (globalRegister) {
    await rest.put(Routes.applicationCommands(config.clientId), { body });
    console.log(`Registered ${body.length} global commands: ${body.map((command) => command.name).join(', ')}.`);
    console.log('Global commands can take up to an hour to appear.');
    return;
  }
  await rest.put(Routes.applicationGuildCommands(config.clientId, config.guildId!), { body });
  console.log(
    `Registered ${body.length} commands for guild ${config.guildId}: ${body.map((command) => command.name).join(', ')}.`,
  );
}

main().catch((error: unknown) => {
  const message = error instanceof Error ? error.message : 'unknown error';
  console.error(`Failed to register commands. ${message}`);
  process.exit(1);
});
