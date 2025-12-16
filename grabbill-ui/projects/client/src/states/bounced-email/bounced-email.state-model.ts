import { BouncedEmailModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface BouncedEmailStateModel {
  email?: string;
  startDate?: Date;
  endDate?: Date;
  bouncedEmailPageable: PageableModel;
  bouncedEmailSearchResult: SearchResultPayloadModel<BouncedEmailModel>;
}
