import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseModel, UnsubscribedEmailModel, UnsubscribedEmailRequestModel } from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { EmailUnsubscriptionApi } from '../api/email-unsubscription.api';

@Injectable({
  providedIn: 'root',
})
export class EmailUnsubscriptionService implements EmailUnsubscriptionApi {
  readonly baseRoute = 'unsubscribe';

  constructor(private http: ApiHttpService) {}

  unsubscribeEmail(
    linkId: string,
    request: UnsubscribedEmailRequestModel
  ): Observable<ApiResponseModel<UnsubscribedEmailModel>> {
    return this.http.post<ApiResponseModel<UnsubscribedEmailModel>>(`${this.baseRoute}?linkId=${linkId}`, request);
  }
}
