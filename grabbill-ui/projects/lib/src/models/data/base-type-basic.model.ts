import { AuditableModel } from "./common";

export interface BaseTypeBasicModel extends AuditableModel {
  id: number;
  name: string;
  code: string;
  noOfFiles: number;
}
