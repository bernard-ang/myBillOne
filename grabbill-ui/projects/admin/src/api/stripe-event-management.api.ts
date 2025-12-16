import { Observable } from 'rxjs';
import {
  ApiResponseModel,
  PageableModel,
  SearchResultPayloadModel,
  StripeEventBasicModel,
  StripeEventModel,
} from '@grabbill/lib';

export abstract class StripeEventManagementApi {
  abstract getEvents(
    pageable: PageableModel,
    accountName?: string,
    type?: string,
    refId?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<StripeEventBasicModel>>>;

  abstract getEvent(id: number): Observable<ApiResponseModel<StripeEventModel>>;
}
