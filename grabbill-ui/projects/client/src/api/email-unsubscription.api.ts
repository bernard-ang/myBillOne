import { Observable } from 'rxjs';
import { ApiResponseModel, UnsubscribedEmailModel, UnsubscribedEmailRequestModel } from '@grabbill/lib';

export abstract class EmailUnsubscriptionApi {
  abstract unsubscribeEmail(
    linkId: string,
    request: UnsubscribedEmailRequestModel
  ): Observable<ApiResponseModel<UnsubscribedEmailModel>>;
}
