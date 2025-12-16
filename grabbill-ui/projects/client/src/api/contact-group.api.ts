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

export abstract class ContactGroupApi {
  abstract getContactGroups(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<ContactGroupBasicModel>>>;

  abstract newContactGroup(request: ContactGroupRequestModel): Observable<ApiResponseModel<ContactGroupBasicModel>>;

  abstract updateContactGroup(
    id: number,
    request: ContactGroupRequestModel
  ): Observable<ApiResponseModel<ContactGroupBasicModel>>;

  abstract deleteContactGroup(id: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract updateContactGroupContact(
    id: number,
    request: ContactGroupBindContactsRequestModel
  ): Observable<ApiResponseModel<ContactGroupBindContactsPayloadModel>>;
}
