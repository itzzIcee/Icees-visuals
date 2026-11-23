import { ChannelType, type Guild, type TextChannel } from 'discord.js';

export const OPENER_PREFIX = 'ticket-opener:';

export function ticketTopic(userId: string, reason: string): string {
  const clean = reason.replace(/[\r\n]+/g, ' ').trim().slice(0, 900);
  return `${OPENER_PREFIX}${userId}\n${clean}`;
}

export function openerFromTopic(topic: string | null | undefined): string | null {
  if (!topic) return null;
  const first = topic.split('\n')[0]?.trim() ?? '';
  if (!first.startsWith(OPENER_PREFIX)) return null;
  const id = first.slice(OPENER_PREFIX.length).trim();
  return /^\d{17,20}$/.test(id) ? id : null;
}

export function ticketChannelName(username: string, userId: string): string {
  const slug = username
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 80);
  const suffix = slug.length > 0 ? slug : userId;
  return `ticket-${suffix}`.slice(0, 100);
}

export function findOpenTicket(guild: Guild, userId: string): TextChannel | null {
  const match = guild.channels.cache.find((channel) => {
    if (channel.type !== ChannelType.GuildText) return false;
    return openerFromTopic(channel.topic) === userId;
  });
  if (!match || match.type !== ChannelType.GuildText) return null;
  return match;
}
