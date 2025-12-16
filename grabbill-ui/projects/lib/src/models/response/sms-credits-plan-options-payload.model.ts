import { CreditType } from '../data/credit-type';

export interface SmsCreditsPlanOptionPayloadModel {
  id: number;
  type: CreditType;
  name: string;
  description: string;
  quantity: number;
  price: number;
}

export interface SmsCreditsPlanOptionsPayloadModel {
  options: SmsCreditsPlanOptionPayloadModel[];
}
