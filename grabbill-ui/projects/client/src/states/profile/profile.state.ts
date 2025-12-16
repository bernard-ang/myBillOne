import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ActivateTwoFactorAuth,
  ChangePassword,
  GenerateQrcode,
  MarkGuidedStepsViewed,
  UpdateProfile,
  UpdateTwoFactorAuth,
} from './profile.state-actions';
import { ProfileStateModel } from './profile-state.model';
import { Me } from '../auth/auth.state-actions';
import { ProfileApi } from '../../api/profile.api';
import produce from 'immer';

@State<ProfileStateModel>({
  name: 'profile',
  defaults: {},
})
@Injectable()
export class ProfileState {
  constructor(private profileApi: ProfileApi) {}

  @Selector()
  static file(state: ProfileStateModel) {
    return state.file;
  }

  @Action(UpdateProfile)
  updateProfile(context: StateContext<ProfileStateModel>, { request }: UpdateProfile) {
    return this.profileApi.updateProfile(request).pipe(tap(() => context.dispatch(new Me())));
  }

  @Action(ChangePassword)
  changePassword(context: StateContext<ProfileStateModel>, { request }: ChangePassword) {
    return this.profileApi.changePassword(request);
  }

  @Action(MarkGuidedStepsViewed)
  markGuidedStepsViewed(context: StateContext<ProfileStateModel>) {
    return this.profileApi.markGuidedStepsViewed().pipe(tap(() => context.dispatch(new Me())));
  }

  @Action(UpdateTwoFactorAuth)
  updateTwoFactorAuth(context: StateContext<ProfileStateModel>, { request }: UpdateTwoFactorAuth) {
    return this.profileApi.updateMfa(request).pipe(tap(() => context.dispatch(new Me())));
  }

  @Action(ActivateTwoFactorAuth)
  activateTwoFactorAuth(context: StateContext<ProfileStateModel>, { request }: ActivateTwoFactorAuth) {
    return this.profileApi.activateMfa(request).pipe(tap(() => context.dispatch(new Me())));
  }

  @Action(GenerateQrcode)
  generateQrcode(context: StateContext<ProfileStateModel>) {
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
