import { MailServerUpdateRequestModel } from '@grabbill/lib';

export class GetMailServer {
  static readonly type = '[Mail Server] GetMailServer';
}

export class UpdateMailServer {
  static readonly type = '[Mail Server] UpdateMailServer';

  constructor(public request: MailServerUpdateRequestModel) {}
}
