import { BaseTypeDataModel } from "../base-type-data.model";

export interface ContactBasicModel extends BaseTypeDataModel {
  id: number;
  email: string;
  mobileNo: string;

  groups: string[];
}
