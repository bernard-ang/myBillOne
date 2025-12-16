import { BaseIndexFieldModel } from '../data';

export interface DigitalFilingTypeRequestModel {
  name: string;
  code: string;
  autoPurge: boolean;
  autoPurgeByDays: number;
  csvSeparator: string;
  indexFields: BaseIndexFieldModel[];
}
