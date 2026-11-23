import { UserFacingError } from './errors';

export function assertSnowflake(raw: string, label: string): string {
  const id = raw.trim();
  if (!/^\d{17,20}$/.test(id)) {
    throw new UserFacingError(`Choose a ${label} from the list.`);
  }
  return id;
}

export function assertEmojiName(raw: string): string {
  const name = raw.trim();
  if (!/^[A-Za-z0-9_]{2,32}$/.test(name)) {
    throw new UserFacingError(
      'Emoji names must be 2-32 characters and use only letters, numbers, and underscores.',
    );
  }
  return name;
}

export function assertStickerName(raw: string): string {
  const name = raw.trim().replace(/\s+/g, ' ');
  if (name.length < 2 || name.length > 30) {
    throw new UserFacingError('Sticker names must be 2-30 characters.');
  }
  return name;
}

export function assertStickerDescription(raw: string): string {
  const description = raw.trim().replace(/\s+/g, ' ');
  if (description.length < 2 || description.length > 100) {
    throw new UserFacingError('Sticker descriptions must be 2-100 characters.');
  }
  return description;
}

export function assertStickerTag(raw: string): string {
  const tag = raw.trim();
  const named = /^[A-Za-z0-9_]{1,32}$/.test(tag);
  const emoji =
    tag.length > 0 &&
    tag.length <= 32 &&
    !/[A-Za-z0-9]/.test(tag) &&
    /\p{Extended_Pictographic}/u.test(tag);
  if (!named && !emoji) {
    throw new UserFacingError('The sticker tag must be a standard emoji or its name, such as 👋 or wave.');
  }
  return named ? tag.toLowerCase() : tag;
}

export function assertSoundName(raw: string): string {
  const name = raw.trim().replace(/\s+/g, ' ');
  if (name.length < 2 || name.length > 32) {
    throw new UserFacingError('Sound names must be 2-32 characters.');
  }
  if (!/^[\p{L}\p{N} _'-]+$/u.test(name)) {
    throw new UserFacingError('Sound names can use letters, numbers, spaces, hyphens, and apostrophes.');
  }
  return name;
}

export function volumeFraction(value: number): number {
  if (!Number.isInteger(value) || value < 0 || value > 100) {
    throw new UserFacingError('Volume must be a whole number from 0 to 100.');
  }
  return value / 100;
}

export function assertTicketReason(raw: string): string {
  const reason = raw.trim().replace(/\s+/g, ' ');
  if (reason.length < 3 || reason.length > 100) {
    throw new UserFacingError('Enter a topic or reason between 3 and 100 characters.');
  }
  return reason;
}
