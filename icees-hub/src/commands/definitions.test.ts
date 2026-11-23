import assert from 'node:assert/strict';
import test from 'node:test';
import { ApplicationCommandOptionType } from 'discord.js';
import { commandJSON } from './definitions';

test('registers the hub commands and expression subcommands', () => {
  const commands = commandJSON();
  assert.deepEqual(
    commands.map((command) => command.name).sort(),
    ['emoji', 'rules', 'soundboard', 'sticker', 'ticket'],
  );
  for (const name of ['emoji', 'sticker', 'soundboard']) {
    const command = commands.find((entry) => entry.name === name);
    const subcommands = (command?.options ?? [])
      .filter((option) => option.type === ApplicationCommandOptionType.Subcommand)
      .map((option) => option.name)
      .sort();
    assert.deepEqual(subcommands, ['add', 'list', 'remove']);
  }
  const emojiAdd = commands
    .find((command) => command.name === 'emoji')
    ?.options?.find((option) => option.name === 'add');
  const optionNames = (emojiAdd && 'options' in emojiAdd ? emojiAdd.options : [])?.map((option) => option.name).sort();
  assert.deepEqual(optionNames, ['image', 'name', 'url']);
});
