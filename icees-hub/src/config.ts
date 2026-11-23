import path from 'node:path';
import dotenv from 'dotenv';

export interface Config {
  discordToken: string;
  clientId: string;
  guildId: string;
  rulesRoleId: string;
  staffRoleId: string;
}

export interface RegisterConfig {
  discordToken: string;
  clientId: string;
  guildId?: string;
}

const SNOWFLAKE = /^\d{17,20}$/;

function readEnv(keys: string[]): Record<string, string> {
  dotenv.config({ path: path.resolve(__dirname, '../.env') });
  const missing = keys.filter((key) => !process.env[key]?.trim());
  if (missing.length > 0) {
    console.error(`Missing environment variables: ${missing.join(', ')}.`);
    console.error('Copy .env.example to .env and fill in each value. See README.md for where to find them.');
    process.exit(1);
  }
  return Object.fromEntries(keys.map((key) => [key, process.env[key]!.trim()]));
}

function assertIds(values: Record<string, string>, keys: string[]): void {
  const bad = keys.filter((key) => !SNOWFLAKE.test(values[key] ?? ''));
  if (bad.length > 0) {
    console.error(`These environment variables must be Discord IDs (numbers only): ${bad.join(', ')}.`);
    process.exit(1);
  }
}

export function loadConfig(): Config {
  const values = readEnv(['DISCORD_TOKEN', 'CLIENT_ID', 'GUILD_ID', 'RULES_ROLE_ID', 'STAFF_ROLE_ID']);
  assertIds(values, ['CLIENT_ID', 'GUILD_ID', 'RULES_ROLE_ID', 'STAFF_ROLE_ID']);
  if (values.RULES_ROLE_ID === values.STAFF_ROLE_ID) {
    console.error('RULES_ROLE_ID and STAFF_ROLE_ID must be different roles.');
    process.exit(1);
  }
  if (values.RULES_ROLE_ID === values.GUILD_ID || values.STAFF_ROLE_ID === values.GUILD_ID) {
    console.error('RULES_ROLE_ID and STAFF_ROLE_ID must be role IDs, not the server ID.');
    process.exit(1);
  }
  return {
    discordToken: values.DISCORD_TOKEN,
    clientId: values.CLIENT_ID,
    guildId: values.GUILD_ID,
    rulesRoleId: values.RULES_ROLE_ID,
    staffRoleId: values.STAFF_ROLE_ID,
  };
}

export function loadRegisterConfig(globalRegister: boolean): RegisterConfig {
  const keys = globalRegister ? ['DISCORD_TOKEN', 'CLIENT_ID'] : ['DISCORD_TOKEN', 'CLIENT_ID', 'GUILD_ID'];
  const values = readEnv(keys);
  assertIds(values, globalRegister ? ['CLIENT_ID'] : ['CLIENT_ID', 'GUILD_ID']);
  return {
    discordToken: values.DISCORD_TOKEN,
    clientId: values.CLIENT_ID,
    guildId: values.GUILD_ID,
  };
}
