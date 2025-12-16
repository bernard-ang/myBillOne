import { BaseFileModel } from "./base-file.model";
import { BaseTypeDataModel } from "./base-type-data.model";

export interface BaseIndexRowModel extends BaseTypeDataModel {
  file?: BaseFileModel;

  activityId: number;
  activityName: string;

  id: number;
  seqOrder: number;
}
