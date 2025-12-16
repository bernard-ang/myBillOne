import { DiscountOccurrence, DiscountType } from '../../data';

export interface PromoCodeNewRequestModel {
  name: string;

  code: string;

  start?: Date;

  end?: Date;

  discount: number;

  discountType: DiscountType;

  discountOccurrence: DiscountOccurrence;

  discountOccurrenceCount: number;

  active: boolean;

  remarks: string;
}
