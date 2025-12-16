import { DataType } from "./data-type";

export interface BaseFieldModel {
  id: number;
  seqOrder: number;
  label: string;
  required: boolean;
  dataType: DataType;
  referenced: boolean;
}
