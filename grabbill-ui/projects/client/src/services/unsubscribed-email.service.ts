import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  PageableModel,
  SearchResultPayloadModel,
  UnsubscribedEmailModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { UnsubscribedEmailApi } from '../api/unsubscribed-email.api';

@Injectable({
  providedIn: 'root',
})
export class UnsubscribedEmailService implements UnsubscribedEmailApi {
  readonly baseRoute = 'unsubscribed-emails';

  constructor(private http: ApiHttpService) {}

  getUnsubscribedEmails(
    pageable: PageableModel,
    email?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<UnsubscribedEmailModel>>> {
    startDate?.setHours(0, 0, 0, 0);
    endDate?.setHours(23, 59, 59, 9999);

    return this.http.query<ApiResponseModel<SearchResultPayloadModel<UnsubscribedEmailModel>>>(
      `${this.baseRoute}`,
      pageable,
      {
        email,
        startDate,
        endDate,
      }
    );
  }

  deleteUnsubscribedEmail(id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}`);
  }
}
