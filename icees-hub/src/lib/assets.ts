import { lookup } from 'node:dns/promises';
import { isIP } from 'node:net';
import { EMOJI_MAX_BYTES, SOUND_MAX_BYTES, STICKER_MAX_BYTES } from './ids';
import { UserFacingError } from './errors';

export type EmojiKind = 'png' | 'jpeg' | 'gif';
export type StickerKind = 'png' | 'gif' | 'json';
export type SoundKind = 'mp3' | 'ogg';

const BLOCKED_HOSTS = new Set(['localhost', 'metadata.google.internal', 'metadata.google.internal.']);

export function isPrivateAddress(address: string): boolean {
  const version = isIP(address);
  if (version === 4) {
    const [a, b] = address.split('.').map((part) => Number(part));
    if (a === 0 || a === 10 || a === 127) return true;
    if (a === 169 && b === 254) return true;
    if (a === 172 && b >= 16 && b <= 31) return true;
    if (a === 192 && b === 168) return true;
    if (a === 100 && b >= 64 && b <= 127) return true;
    return false;
  }
  if (version === 6) {
    const hex = address.toLowerCase().split('%')[0] ?? '';
    if (hex === '::' || hex === '::1') return true;
    if (hex.startsWith('fc') || hex.startsWith('fd')) return true;
    if (/^fe[89ab]/.test(hex)) return true;
    const mapped = hex.match(/::ffff:(\d+\.\d+\.\d+\.\d+)$/);
    if (mapped?.[1]) return isPrivateAddress(mapped[1]);
  }
  return false;
}

export function parsePublicHttpsUrl(raw: string): URL {
  let url: URL;
  try {
    url = new URL(raw.trim());
  } catch {
    throw new UserFacingError('That URL is not valid. Use an https link.');
  }
  if (url.protocol !== 'https:') {
    throw new UserFacingError('The URL must start with https.');
  }
  if (url.username || url.password) {
    throw new UserFacingError('That URL is not allowed.');
  }
  const host = url.hostname.toLowerCase().replace(/^\[|\]$/g, '');
  if (BLOCKED_HOSTS.has(host) || host.endsWith('.local') || host.endsWith('.localhost') || host.endsWith('.internal')) {
    throw new UserFacingError('That URL is not allowed.');
  }
  if (isIP(host) && isPrivateAddress(host)) {
    throw new UserFacingError('That URL is not allowed.');
  }
  return url;
}

async function assertResolvedPublic(hostname: string): Promise<void> {
  const host = hostname.replace(/^\[|\]$/g, '');
  if (isIP(host)) {
    if (isPrivateAddress(host)) throw new UserFacingError('That URL is not allowed.');
    return;
  }
  let results: { address: string }[];
  try {
    results = await lookup(host, { all: true, verbatim: true });
  } catch {
    throw new UserFacingError('I could not resolve that URL.');
  }
  if (results.length === 0 || results.some((entry) => isPrivateAddress(entry.address))) {
    throw new UserFacingError('That URL is not allowed.');
  }
}

export function sniffEmoji(data: Buffer): EmojiKind | null {
  if (data.length >= 8 && data[0] === 0x89 && data[1] === 0x50 && data[2] === 0x4e && data[3] === 0x47) {
    return 'png';
  }
  if (data.length >= 3 && data[0] === 0xff && data[1] === 0xd8 && data[2] === 0xff) return 'jpeg';
  if (data.length >= 6) {
    const header = data.subarray(0, 6).toString('ascii');
    if (header === 'GIF87a' || header === 'GIF89a') return 'gif';
  }
  return null;
}

export function sniffSticker(data: Buffer): StickerKind | null {
  const emoji = sniffEmoji(data);
  if (emoji === 'png' || emoji === 'gif') return emoji;
  const start = data.subarray(0, Math.min(data.length, 64)).toString('utf8').trimStart();
  if (start.startsWith('{')) return 'json';
  return null;
}

export function sniffSound(data: Buffer): SoundKind | null {
  if (data.length >= 4 && data.subarray(0, 4).toString('ascii') === 'OggS') return 'ogg';
  if (data.length >= 3 && data.subarray(0, 3).toString('ascii') === 'ID3') return 'mp3';
  if (data.length >= 2 && data[0] === 0xff && (data[1] & 0xe0) === 0xe0) return 'mp3';
  return null;
}

export function emojiContentType(kind: EmojiKind): string {
  if (kind === 'png') return 'image/png';
  if (kind === 'gif') return 'image/gif';
  return 'image/jpeg';
}

export function soundContentType(kind: SoundKind): string {
  return kind === 'ogg' ? 'audio/ogg' : 'audio/mpeg';
}

export function tooLargeMessage(label: string, maxBytes: number): string {
  return `That file is too large. ${label} must be ${Math.round(maxBytes / 1024)} KiB or smaller.`;
}

export async function downloadPublicFile(rawUrl: string, maxBytes: number, label: string): Promise<Buffer> {
  let current = parsePublicHttpsUrl(rawUrl);
  for (let hop = 0; hop < 3; hop += 1) {
    await assertResolvedPublic(current.hostname);
    let response: Response;
    try {
      response = await fetch(current, { redirect: 'manual', signal: AbortSignal.timeout(20_000) });
    } catch (error) {
      if (error instanceof Error && error.name === 'TimeoutError') {
        throw new UserFacingError('The download timed out. Try a smaller file or a different link.');
      }
      if (error instanceof Error && error.name === 'AbortError') {
        throw new UserFacingError('The download timed out. Try a smaller file or a different link.');
      }
      throw new UserFacingError('I could not download that file.');
    }
    if (response.status >= 300 && response.status < 400) {
      const location = response.headers.get('location');
      if (!location) throw new UserFacingError('The file link redirected without a destination.');
      current = parsePublicHttpsUrl(new URL(location, current).href);
      continue;
    }
    if (!response.ok) {
      throw new UserFacingError(`I could not download that file (HTTP ${response.status}).`);
    }
    const advertised = Number(response.headers.get('content-length') ?? '0');
    if (Number.isFinite(advertised) && advertised > maxBytes) {
      throw new UserFacingError(tooLargeMessage(label, maxBytes));
    }
    const reader = response.body?.getReader();
    if (!reader) throw new UserFacingError('I could not read that file.');
    const chunks: Uint8Array[] = [];
    let total = 0;
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      if (!value) continue;
      total += value.byteLength;
      if (total > maxBytes) {
        await reader.cancel().catch(() => undefined);
        throw new UserFacingError(tooLargeMessage(label, maxBytes));
      }
      chunks.push(value);
    }
    if (total < 8) throw new UserFacingError('That file looks empty or incomplete.');
    return Buffer.concat(chunks);
  }
  throw new UserFacingError('That file link redirected too many times.');
}

export function prepareEmoji(data: Buffer): { dataUri: string; animated: boolean } {
  if (data.length > EMOJI_MAX_BYTES) {
    throw new UserFacingError(tooLargeMessage('Custom emojis', EMOJI_MAX_BYTES));
  }
  const kind = sniffEmoji(data);
  if (!kind) {
    throw new UserFacingError('Custom emojis must be a PNG, JPEG, or GIF file.');
  }
  return {
    dataUri: `data:${emojiContentType(kind)};base64,${data.toString('base64')}`,
    animated: kind === 'gif',
  };
}

export function prepareSticker(data: Buffer): { data: Buffer; filename: string } {
  if (data.length > STICKER_MAX_BYTES) {
    throw new UserFacingError(tooLargeMessage('Custom stickers', STICKER_MAX_BYTES));
  }
  const kind = sniffSticker(data);
  if (!kind) {
    throw new UserFacingError('Custom stickers must be a PNG, APNG, GIF, or Lottie JSON file.');
  }
  if (kind === 'json') {
    try {
      const parsed: unknown = JSON.parse(data.toString('utf8'));
      if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
        throw new Error('not an object');
      }
    } catch (error) {
      if (error instanceof UserFacingError) throw error;
      throw new UserFacingError('That Lottie sticker is not valid JSON.');
    }
  }
  const extension = kind === 'gif' ? 'gif' : kind === 'json' ? 'json' : 'png';
  return { data, filename: `sticker.${extension}` };
}

export function prepareSound(data: Buffer): { data: Buffer; contentType: string } {
  if (data.length > SOUND_MAX_BYTES) {
    throw new UserFacingError(
      'That file is too large. Soundboard sounds must be 512 KiB or smaller and 5.2 seconds or shorter.',
    );
  }
  const kind = sniffSound(data);
  if (!kind) {
    throw new UserFacingError('Soundboard sounds must be an MP3 or Ogg file, 5.2 seconds or shorter.');
  }
  return { data, contentType: soundContentType(kind) };
}
