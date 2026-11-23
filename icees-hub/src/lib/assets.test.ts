import assert from 'node:assert/strict';
import test from 'node:test';
import { isPrivateAddress, parsePublicHttpsUrl, prepareEmoji, sniffSound, sniffSticker } from './assets';
import { UserFacingError } from './errors';

test('blocks private and non-https URLs', () => {
  assert.equal(isPrivateAddress('127.0.0.1'), true);
  assert.equal(isPrivateAddress('10.1.2.3'), true);
  assert.equal(isPrivateAddress('192.168.0.8'), true);
  assert.equal(isPrivateAddress('8.8.8.8'), false);
  assert.equal(parsePublicHttpsUrl('https://cdn.example.com/emoji.png').hostname, 'cdn.example.com');
  assert.throws(() => parsePublicHttpsUrl('http://cdn.example.com/emoji.png'), UserFacingError);
  assert.throws(() => parsePublicHttpsUrl('https://127.0.0.1/emoji.png'), UserFacingError);
  assert.throws(() => parsePublicHttpsUrl('https://localhost/emoji.png'), UserFacingError);
});

test('detects emoji, sticker, and sound files', () => {
  const png = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0, 0, 0, 0]);
  const prepared = prepareEmoji(png);
  assert.equal(prepared.animated, false);
  assert.match(prepared.dataUri, /^data:image\/png;base64,/);
  assert.equal(sniffSticker(Buffer.from('{"v":"5.5.7"}')), 'json');
  assert.equal(sniffSound(Buffer.from('OggSxxxx')), 'ogg');
  assert.equal(sniffSound(Buffer.from('ID3xxxx')), 'mp3');
  assert.throws(() => prepareEmoji(Buffer.from('not an image!!')), UserFacingError);
});
