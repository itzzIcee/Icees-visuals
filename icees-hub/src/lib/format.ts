export function fitLines(lines: string[], maxChars = 3900): { text: string; hidden: number } {
  const kept: string[] = [];
  let size = 0;
  for (const line of lines) {
    const next = size + line.length + (kept.length > 0 ? 1 : 0);
    if (kept.length > 0 && next > maxChars) break;
    kept.push(line);
    size = next;
  }
  return { text: kept.join('\n'), hidden: lines.length - kept.length };
}

export function auditReason(tag: string, action: string): string {
  return `${action} by ${tag} via icee's Hub`.slice(0, 512);
}

export function uploaderLabel(userId: string | undefined, botId: string, knownTag?: string): string {
  if (!userId) return 'uploader unknown';
  if (userId === botId) return 'added by this bot';
  return knownTag ? `added by ${knownTag}` : 'added by another member';
}

export function countLabel(count: number, singular: string, plural: string): string {
  return `${count} ${count === 1 ? singular : plural}`;
}
