import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiResponseModel,
  SmsCreditsPlanOptionsPayloadModel,
  SmsCreditTopupRequestModel,
  SmsRemainingCreditPayloadModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { SmsCreditApi } from '../api/sms-credit.api';

@Injectable({
  providedIn: 'root',
})
export class SmsCreditService implements SmsCreditApi {
  readonly baseRoute = `sms-credits`;

  constructor(private http: ApiHttpService) {}

  getCreditsPlanOptions(): Observable<ApiResponseModel<SmsCreditsPlanOptionsPayloadModel>> {
    return this.http.get<ApiResponseModel<SmsCreditsPlanOptionsPayloadModel>>(`${this.baseRoute}/plan-options`);
  }

  topup(request: SmsCreditTopupRequestModel): Observable<ApiResponseModel<SmsRemainingCreditPayloadModel>> {
    return this.http.post<ApiResponseModel<SmsRemainingCreditPayloadModel>>(`${this.baseRoute}/topup`, request);
  }
}
