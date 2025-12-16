import { Observable } from 'rxjs';
import { ApiResponseModel, HostedPaymentUiModel, StripeSessionType, UserPlanUpdateRequestModel } from '@grabbill/lib';

export abstract class StripeSessionApi {
  abstract createSetupSession(
    type: StripeSessionType,
    request?: UserPlanUpdateRequestModel
  ): Observable<ApiResponseModel<HostedPaymentUiModel>>;
}
