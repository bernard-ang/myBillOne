import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseModel, HostedPaymentUiModel, StripeSessionType, UserPlanUpdateRequestModel } from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { StripeSessionApi } from '../api/stripe-session.api';

@Injectable({
  providedIn: 'root',
})
export class StripeSessionService implements StripeSessionApi {
  readonly baseRoute = `stripe/sessions`;

  constructor(private http: ApiHttpService) {}

  createSetupSession(
    type: StripeSessionType,
    request?: UserPlanUpdateRequestModel
  ): Observable<ApiResponseModel<HostedPaymentUiModel>> {
    return this.http.post<ApiResponseModel<HostedPaymentUiModel>>(
      `${this.baseRoute}?type=${type}`,
      request ? request : {}
    );
  }
}
