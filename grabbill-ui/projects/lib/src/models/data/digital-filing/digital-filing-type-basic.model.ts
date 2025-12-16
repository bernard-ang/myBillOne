import { BaseTypeBasicModel } from '../base-type-basic.model';

export interface DigitalFilingTypeBasicModel extends BaseTypeBasicModel {
  lastUploadBy: string;
  lastUploadDate: Date;
}
