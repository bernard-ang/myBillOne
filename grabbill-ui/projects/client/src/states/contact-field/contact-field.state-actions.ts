import { ContactFieldsRequestModel } from "@grabbill/lib";

export class ResetContactFields {
  static readonly type = '[Contract Group] ResetContactFields';
}

export class GetContactFields {
  static readonly type = '[Contract Field] GetContactFields';

  constructor() {}
}

export class UpdateContactFields {
  static readonly type = '[Contract Field] UpdateContactFields';

  constructor(public request: ContactFieldsRequestModel) {}
}
