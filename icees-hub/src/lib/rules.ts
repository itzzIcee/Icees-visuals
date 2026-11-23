import { readFileSync } from 'node:fs';
import path from 'node:path';
import { EmbedBuilder } from 'discord.js';
import { UserFacingError } from './errors';

export interface RulesFile {
  title: string;
  intro: string;
  rules: string[];
  footer: string;
}

export const defaultRulesPath = path.resolve(__dirname, '../../config/rules.json');

function requireText(value: unknown, label: string, max: number): string {
  if (typeof value !== 'string' || value.trim().length === 0) {
    throw new UserFacingError(`config/rules.json is missing text for ${label}.`);
  }
  const text = value.trim();
  if (text.length > max) {
    throw new UserFacingError(`config/rules.json ${label} is too long (maximum ${max} characters).`);
  }
  return text;
}

export function parseRules(value: unknown): RulesFile {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    throw new UserFacingError('config/rules.json must be a JSON object.');
  }
  const record = value as Record<string, unknown>;
  const title = requireText(record.title, 'title', 256);
  const intro = requireText(record.intro, 'intro', 1000);
  if (!Array.isArray(record.rules) || record.rules.length === 0) {
    throw new UserFacingError('config/rules.json needs a rules list with at least one rule.');
  }
  if (record.rules.length > 25) {
    throw new UserFacingError('config/rules.json can include at most 25 rules.');
  }
  const rules = record.rules.map((rule, index) => requireText(rule, `rules[${index}]`, 500));
  const footer =
    record.footer === undefined ? 'Tap I agree to accept these rules.' : requireText(record.footer, 'footer', 2048);
  return { title, intro, rules, footer };
}

export function rulesDescription(rules: RulesFile): string {
  const lines = rules.rules.map((rule, index) => `**${index + 1}.** ${rule}`);
  return [rules.intro, '', ...lines].join('\n');
}

export function loadRules(filePath = defaultRulesPath): RulesFile {
  let raw: string;
  try {
    raw = readFileSync(filePath, 'utf8');
  } catch {
    throw new UserFacingError('I could not read config/rules.json. Check that the file is in the bot folder.');
  }
  try {
    return parseRules(JSON.parse(raw) as unknown);
  } catch (error) {
    if (error instanceof UserFacingError) throw error;
    throw new UserFacingError('config/rules.json is not valid JSON.');
  }
}

export function rulesEmbed(rules: RulesFile): EmbedBuilder {
  const description = rulesDescription(rules);
  if (description.length > 4096) {
    throw new UserFacingError('The rules text is too long for one Discord message. Shorten config/rules.json.');
  }
  return new EmbedBuilder().setColor(0x5865f2).setTitle(rules.title).setDescription(description).setFooter({
    text: rules.footer,
  });
}
