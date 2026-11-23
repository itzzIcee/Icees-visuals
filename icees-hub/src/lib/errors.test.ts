import assert from 'node:assert/strict';
import test from 'node:test';
import { explainDiscordFailure, flattenDiscordErrors, permissionMessage } from './errors';

test('maps Nitro slot limits to plain English', () => {
  assert.match(explainDiscordFailure({ code: 30008, action: 'add that emoji' }), /Nitro Server Boost/);
  assert.match(explainDiscordFailure({ code: 30018, action: 'add that emoji' }), /animated emoji/);
  assert.match(explainDiscordFailure({ code: 30039, action: 'add that sticker' }), /sticker/);
  assert.match(explainDiscordFailure({ code: 30045, action: 'add that sound' }), /soundboard/);
});

test('maps file size and permission errors', () => {
  assert.match(explainDiscordFailure({ code: 50045, action: 'add that emoji' }), /256 KiB/);
  assert.match(explainDiscordFailure({ code: 50138, action: 'add that sticker' }), /512 KiB/);
  assert.match(explainDiscordFailure({ code: 50013, action: 'assign the member role' }), /Manage Roles/);
  assert.match(permissionMessage('add that sound'), /Manage Expressions/);
});

test('includes invalid form details', () => {
  const message = explainDiscordFailure({
    code: 50035,
    action: 'add that emoji',
    errors: { name: { _errors: [{ code: 'BASE_TYPE_BAD_LENGTH', message: 'Must be between 2 and 32 in length.' }] } },
  });
  assert.match(message, /Must be between 2 and 32 in length/);
  assert.deepEqual(flattenDiscordErrors({ name: { _errors: [{ message: 'Too short.' }] } }), ['name: Too short.']);
});

test('falls back when Discord only describes the limit in text', () => {
  assert.match(
    explainDiscordFailure({ message: 'Maximum number of emojis reached', action: 'add that emoji' }),
    /custom emoji limit/,
  );
});
