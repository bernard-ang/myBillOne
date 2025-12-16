import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { AuthStateModel } from './auth.state-model';
import {
  Authorize,
  ForgetPassword, GenerateActivateTwoFactorAuthorizationEmail,
  GenerateOtpEmail,
  Login,
  Logout,
  Me,
  RefreshToken,
  ResetPassword,
  TwoFactorAuthorization,
  UpdateSubscription
} from "./auth.state-actions";
import { AuthApi } from '../../api/auth.api';
import { ApiResponseModel, UserAuthorityModel } from '@grabbill/lib';
import { AccountApi } from '../../api/account.api';

@State<AuthStateModel>({
  name: 'auth',
  defaults: {},
})
@Injectable()
export class AuthState {
  constructor(private authApi: AuthApi, private accountApi: AccountApi) {}

  @Selector()
  static user(state: AuthStateModel) {
    return state.user;
  }

  @Selector()
  static email(state: AuthStateModel) {
    return state.email;
  }

  @Action(Login)
  login(context: StateContext<AuthStateModel>, { request }: Login) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.email = request.email;
      })
    );

    return this.authApi.login(request).pipe(
      tap((response: ApiResponseModel<UserAuthorityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.user = response.data;
          })
        );
      })
    );
  }

  @Action(Authorize)
  authorize(context: StateContext<AuthStateModel>, { request }: Authorize) {
    return this.authApi.authorize(request).pipe(
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
  logout(context: StateContext<AuthStateModel>) {
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
  me(context: StateContext<AuthStateModel>) {
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

  @Action(UpdateSubscription)
  updateSubscription(context: StateContext<AuthStateModel>, { subscription }: UpdateSubscription) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.user!.subscription = subscription;
      })
    );
  }

  @Action(ForgetPassword)
  forgetPassword(context: StateContext<AuthStateModel>, { request }: ForgetPassword) {
    return this.accountApi.forgetPassword(request);
  }

  @Action(ResetPassword)
  resetPassword(context: StateContext<AuthStateModel>, { request }: ResetPassword) {
    return this.accountApi.resetPassword(request);
  }

  @Action(GenerateOtpEmail)
  generateOtpEmail(context: StateContext<AuthStateModel>, { request }: GenerateOtpEmail) {
    return this.accountApi.generateEmailOtp(request);
  }

  @Action(GenerateActivateTwoFactorAuthorizationEmail)
  generateActivateEmailOtp(context: StateContext<AuthStateModel>, { request }: GenerateActivateTwoFactorAuthorizationEmail) {
    return this.accountApi.generateActivateEmailOtp(request);
  }

  @Action(TwoFactorAuthorization)
  twoFactorAuthorization(context: StateContext<AuthStateModel>, { request }: TwoFactorAuthorization) {
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
