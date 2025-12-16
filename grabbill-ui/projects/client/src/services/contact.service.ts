import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  ContactBasicModel,
  ContactModel,
  ContactRequestModel,
  ContactsBasicPayloadModel,
  ContactsBulkDeleteRequestModel,
  ContactsRequestModel,
  ContactUpdateGroupsRequestModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { ContactApi } from '../api/contact.api';

@Injectable({
  providedIn: 'root',
})
export class ContactService implements ContactApi {
  readonly baseRoute = `contacts`;

  constructor(private http: ApiHttpService) {}

  deleteContact(id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}`);
  }

  getContactDetails(id: number): Observable<ApiResponseModel<ContactModel>> {
    return this.http.get<ApiResponseModel<ContactModel>>(`${this.baseRoute}/${id}`);
  }

  newContact(request: ContactRequestModel): Observable<ApiResponseModel<ContactModel>> {
    return this.http.post<ApiResponseModel<ContactModel>>(`${this.baseRoute}`, request);
  }

  searchContacts(
    pageable: PageableModel,
    email?: string,
    filters?: { [p: string]: any }
  ): Observable<ApiResponseModel<SearchResultPayloadModel<ContactBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<ContactBasicModel>>>(
      this.baseRoute,
      pageable,
      { email: email, ...filters },
      { localDate: true }
    );
  }

  updateContactDetails(id: number, request: ContactRequestModel): Observable<ApiResponseModel<ContactModel>> {
    return this.http.put<ApiResponseModel<ContactModel>>(`${this.baseRoute}/${id}`, request);
  }

  updateContactGroups(
    id: number,
    request: ContactUpdateGroupsRequestModel
  ): Observable<ApiResponseModel<ContactModel>> {
    return this.http.put<ApiResponseModel<ContactModel>>(`${this.baseRoute}/${id}/groups`, request);
  }

  uploadContacts(request: ContactsRequestModel): Observable<ApiResponseModel<ContactsBasicPayloadModel>> {
    return this.http.post<ApiResponseModel<ContactsBasicPayloadModel>>(`${this.baseRoute}/bulk-upload`, request);
  }

  bulkDeleteContact(request: ContactsBulkDeleteRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}`, request);
  }
}
