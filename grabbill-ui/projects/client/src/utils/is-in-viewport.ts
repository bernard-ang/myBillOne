export const isElementTopVisible = (element: HTMLElement) => {
  const rect = element.getBoundingClientRect();
  if (rect.top < 0) {
    return false;
  }

  return (
    rect.top <= (window.innerHeight || document.documentElement.clientHeight) &&
    rect.left >= 0 &&
    rect.right <= (window.innerWidth || document.documentElement.clientWidth)
  );
};

export const isElementBottomVisible = (element: HTMLElement) => {
  const rect = element.getBoundingClientRect();
  if (rect.bottom < 0) {
    return false;
  }

  return (
    rect.top >= 0 &&
    rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
    rect.left >= 0 &&
    rect.right <= (window.innerWidth || document.documentElement.clientWidth)
  );
};
