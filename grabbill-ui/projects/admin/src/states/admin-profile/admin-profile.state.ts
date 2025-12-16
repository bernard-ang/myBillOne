import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { AdminProfileStateModel } from './admin-profile-state.model';
import produce from 'immer';
import { AdminUserProfileApi } from '../../api/admin-user-profile.api';
import { ActivateTwoFactorAuth, GenerateQrcode, UpdateTwoFactorAuth } from "./admin-profile.state-actions";
import { Me } from '../admin-auth/admin-auth.state-actions';

@State<AdminProfileStateModel>({
  name: 'admin_profile',
  defaults: {},
})
@Injectable()
export class AdminProfileState {
  constructor(private profileApi: AdminUserProfileApi) {}

  @Selector()
  static file(state: AdminProfileStateModel) {
    return state.file;
  }

  @Action(UpdateTwoFactorAuth)
  updateTwoFactorAuth(context: StateContext<AdminProfileStateModel>, { request }: UpdateTwoFactorAuth) {
    return this.profileApi.updateMfa(request).pipe(tap(() => context.dispatch(new Me())));
  }

  @Action(ActivateTwoFactorAuth)
  activateTwoFactorAuth(context: StateContext<AdminProfileStateModel>, { request }: ActivateTwoFactorAuth) {
    return this.profileApi.activateMfa(request).pipe(tap(() => context.dispatch(new Me())));
  }

  @Action(GenerateQrcode)
  generateQrcode(context: StateContext<AdminProfileStateModel>) {
    return this.profileApi.generateMfaQrcode().pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }
}
