import { PageableModel, SearchResultPayloadModel, UnsubscribedEmailModel } from '@grabbill/lib';

export interface UnsubscribedEmailStateModel {
  email?: string;
  startDate?: Date;
  endDate?: Date;
  unsubscribedEmailPageable: PageableModel;
  unsubscribedEmailSearchResult: SearchResultPayloadModel<UnsubscribedEmailModel>;
}
