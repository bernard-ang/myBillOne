export const getUpdatedMjml = (mjml: string): string => {
  const mergeFieldMatch = mjml.matchAll(/<(mj-\w+)([^<]*?)\s*\/>/g);

  for (const match of mergeFieldMatch) {
    const closeTagMjElement = match[0].replace('/>', `></${match[1]}>`);
    mjml = mjml.replace(match[0], closeTagMjElement);
  }

  return mjml;
};
