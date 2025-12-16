import { DiscountOccurrence } from '../../data';

export interface PromoCodeUpdateRequestModel {
  name: string;
  start?: Date;
  end?: Date;
  discountOccurrence: DiscountOccurrence;
  discountOccurrenceCount: number;
  remarks: string;
}
