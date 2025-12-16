export const csvSeparators = [
  { value: ',', label: 'Excel or CSV Comma ( , )' },
  { value: '|', label: 'Excel or CSV Pipe ( | )' },
  { value: ';', label: 'Excel or CSV Colon ( ; )' },
];

export const getCsvSeparatorLabel = (csvSeparator: string): string => {
  return csvSeparators.filter((separator) => separator.value === csvSeparator)[0].label;
};
