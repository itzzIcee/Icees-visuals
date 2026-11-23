import { DiscordAPIError, HTTPError, RateLimitError } from 'discord.js';

export class UserFacingError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'UserFacingError';
  }
}

export interface DiscordFailure {
  code?: number | string;
  status?: number;
  message?: string;
  errors?: unknown;
  action: string;
}

const NITRO_EMOJI =
  'This server has reached its custom emoji limit for its Nitro Server Boost level. Boost the server, or remove an emoji, and try again.';
const NITRO_ANIMATED =
  'This server has reached its animated emoji limit for its Nitro Server Boost level. Boost the server, or remove an animated emoji, and try again.';
const NITRO_STICKER =
  'This server has reached its custom sticker limit for its Nitro Server Boost level. Boost the server, or remove a sticker, and try again.';
const NITRO_SOUND =
  'This server has reached its soundboard limit for its Nitro Server Boost level. Boost the server, or remove a sound, and try again.';

export function permissionMessage(action: string): string {
  if (action.includes('role')) {
    return "I cannot assign that role. Move the icee's Hub role above the member role, and grant me Manage Roles.";
  }
  if (action.includes('ticket') || action.includes('channel') || action.includes('post')) {
    return 'I cannot manage that channel. Grant me Manage Channels, View Channel, Send Messages, and Embed Links.';
  }
  if (action.includes('emoji') || action.includes('sticker') || action.includes('sound')) {
    return 'I do not have permission to manage expressions. Grant me Create Expressions and Manage Expressions (Manage Emojis and Stickers).';
  }
  return 'I do not have permission to do that. Check my role and channel permissions.';
}

export function flattenDiscordErrors(node: unknown, prefix = ''): string[] {
  if (!node || typeof node !== 'object') return [];
  const record = node as Record<string, unknown>;
  const messages: string[] = [];
  if (Array.isArray(record._errors)) {
    for (const entry of record._errors) {
      if (!entry || typeof entry !== 'object' || !('message' in entry)) continue;
      const text = (entry as { message?: unknown }).message;
      if (typeof text !== 'string' || text.trim().length === 0) continue;
      messages.push(prefix ? `${prefix}: ${text}` : text);
    }
  }
  for (const [key, value] of Object.entries(record)) {
    if (key === '_errors') continue;
    const next = prefix ? `${prefix}.${key}` : key;
    messages.push(...flattenDiscordErrors(value, next));
  }
  return messages;
}

function fileTooLarge(action: string): string {
  if (action.includes('emoji')) return 'That file is too large. Custom emojis must be 256 KiB or smaller.';
  if (action.includes('sticker')) {
    return 'That file is too large. Custom stickers must be 512 KiB or smaller.';
  }
  if (action.includes('sound')) {
    return 'That file is too large. Soundboard sounds must be 512 KiB or smaller and 5.2 seconds or shorter.';
  }
  return 'That file is too large for Discord.';
}

function messageForCode(code: number, action: string, detail: string | null): string | null {
  switch (code) {
    case 20031:
      return 'Discord rejected that name. Try a different one.';
    case 10003:
      return 'That channel no longer exists.';
    case 10007:
      return 'That member is not in this server.';
    case 10011:
      return 'That role does not exist. Check RULES_ROLE_ID and STAFF_ROLE_ID.';
    case 10014:
      return 'That emoji no longer exists.';
    case 10060:
      return 'That sticker no longer exists.';
    case 30008:
      return NITRO_EMOJI;
    case 30013:
      return 'This server has reached Discord’s channel limit, so I cannot open another ticket.';
    case 30018:
      return NITRO_ANIMATED;
    case 30039:
      return NITRO_STICKER;
    case 30045:
      return NITRO_SOUND;
    case 30056:
      return 'This server has reached its premium emoji limit.';
    case 40005:
    case 50045:
    case 50138:
      return fileTooLarge(action);
    case 50001:
      return 'I cannot access that. Check that I am in the server and can view the channel.';
    case 50013:
      return permissionMessage(action);
    case 50035:
      return detail
        ? `Discord rejected those details. ${detail}`
        : 'Discord rejected those details. Check the name, file type, tags, and size limits in the README.';
    case 50046:
      return 'Discord rejected that file. Use a supported format and stay under the size limit in the README.';
    case 170001:
      return 'That Lottie sticker is not valid JSON.';
    case 170002:
      return 'That Lottie sticker cannot contain PNG or JPEG images.';
    case 170003:
    case 170006:
      return 'That sticker’s frame rate is outside Discord’s allowed range.';
    case 170004:
      return 'That sticker has too many frames. Discord allows at most 1000.';
    case 170005:
      return 'That sticker is larger than 320 by 320 pixels.';
    case 170007:
      return 'That sticker is longer than 5 seconds.';
    default:
      return null;
  }
}

function messageFromText(message: string, action: string): string | null {
  const text = message.toLowerCase();
  if (text.includes('maximum number of animated emojis')) return NITRO_ANIMATED;
  if (text.includes('maximum number of emojis')) return NITRO_EMOJI;
  if (text.includes('maximum number of stickers')) return NITRO_STICKER;
  if (text.includes('maximum number of soundboard')) return NITRO_SOUND;
  if (text.includes('file size') || text.includes('too large') || text.includes('entity too large')) {
    return fileTooLarge(action);
  }
  if (text.includes('missing permission') || text.includes('lack permission')) return permissionMessage(action);
  return null;
}

export function explainDiscordFailure(input: DiscordFailure): string {
  const code = typeof input.code === 'number' ? input.code : Number(input.code);
  const detailParts = flattenDiscordErrors(input.errors).slice(0, 3);
  const detail = detailParts.length > 0 ? detailParts.join(' ') : null;
  if (Number.isFinite(code)) {
    const known = messageForCode(code, input.action, detail);
    if (known) return known;
  }
  if (input.status === 413) return fileTooLarge(input.action);
  if (input.message) {
    const fromText = messageFromText(input.message, input.action);
    if (fromText) return fromText;
  }
  if (Number.isFinite(code) && code > 0) {
    return `Discord could not ${input.action} (error ${code}).`;
  }
  return `Discord could not ${input.action}.`;
}

export function explainDiscordError(error: unknown, action: string): string {
  if (error instanceof UserFacingError) return error.message;
  if (error instanceof DiscordAPIError) {
    const raw = error.rawError;
    const errors = raw && 'errors' in raw ? raw.errors : undefined;
    const message = raw && 'message' in raw && typeof raw.message === 'string' ? raw.message : error.message;
    return explainDiscordFailure({
      code: error.code,
      status: error.status,
      message,
      errors,
      action,
    });
  }
  if (error instanceof RateLimitError) {
    return 'Discord is rate limiting this action. Wait a moment and try again.';
  }
  if (error instanceof HTTPError) {
    if (error.status === 413) return fileTooLarge(action);
    return `Discord could not ${action} (HTTP ${error.status}).`;
  }
  return `Something went wrong while trying to ${action}. Please try again.`;
}
