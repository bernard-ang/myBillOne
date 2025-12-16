export class UnsubscribeEmail {
  static readonly type = '[UnsubscribeEmail] UnsubscribeEmail';

  constructor(public linkId: string, public reason: string) {}
}
