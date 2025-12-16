import { BaseTypeModel, ContactFieldModel } from '@grabbill/lib';

export const getIndexFieldLabels = (type: BaseTypeModel) => {
  return type.indexFields.filter((field) => field.applicable).map((field) => field.label);
};

export const getContactFieldLabels = (fields: ContactFieldModel[]) => {
  return fields.map((field) => field.label);
};
