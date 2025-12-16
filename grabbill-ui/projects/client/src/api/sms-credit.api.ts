import { Observable } from 'rxjs';
import {
  ApiResponseModel,
  SmsCreditsPlanOptionsPayloadModel,
  SmsCreditTopupRequestModel,
  SmsRemainingCreditPayloadModel
} from "@grabbill/lib";

export abstract class SmsCreditApi {
  abstract getCreditsPlanOptions(): Observable<ApiResponseModel<SmsCreditsPlanOptionsPayloadModel>>;

  abstract topup(request: SmsCreditTopupRequestModel): Observable<ApiResponseModel<SmsRemainingCreditPayloadModel>>;
}
