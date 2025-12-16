import {
  AccountSubscriptionModel,
  PageableModel,
  PrePlanSwitchInvoicePayloadModel,
  SearchResultPayloadModel,
  UserPlanUpdateRequestModel,
} from '@grabbill/lib';

export interface AccountStateModel {
  message?: string;
  switchPlanRequest?: UserPlanUpdateRequestModel;
  prePlanSwitchInvoice?: PrePlanSwitchInvoicePayloadModel;
  accountSubscriptionPageable: PageableModel;
  accountSubscriptionSearchResult: SearchResultPayloadModel<AccountSubscriptionModel>;
}
