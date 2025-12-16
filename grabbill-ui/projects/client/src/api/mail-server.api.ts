import { Observable } from 'rxjs';
import { ApiResponseModel, MailServerModel, MailServerUpdateRequestModel } from '@grabbill/lib';

export abstract class MailServerApi {
  abstract getMailServer(): Observable<ApiResponseModel<MailServerModel>>;

  abstract updateMailServer(request: MailServerUpdateRequestModel): Observable<ApiResponseModel<MailServerModel>>;
}
