import { BaseIndexRowModel, BaseTypeModel } from '@grabbill/lib';
import { getIndexFieldValue } from './get-index-row-values';

export const getIndexRowObjects = (row: BaseIndexRowModel, type: BaseTypeModel) => {
  return type.indexFields
    .filter((field) => field.applicable)
    .map((field) => {
      return { label: field.label, value: getIndexFieldValue(field, row) };
    });
};
