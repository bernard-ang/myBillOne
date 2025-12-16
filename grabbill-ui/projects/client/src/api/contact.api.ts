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

export abstract class ContactApi {
  abstract searchContacts(
    pageable: PageableModel,
    email?: string,
    filters?: { [index: string]: any }
  ): Observable<ApiResponseModel<SearchResultPayloadModel<ContactBasicModel>>>;

  abstract getContactDetails(id: number): Observable<ApiResponseModel<ContactModel>>;

  abstract newContact(request: ContactRequestModel): Observable<ApiResponseModel<ContactModel>>;

  abstract updateContactDetails(id: number, request: ContactRequestModel): Observable<ApiResponseModel<ContactModel>>;

  abstract updateContactGroups(
    id: number,
    request: ContactUpdateGroupsRequestModel
  ): Observable<ApiResponseModel<ContactModel>>;

  abstract bulkDeleteContact(request: ContactsBulkDeleteRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract deleteContact(id: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract uploadContacts(request: ContactsRequestModel): Observable<ApiResponseModel<ContactsBasicPayloadModel>>;
}
