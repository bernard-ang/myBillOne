import { Observable } from "rxjs";
import { ApiResponseModel, PageableModel, SearchResultPayloadModel, WhatsappEventBasicModel } from "@grabbill/lib";

export abstract class WhatsAppEventApi {
  abstract getEvents(
    pageable: PageableModel,
    messageType?: string,
    mobileNo?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<WhatsappEventBasicModel>>>;
}
