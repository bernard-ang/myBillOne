import { BaseTypeDataModel } from '../data';

export interface ContactRequestModel extends BaseTypeDataModel {
  email: string;
  mobileNo: string;
  groups: number[];
}
