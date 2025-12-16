import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { AccountApi } from '../../api/account.api';
import {
  PreviewPlanSwitch,
  QueryAccountSubscriptions,
  Register,
  RegisterWabaWebhook,
  ResendVerifyEmail,
  ResetAccountSubscriptions,
  SwitchPlan,
  TestWabaLogin,
  UnregisterWabaWebhook,
  UpdateAccount,
  UpdatePlan, UpdateSftp,
  UpdateWabaInfo,
  VerifyEmail
} from "./account.state-actions";
import {
  AccountSubscriptionModel,
  ApiMessage,
  ApiResponseModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel
} from "@grabbill/lib";
import { AccountStateModel } from "./account-state.model";
import { Me, UpdateSubscription } from "../auth/auth.state-actions";

@State<AccountStateModel>({
  name: 'account',
  defaults: {
    accountSubscriptionPageable: makePageable(10),
    accountSubscriptionSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class AccountState {
  constructor(private accountApi: AccountApi) {}

  @Selector()
  static prePlanSwitchInvoice(state: AccountStateModel) {
    return state.prePlanSwitchInvoice;
  }

  @Selector()
  static switchPlanRequest(state: AccountStateModel) {
    return state.switchPlanRequest;
  }

  @Selector()
  static accountSubscriptionPageable(state: AccountStateModel) {
    return state.accountSubscriptionPageable;
  }

  @Selector()
  static accountSubscriptionSearchResult(state: AccountStateModel) {
    return state.accountSubscriptionSearchResult;
  }

  @Action(Register)
  register(context: StateContext<AccountStateModel>, { request }: Register) {
    return this.accountApi.register(request).pipe(
      tap((response: ApiResponseModel<ApiMessage>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.message = response.data?.message;
          })
        );
      })
    );
  }

  @Action(UpdateAccount)
  updateAccount(context: StateContext<AccountStateModel>, { request }: UpdateAccount) {
    return this.accountApi.updateAccount(request).pipe(tap(() => context.dispatch(new Me())));
  }

  @Action(UpdatePlan)
  updatePlan(context: StateContext<AccountStateModel>, { request }: UpdatePlan) {
    return this.accountApi.updatePlan(request).pipe(
      tap((response) => {
        context.dispatch(new UpdateSubscription(response.data));
      })
    );
  }

  @Action(SwitchPlan)
  switchPlan(context: StateContext<AccountStateModel>) {
    return this.accountApi.switchPlan(context.getState().switchPlanRequest!).pipe(
      tap((response) => {
        context.dispatch(new UpdateSubscription(response.data));
      })
    );
  }

  @Action(PreviewPlanSwitch)
  preSwitchPlan(context: StateContext<AccountStateModel>, { request }: PreviewPlanSwitch) {
    return this.accountApi.previewPlanSwitch(request).pipe(
      tap((response) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.switchPlanRequest = request;
            draft.prePlanSwitchInvoice = response.data;
            draft.prePlanSwitchInvoice!.planId = request.planId;
          })
        );
      })
    );
  }

  @Action(ResendVerifyEmail)
  resendVerifyEmail(context: StateContext<AccountStateModel>, { request }: ResendVerifyEmail) {
    return this.accountApi.resendVerifyEmail(request);
  }

  @Action(VerifyEmail)
  verifyEmail(context: StateContext<AccountStateModel>, { request }: VerifyEmail) {
    return this.accountApi.verifyEmail(request);
  }

  @Action(ResetAccountSubscriptions)
  resetInvoices(context: StateContext<AccountStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.accountSubscriptionSearchResult = makeSearchResultPayload();
        draft.accountSubscriptionPageable = makePageable(10);
      })
    );
  }

  @Action(QueryAccountSubscriptions)
  queryInvoices(context: StateContext<AccountStateModel>, { pageable }: QueryAccountSubscriptions) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.accountSubscriptionPageable = pageable ? pageable : draft.accountSubscriptionPageable;
      })
    );

    return this.accountApi.getAccountSubscriptions(context.getState().accountSubscriptionPageable).pipe(
      tap((response: ApiResponseModel<SearchResultPayloadModel<AccountSubscriptionModel>>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.accountSubscriptionSearchResult = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateWabaInfo)
  updateWabaInfo(context: StateContext<AccountStateModel>, { request }: UpdateWabaInfo) {
    return this.accountApi.updateWaba(request).pipe(tap(() => context.dispatch(new Me())));
  }

  @Action(TestWabaLogin)
  testWabaLogin(context: StateContext<AccountStateModel>) {
    return this.accountApi.wabaLogin();
  }

  @Action(RegisterWabaWebhook)
  registerWabaWebhook(context: StateContext<AccountStateModel>) {
    return this.accountApi.registerWabaWebhook().pipe(tap(() => context.dispatch(new Me())));
  }

  @Action(UnregisterWabaWebhook)
  unregisterWabaWebhook(context: StateContext<AccountStateModel>) {
    return this.accountApi.unregisterWabaWebhook().pipe(
      tap(() => context.dispatch(new Me()))
    );
  }

  @Action(UpdateSftp)
  updateSftp(context: StateContext<AccountStateModel>, { request }: UpdateSftp) {
    return this.accountApi.updateSftpSettings(request).pipe(
      tap(() => context.dispatch(new Me()))
    );
  }
}
