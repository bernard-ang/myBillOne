import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseModel, MailServerModel, MailServerUpdateRequestModel } from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { MailServerApi } from '../api/mail-server.api';

@Injectable({
  providedIn: 'root',
})
export class MailServerService implements MailServerApi {
  readonly baseRoute = `mail-server`;

  constructor(private http: ApiHttpService) {}

  getMailServer(): Observable<ApiResponseModel<MailServerModel>> {
    return this.http.get<ApiResponseModel<MailServerModel>>(this.baseRoute);
  }

  updateMailServer(request: MailServerUpdateRequestModel): Observable<ApiResponseModel<MailServerModel>> {
    return this.http.post<ApiResponseModel<MailServerModel>>(this.baseRoute, request);
  }
}
