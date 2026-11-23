import assert from 'node:assert/strict';
import test from 'node:test';
import { openerFromTopic, ticketChannelName, ticketTopic } from './tickets';

const userId = '123456789012345678';

test('round-trips the ticket opener id', () => {
  const topic = ticketTopic(userId, 'Need help with billing');
  assert.equal(openerFromTopic(topic), userId);
  assert.equal(openerFromTopic('just a topic'), null);
  assert.equal(openerFromTopic(null), null);
});

test('builds a safe channel name', () => {
  assert.equal(ticketChannelName('Ice E!', userId), 'ticket-ice-e');
  assert.equal(ticketChannelName('!!!', userId), `ticket-${userId}`);
  assert.ok(ticketChannelName('a'.repeat(80), userId).length <= 100);
});
