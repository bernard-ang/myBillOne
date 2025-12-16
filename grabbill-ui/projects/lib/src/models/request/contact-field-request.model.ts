import { DataType } from "../data";

export interface ContactFieldRequestModel {
  id: number;
  name: string;
  label: string;
  required: boolean;
  dataType: DataType;
}
