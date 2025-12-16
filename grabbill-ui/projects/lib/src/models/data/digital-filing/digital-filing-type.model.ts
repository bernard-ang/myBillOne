import { BaseTypeModel } from '../base-type.model';

export interface DigitalFilingTypeModel extends BaseTypeModel {
  autoPurge: boolean;
  autoPurgeByDays: number;
}
