import {
  ContactRequestModel,
  ContactsBulkDeleteRequestModel,
  ContactsRequestModel,
  ContactUpdateGroupsRequestModel,
  PageableModel,
} from '@grabbill/lib';

export class QueryContacts {
  static readonly type = '[Contact] QueryContacts';

  constructor(public pageable?: PageableModel, public email?: string, public filters?: { [index: string]: any }) {}
}

export class QuerySearchContacts {
  static readonly type = '[Contact] QuerySearchContacts';

  constructor(public pageable?: PageableModel, public email?: string, public filters?: { [index: string]: any }) {}
}

export class ResetContacts {
  static readonly type = '[Contact] ResetContacts';
}

export class ResetSearchContacts {
  static readonly type = '[Contact] ResetSearchContacts';
}

export class GetContact {
  static readonly type = '[Contact] GetContact';

  constructor(public id: number) {}
}

export class ResetContact {
  static readonly type = '[Contact] ResetContact';
}

export class NewContact {
  static readonly type = '[Contact] NewContact';

  constructor(public request: ContactRequestModel) {}
}

export class UpdateContact {
  static readonly type = '[Contact] UpdateContact';

  constructor(public id: number, public request: ContactRequestModel) {}
}

export class DeleteContact {
  static readonly type = '[Contact] DeleteContact';

  constructor(public id: number) {}
}

export class UpdateContactGroups {
  static readonly type = '[Contact] UpdateContactGroups';

  constructor(public id: number, public request: ContactUpdateGroupsRequestModel) {}
}

export class UploadContacts {
  static readonly type = '[Contact] UploadContacts';
  constructor(public request: ContactsRequestModel) {}
}

export class BulkDeleteContacts {
  static readonly type = '[Contact] BulkDeleteContacts';

  constructor(public request: ContactsBulkDeleteRequestModel) {}
}
