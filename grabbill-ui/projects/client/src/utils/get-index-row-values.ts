import { BaseFieldModel, BaseIndexFieldModel, BaseTypeDataModel, BaseTypeModel, DataType } from '@grabbill/lib';

export const getIndexFieldValue = (field: BaseFieldModel, row: BaseTypeDataModel, toLocale = true) => {
  switch (field.dataType) {
    case DataType.NUMBER:
      return (row as any)[`number${field.seqOrder}`];
    case DataType.DATE:
      const dateValue = (row as any)[`date${field.seqOrder}`];
      if (toLocale) {
        return dateValue ? new Date(dateValue).toLocaleDateString() : '';
      } else {
        return dateValue ? new Date(dateValue) : undefined;
      }
    default:
      return (row as any)[`text${field.seqOrder}`];
  }
};

export const getDataValues = (row: BaseTypeDataModel, fields: BaseFieldModel[]) => {
  return fields.map((field) => {
    return getIndexFieldValue(field, row);
  });
};

export const getIndexRowValues = (row: BaseTypeDataModel, type: BaseTypeModel) => {
  return getIndexFieldsRowValues(row, type.indexFields);
};

export const getIndexFieldsRowValues = (row: BaseTypeDataModel, indexFields: BaseIndexFieldModel[]) => {
  return indexFields
    .filter((field) => field.applicable)
    .map((field) => {
      return getIndexFieldValue(field, row);
    });
};

export const getIndexFields = (row: BaseTypeDataModel, indexFields: BaseIndexFieldModel[]) => {
  return indexFields
    .filter((field) => field.applicable)
    .map((field) => {
      return {
        label: field.label,
        value: getIndexFieldValue(field, row)
      };
    });
};
