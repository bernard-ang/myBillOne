import { BaseFieldModel } from "./base-field.model";

export interface BaseIndexFieldModel extends BaseFieldModel {
  header: string;
  applicable: boolean;
}
