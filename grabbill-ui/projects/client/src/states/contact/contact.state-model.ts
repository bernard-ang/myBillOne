import {
  ContactBasicModel,
  ContactModel,
  ContactRequestModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';

export interface ContactStateModel {
  email?: string;
  searchEmail?: string;
  filters: { [index: string]: any };
  contactPageable: PageableModel;
  contactSearchResult: SearchResultPayloadModel<ContactBasicModel>;
  searchContactPageable: PageableModel;
  searchContactSearchResult: SearchResultPayloadModel<ContactBasicModel>;
  searchFilters: { [index: string]: any };
  contact?: ContactModel;

  updatedContacts: ContactBasicModel[];
  skipContacts: ContactRequestModel[];
}
