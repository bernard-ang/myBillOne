import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  BouncedEmailModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';

export abstract class BouncedEmailApi {
  abstract getBouncedEmails(
    pageable: PageableModel,
    email?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<BouncedEmailModel>>>;

  abstract deleteBouncedEmail(id: number): Observable<ApiResponseModel<ApiMessage>>;
}
