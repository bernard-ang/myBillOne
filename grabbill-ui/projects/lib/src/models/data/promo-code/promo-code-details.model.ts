import { DiscountType } from './discount-type';
import { DiscountOccurrence } from './discount-occurrence';

export interface PromoCodeDetailsModel {
  id: number;

  name: string;

  code: string;

  start: Date;

  end: Date;

  discount: number;

  discountType: DiscountType;

  discountOccurrence: DiscountOccurrence;

  discountOccurrenceCount: number;

  active: boolean;

  remarks: string;

  createdTime: Date;

  lastModifiedTime: Date;
}
