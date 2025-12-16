import { AuditableModel, BaseIndexFieldModel } from "@grabbill/lib";

export interface BaseTypeModel extends AuditableModel {
  id: number;
  name: string;
  code: string;
  noOfFiles: number;
  csvSeparator: string;
  indexFields: BaseIndexFieldModel[];
}
