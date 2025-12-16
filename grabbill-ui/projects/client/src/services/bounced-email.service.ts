import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  BouncedEmailModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { BouncedEmailApi } from '../api/bounced-email.api';

@Injectable({
  providedIn: 'root',
})
export class BouncedEmailService implements BouncedEmailApi {
  readonly baseRoute = 'bounced-emails';

  constructor(private http: ApiHttpService) {}

  getBouncedEmails(
    pageable: PageableModel,
    email?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<BouncedEmailModel>>> {
    startDate?.setHours(0, 0, 0, 0);
    endDate?.setHours(23, 59, 59, 9999);

    return this.http.query<ApiResponseModel<SearchResultPayloadModel<BouncedEmailModel>>>(
      `${this.baseRoute}`,
      pageable,
      {
        email,
        startDate,
        endDate,
      }
    );
  }

  deleteBouncedEmail(id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}`);
  }
}
