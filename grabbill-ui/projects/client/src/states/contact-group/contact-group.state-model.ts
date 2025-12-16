import { ContactGroupBasicModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface ContactGroupStateModel {
  name?: string;
  contactGroupPageable: PageableModel;
  contactGroupSearchResult: SearchResultPayloadModel<ContactGroupBasicModel>;
  isGroupInitialize: boolean;

  emailsUpdated: string[];
  invalidEmails: string[];
}
