import { PageableModel, SearchResultPayloadModel, StripeEventBasicModel, StripeEventModel } from '@grabbill/lib';

export interface StripeManagementStateModel {
  accountName: string;
  type: string;
  refId: string;
  startDate?: Date;
  endDate?: Date;

  eventPageable: PageableModel;
  eventSearchResult: SearchResultPayloadModel<StripeEventBasicModel>;
  event?: StripeEventModel;
}
