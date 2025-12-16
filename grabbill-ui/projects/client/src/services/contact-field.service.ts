import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { ApiResponseModel, ContactFieldsPayloadModel, ContactFieldsRequestModel } from "@grabbill/lib";
import { ApiHttpService } from "./api-http.service";
import { ContactFieldApi } from "../api/contact-field.api";

@Injectable({
  providedIn: 'root',
})
export class ContactFieldService implements ContactFieldApi {
  readonly baseRoute = `contact-fields`;

  constructor(private http: ApiHttpService) {}

  getContactFields (): Observable<ApiResponseModel<ContactFieldsPayloadModel>> {
    return this.http.get<ApiResponseModel<ContactFieldsPayloadModel>>((this.baseRoute));
  }

  saveContactFields (request: ContactFieldsRequestModel): Observable<ApiResponseModel<ContactFieldsPayloadModel>> {
    return this.http.post<ApiResponseModel<ContactFieldsPayloadModel>>(this.baseRoute, request);
  }
}
