import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { AdminAuthStateModel } from './admin-auth-state.model';
import {
  ForgetPassword, GenerateActivateTwoFactorAuthorizationEmail,
  GenerateOtpEmail,
  Login,
  Logout,
  Me,
  RefreshToken,
  ResetPassword,
  TwoFactorAuthorization
} from "./admin-auth.state-actions";
import { AuthApi } from '../../api/auth.api';
import { ApiResponseModel, UserAuthorityModel } from '@grabbill/lib';
import { AdminAccountApi } from '../../api/admin-account.api';

@State<AdminAuthStateModel>({
  name: 'admin_auth',
  defaults: {},
})
@Injectable()
export class AdminAuthState {
  constructor(private authApi: AuthApi, private adminAccountApi: AdminAccountApi) {}

  @Selector()
  static user(state: AdminAuthStateModel) {
    return state.user;
  }

  @Action(Login)
  login(context: StateContext<AdminAuthStateModel>, { request }: Login) {
    return this.authApi.loginWithEmail(request).pipe(
      tap((response: ApiResponseModel<UserAuthorityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.user = response.data;
          })
        );
      })
    );
  }

  @Action(Logout)
  logout(context: StateContext<AdminAuthStateModel>) {
    return this.authApi.logout().pipe(
      tap(() => {
        context.patchState(
          produce(context.getState(), (draft) => {
            draft.user = undefined;
          })
        );
      })
    );
  }

  @Action(Me)
  me(context: StateContext<AdminAuthStateModel>) {
    return this.authApi.getAuthority().pipe(
      tap((response: ApiResponseModel<UserAuthorityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.user = response.data;
          })
        );
      })
    );
  }

  @Action(RefreshToken)
  refreshToken() {
    return this.authApi.refreshToken();
  }

  @Action(ForgetPassword)
  forgetPassword(context: StateContext<AdminAuthStateModel>, { request }: ForgetPassword) {
    return this.adminAccountApi.forgetPassword(request);
  }

  @Action(ResetPassword)
  resetPassword(context: StateContext<AdminAuthStateModel>, { request }: ResetPassword) {
    return this.adminAccountApi.resetPassword(request);
  }

  @Action(GenerateOtpEmail)
  generateOtpEmail(context: StateContext<AdminAuthStateModel>, { request }: GenerateOtpEmail) {
    return this.adminAccountApi.generateEmailOtp(request);
  }

  @Action(GenerateActivateTwoFactorAuthorizationEmail)
  generateActivateEmailOtp(context: StateContext<AdminAuthStateModel>, { request }: GenerateActivateTwoFactorAuthorizationEmail) {
    return this.adminAccountApi.generateActivateEmailOtp(request);
  }

  @Action(TwoFactorAuthorization)
  twoFactorAuthorization(context: StateContext<AdminAuthStateModel>, { request }: TwoFactorAuthorization) {
    return this.authApi.twoFactorAuthorization(request).pipe(
      tap((response: ApiResponseModel<UserAuthorityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.user = response.data;
          })
        );
      })
    );
  }
}
