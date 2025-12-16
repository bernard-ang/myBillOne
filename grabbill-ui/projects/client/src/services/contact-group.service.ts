import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  ContactGroupBasicModel,
  ContactGroupBindContactsPayloadModel,
  ContactGroupBindContactsRequestModel,
  ContactGroupRequestModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { ContactGroupApi } from '../api/contact-group.api';

@Injectable({
  providedIn: 'root',
})
export class ContactGroupService implements ContactGroupApi {
  readonly baseRoute = `contact-groups`;

  constructor(private http: ApiHttpService) {}

  deleteContactGroup(id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}`);
  }

  getContactGroups(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<ContactGroupBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<ContactGroupBasicModel>>>(
      `${this.baseRoute}`,
      pageable,
      {
        name,
      }
    );
  }

  newContactGroup(request: ContactGroupRequestModel): Observable<ApiResponseModel<ContactGroupBasicModel>> {
    return this.http.post<ApiResponseModel<ContactGroupBasicModel>>(`${this.baseRoute}`, request);
  }

  updateContactGroup(
    id: number,
    request: ContactGroupRequestModel
  ): Observable<ApiResponseModel<ContactGroupBasicModel>> {
    return this.http.put<ApiResponseModel<ContactGroupBasicModel>>(`${this.baseRoute}/${id}`, request);
  }

  updateContactGroupContact(
    id: number,
    request: ContactGroupBindContactsRequestModel
  ): Observable<ApiResponseModel<ContactGroupBindContactsPayloadModel>> {
    return this.http.post<ApiResponseModel<ContactGroupBindContactsPayloadModel>>(
      `${this.baseRoute}/${id}/bind-contacts`,
      request
    );
  }
}
