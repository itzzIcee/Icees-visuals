import assert from 'node:assert/strict';
import test from 'node:test';
import { UserFacingError } from './errors';
import { loadRules, parseRules, rulesDescription } from './rules';

test('ships sensible default rules', () => {
  const rules = loadRules();
  assert.ok(rules.rules.length >= 5);
  assert.ok(rules.title.length > 0);
  assert.ok(rulesDescription(rules).length <= 4096);
  assert.match(rulesDescription(rules), /\*\*1\.\*\*/);
});

test('rejects an empty rules list', () => {
  assert.throws(() => parseRules({ title: 'Rules', intro: 'Read these.', rules: [] }), UserFacingError);
});
