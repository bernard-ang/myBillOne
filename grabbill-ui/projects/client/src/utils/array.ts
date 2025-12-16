export const isArrayDates = (value: any): boolean => {
  // Check if input is an array
  if (!Array.isArray(value)) {
    return false;
  }

  // Check if all elements in the array are Date objects
  for (let i = 0; i < value.length; i++) {
    if (!(value[i] instanceof Date)) {
      return false;
    }
  }

  return true;
};
