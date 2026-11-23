import assert from 'node:assert/strict';
import test from 'node:test';
import { UserFacingError } from './errors';
import {
  assertEmojiName,
  assertSnowflake,
  assertSoundName,
  assertStickerTag,
  volumeFraction,
} from './validate';

test('accepts emoji names and rejects spaces', () => {
  assert.equal(assertEmojiName(' ice_cube '), 'ice_cube');
  assert.throws(() => assertEmojiName('ice cube'), UserFacingError);
});

test('accepts sticker tags as names or emoji', () => {
  assert.equal(assertStickerTag('Wave'), 'wave');
  assert.equal(assertStickerTag('👋'), '👋');
  assert.throws(() => assertStickerTag('hello there'), UserFacingError);
});

test('converts volume percent to the Discord fraction', () => {
  assert.equal(volumeFraction(0), 0);
  assert.equal(volumeFraction(100), 1);
  assert.equal(volumeFraction(40), 0.4);
  assert.throws(() => volumeFraction(1.5), UserFacingError);
});

test('rejects sound names that are too short', () => {
  assert.equal(assertSoundName('hi there'), 'hi there');
  assert.throws(() => assertSoundName('a'), UserFacingError);
});

test('requires a snowflake from autocomplete', () => {
  assert.equal(assertSnowflake('123456789012345678', 'emoji'), '123456789012345678');
  assert.throws(() => assertSnowflake('smile', 'emoji'), UserFacingError);
});
