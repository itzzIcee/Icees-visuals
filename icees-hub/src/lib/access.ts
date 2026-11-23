import {
  PermissionFlagsBits,
  type APIInteractionGuildMember,
  type GuildMember,
  type PermissionsBitField,
} from 'discord.js';

type MemberLike = GuildMember | APIInteractionGuildMember | null;

export function memberHasRole(member: MemberLike, roleId: string): boolean {
  if (!member) return false;
  if (Array.isArray(member.roles)) return member.roles.includes(roleId);
  return member.roles.cache.has(roleId);
}

export function canPostPanels(
  permissions: Readonly<PermissionsBitField> | null,
  member: MemberLike,
  staffRoleId: string,
): boolean {
  if (memberHasRole(member, staffRoleId)) return true;
  return permissions?.has(PermissionFlagsBits.ManageGuild) ?? false;
}

export function canManageExpressions(
  permissions: Readonly<PermissionsBitField> | null,
  member: MemberLike,
  staffRoleId: string,
): boolean {
  if (memberHasRole(member, staffRoleId)) return true;
  if (!permissions) return false;
  return (
    permissions.has(PermissionFlagsBits.ManageGuildExpressions) ||
    permissions.has(PermissionFlagsBits.CreateGuildExpressions) ||
    permissions.has(PermissionFlagsBits.ManageEmojisAndStickers)
  );
}

export const STAFF_PANEL_MESSAGE =
  'You need the staff role, or the Manage Server permission, to use this command.';

export const EXPRESSIONS_MESSAGE =
  'You need the staff role, or permission to manage expressions (Manage Emojis and Stickers), to use this command.';
