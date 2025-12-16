export const getUpdatedMjmlHtml = (html: string): string => {
  const parser = new DOMParser();
  const htmlDoc = parser.parseFromString(html, 'text/html');

  // fixed image auto size
  const imageTags = htmlDoc.getElementsByTagName('img');
  for (let i = 0; i < imageTags.length; i++) {
    const imageTag = imageTags[i];
    imageTag.parentElement!.removeAttribute('style');
  }

  // fixed background image td size
  const backgroundImageTdTags = htmlDoc.getElementsByTagName('td');
  for (let i = 0; i < backgroundImageTdTags.length; i++) {
    const backgroundImageTdTag = backgroundImageTdTags[i];
    if (backgroundImageTdTag.hasAttribute('background')) {
      backgroundImageTdTag.style.boxSizing = 'border-box';
    }
  }

  return `<!doctype html><html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">${htmlDoc.documentElement.innerHTML}</html>`;
};
