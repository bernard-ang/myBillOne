import { ContactRequestModel } from "./contact-request.model";

export enum DuplicateOption {
  SKIP_DUPLICATE = 'SKIP_DUPLICATE',
  UPDATE_DUPLICATE = 'UPDATE_DUPLICATE'
}

export interface ContactsRequestModel {
  contacts: ContactRequestModel[];

  duplicateOption: DuplicateOption;
}
