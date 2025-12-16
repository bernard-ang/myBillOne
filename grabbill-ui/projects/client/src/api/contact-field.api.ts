import { Observable } from "rxjs";
import { ApiResponseModel, ContactFieldsPayloadModel, ContactFieldsRequestModel } from "@grabbill/lib";

export abstract class ContactFieldApi {
  abstract getContactFields(): Observable<ApiResponseModel<ContactFieldsPayloadModel>>;

  abstract saveContactFields(request: ContactFieldsRequestModel): Observable<ApiResponseModel<ContactFieldsPayloadModel>>;
}
