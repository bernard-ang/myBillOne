import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  PageableModel,
  SearchResultPayloadModel,
  UnsubscribedEmailModel,
} from '@grabbill/lib';

export abstract class UnsubscribedEmailApi {
  abstract getUnsubscribedEmails(
    pageable: PageableModel,
    email?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<UnsubscribedEmailModel>>>;

  abstract deleteUnsubscribedEmail(id: number): Observable<ApiResponseModel<ApiMessage>>;
}
