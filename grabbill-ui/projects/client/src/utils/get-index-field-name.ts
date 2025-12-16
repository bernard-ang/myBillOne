import { BaseFieldModel, DataType } from "@grabbill/lib";

export const getIndexFieldName = (field: BaseFieldModel) => {
  if(field.seqOrder < 0) {
    if((field as any)['name']) {
      return (field as any)['name']
    } else if((field as any)['header']) {
      return (field as any)['header']
    }
  }
  switch (field.dataType) {
    case DataType.NUMBER:
      return `number${field.seqOrder}`;
    case DataType.DATE:
      return `date${field.seqOrder}`;
    default:
      return `text${field.seqOrder}`;
  }
};
