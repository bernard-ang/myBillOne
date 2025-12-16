import { BaseIndexFieldModel, BaseTypeModel, ContactFieldModel, DataType } from "@grabbill/lib";

export const codeIndexField: BaseIndexFieldModel = {
  id: 0, seqOrder: 0, label: 'Code', required: true, applicable: true, dataType: DataType.TEXT, header: 'code', referenced: true
}
