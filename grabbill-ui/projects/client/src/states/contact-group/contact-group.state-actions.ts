import { ContactGroupBindContactsRequestModel, ContactGroupRequestModel, PageableModel } from '@grabbill/lib';

export class ResetContactGroups {
  static readonly type = '[Contact Group] ResetContactGroups';
}

export class QueryContactGroups {
  static readonly type = '[Contact Group] QueryContactGroups';

  constructor(public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreContactGroups {
  static readonly type = '[Contact Group] LoadMoreContactGroups';
}

export class NewContactGroup {
  static readonly type = '[Contact Group] NewContactGroup';

  constructor(public request: ContactGroupRequestModel) {}
}

export class UpdateContactGroup {
  static readonly type = '[Contact Group] UpdateContactGroup';

  constructor(public id: number, public request: ContactGroupRequestModel) {}
}

export class DeleteContactGroup {
  static readonly type = '[Contact Group] DeleteContactGroup';

  constructor(public id: number) {}
}

export class UploadContactGroupContact {
  static readonly type = '[Contact Group] UploadContactGroupContact';

  constructor(public id: number, public request: ContactGroupBindContactsRequestModel) {}
}
