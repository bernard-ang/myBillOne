import {
  PageableModel,
  PromoCodeNewRequestModel,
  PromoCodeStatusUpdateRequestModel,
  PromoCodeUpdateRequestModel,
} from '@grabbill/lib';

export class ResetPromoCodes {
  static readonly type = '[Promo Code] ResetPromoCodes';
}

export class QueryPromoCodes {
  static readonly type = '[Promo Code] QueryPromoCodes';

  constructor(public pageable?: PageableModel, public code?: string) {}
}

export class NewPromoCode {
  static readonly type = '[Promo Code] NewPromoCode';

  constructor(public request: PromoCodeNewRequestModel) {}
}

export class UpdatePromoCode {
  static readonly type = '[Promo Code] UpdatePromoCode';

  constructor(public id: number, public request: PromoCodeUpdateRequestModel) {}
}

export class UpdatePromoCodeStatus {
  static readonly type = '[Promo Code] UpdatePromoCodeStatus';

  constructor(public id: number, public request: PromoCodeStatusUpdateRequestModel) {}
}

export class DeletePromoCode {
  static readonly type = '[Promo Code] DeletePromoCode';

  constructor(public id: number) {}
}
