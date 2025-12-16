export const isAsciiTextOnly = (str: string) => {
  return /^[\x00-\x7F]*$/.test(str);
};

export const removePlaceholders = (str: string): string => {
  const pattern = /\{\{.*?}}/g;
  return str.replace(pattern, '');
}

export const countBytes = (value: string) => {
  if (isAsciiTextOnly(value)) {
    return new TextEncoder().encode(value).length;
  } else {
    let byteCount = 0;
    for (let i = 0; i < value.length; i++) {
      const charCode = value.charCodeAt(i);
      if (charCode < 0x10000) {
        byteCount += 2;
      } else {
        byteCount += 4;
      }
    }
    return byteCount;
  }
};
