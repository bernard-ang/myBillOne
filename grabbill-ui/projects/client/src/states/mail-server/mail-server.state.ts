import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { ApiResponseModel, MailServerModel } from '@grabbill/lib';
import { MailServerStateModel } from './mail-server.state-model';
import { MailServerApi } from '../../api/mail-server.api';
import { GetMailServer, UpdateMailServer } from './mail-server.state-actions';

@State<MailServerStateModel>({
  name: 'mail_server',
  defaults: {},
})
@Injectable()
export class MailServerState {
  constructor(private mailServerApi: MailServerApi) {}

  @Selector()
  static mailServer(state: MailServerStateModel) {
    return state.mailServer;
  }

  @Action(GetMailServer)
  getMailServer(context: StateContext<MailServerStateModel>) {
    return this.mailServerApi.getMailServer().pipe(
      tap((response: ApiResponseModel<MailServerModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.mailServer = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateMailServer)
  updateMailServer(context: StateContext<MailServerStateModel>, { request }: UpdateMailServer) {
    return this.mailServerApi.updateMailServer(request).pipe(
      tap((response: ApiResponseModel<MailServerModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.mailServer = response.data;
          })
        );
      })
    );
  }
}
