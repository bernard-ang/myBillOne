import { Injectable } from '@angular/core';
import { Action, State, StateContext } from '@ngxs/store';
import { EmailUnsubscriptionApi } from '../../api/email-unsubscription.api';
import { UnsubscribeEmail } from './unsubscribe-email.state-actions';

@State<{}>({
  name: 'unsubscribe_email',
  defaults: {},
})
@Injectable()
export class UnsubscribeEmailState {
  constructor(private emailUnsubscriptionApi: EmailUnsubscriptionApi) {}

  @Action(UnsubscribeEmail)
  queryUsers(context: StateContext<{}>, { linkId, reason }: UnsubscribeEmail) {
    return this.emailUnsubscriptionApi.unsubscribeEmail(linkId, { reason });
  }
}
