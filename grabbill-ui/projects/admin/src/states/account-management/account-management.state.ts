import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  AccountBasicModel,
  AccountDetailsModel,
  ApiResponseModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
  UserModel,
  WhatsappTemplateModel
} from '@grabbill/lib';
import { AccountManagementStateModel } from './account-management.state-model';
import { AccountManagementApi } from '../../api/account-management.api';
import {
  DownloadAccountReport,
  GetAccount,
  GetAccountUsers,
  GetAccountWabaTemplates,
  QueryAccounts,
  RegisterWabaWebhook,
  ResetAccount,
  ResetAccounts,
  SwitchAccountPlan,
  TestWabaLogin,
  UnregisterWabaWebhook,
  UpdateAccountPaymentExemptionStatus,
  UpdateAccountStatus,
  UpdateWabaInfo,
} from './account-management.state-actions';
import { InvoiceManagementStateModel } from '../invoice-management/invoice-management.state-model';
import { JobManagementStateModel } from '../job-management/job-management.state-model';

@State<AccountManagementStateModel>({
  name: 'account_management',
  defaults: {
    filters: {},
    accountPageable: makePageable(10, 1, 'lastModifiedDate', 'DESC'),
    accountSearchResult: makeSearchResultPayload(),
    accountUsers: [],
    wabaTemplates: [],
  },
})
@Injectable()
export class AccountManagementState {
  constructor(private accountManagementApi: AccountManagementApi) {}

  @Selector()
  static accountSearchResult(state: AccountManagementStateModel) {
    return state.accountSearchResult;
  }

  @Selector()
  static accountPageable(state: AccountManagementStateModel) {
    return state.accountPageable;
  }

  @Selector()
  static account(state: AccountManagementStateModel) {
    return state.account;
  }

  @Selector()
  static accountUsers(state: AccountManagementStateModel) {
    return state.accountUsers;
  }

  @Selector()
  static nameSelector(state: AccountManagementStateModel) {
    return state.name;
  }

  @Selector()
  static file(state: AccountManagementStateModel) {
    return state.file;
  }

  @Selector()
  static filters(state: JobManagementStateModel) {
    return state.filters;
  }

  @Selector()
  static startDate(state: JobManagementStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: JobManagementStateModel) {
    return state.endDate;
  }

  @Selector()
  static wabaTemplates(state: AccountManagementStateModel) {
    return state.wabaTemplates;
  }

  @Action(ResetAccounts)
  resetUsers(context: StateContext<AccountManagementStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.accountSearchResult = makeSearchResultPayload();
        draft.accountPageable = makePageable(10, 1, 'lastModifiedDate', 'DESC');
        draft.filters = {};
        draft.name = undefined;
        draft.startDate = undefined;
        draft.endDate = undefined;
      })
    );
  }

  @Action(QueryAccounts)
  queryAccounts(
    context: StateContext<AccountManagementStateModel>,
    { pageable, name, filters, startDate, endDate }: QueryAccounts
  ) {
    context.setState(
      produce(context.getState(), (draft: AccountManagementStateModel) => {
        draft.accountPageable = pageable ? pageable : draft.accountPageable;
        draft.name = name;
        draft.filters = filters;
        draft.startDate = startDate;
        draft.endDate = endDate;
      })
    );

    return this.accountManagementApi
      .getAccounts(
        context.getState().accountPageable,
        context.getState().name,
        context.getState().filters,
        context.getState().startDate,
        context.getState().endDate
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<AccountBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft: AccountManagementStateModel) => {
              draft.accountSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(ResetAccount)
  resetAccount(context: StateContext<AccountManagementStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.account = undefined;
        draft.accountUsers = [];
        draft.wabaTemplates = [];
      })
    );
  }

  @Action(GetAccount)
  getAccount(context: StateContext<AccountManagementStateModel>, { id }: GetAccount) {
    return this.accountManagementApi.getAccount(id).pipe(
      tap((response: ApiResponseModel<AccountDetailsModel>) => {
        context.setState(
          produce(context.getState(), (draft: AccountManagementStateModel) => {
            draft.account = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateAccountStatus)
  updateAccountStatus(context: StateContext<AccountManagementStateModel>, { id, active }: UpdateAccountStatus) {
    return this.accountManagementApi.updateAccountStatus(id, { active }).pipe(
      tap(() => {
        context.setState(
          produce(context.getState(), (draft: AccountManagementStateModel) => {
            if (draft.account) {
              draft.account.active = active;
            }
          })
        );
      })
    );
  }

  @Action(UpdateAccountPaymentExemptionStatus)
  updateAccountPaymentExemptionStatus(
    context: StateContext<AccountManagementStateModel>,
    { id, paymentExemption }: UpdateAccountPaymentExemptionStatus
  ) {
    return this.accountManagementApi.updatePaymentExemptionStatus(id, { paymentExempted: paymentExemption }).pipe(
      tap((response: ApiResponseModel<AccountDetailsModel>) => {
        context.setState(
          produce(context.getState(), (draft: AccountManagementStateModel) => {
            draft.account = response.data;
          })
        );
      })
    );
  }

  @Action(SwitchAccountPlan)
  switchPlan(context: StateContext<AccountManagementStateModel>, { id, request }: SwitchAccountPlan) {
    return this.accountManagementApi.switchPlan(id, request).pipe(
      tap((response: ApiResponseModel<AccountDetailsModel>) => {
        context.setState(
          produce(context.getState(), (draft: AccountManagementStateModel) => {
            draft.account = response.data;
          })
        );
      })
    );
  }

  @Action(GetAccountUsers)
  getAccountUsers(context: StateContext<AccountManagementStateModel>, { id }: GetAccountUsers) {
    return this.accountManagementApi.getAccountUsers(id).pipe(
      tap((response: ApiResponseModel<{ users: UserModel[] }>) => {
        context.setState(
          produce(context.getState(), (draft: AccountManagementStateModel) => {
            draft.accountUsers = response.data.users;
          })
        );
      })
    );
  }

  @Action(DownloadAccountReport)
  downloadInvoiceReport(
    context: StateContext<InvoiceManagementStateModel>,
    { startDate, endDate, affiliateCode }: DownloadAccountReport
  ) {
    return this.accountManagementApi.downloadReport(startDate, endDate, affiliateCode).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft: InvoiceManagementStateModel) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(UpdateWabaInfo)
  updateWabaInfo(context: StateContext<AccountManagementStateModel>, { id, request }: UpdateWabaInfo) {
    return this.accountManagementApi.updateWaba(id, request).pipe(
      tap((response: ApiResponseModel<AccountDetailsModel>) => {
        context.setState(
          produce(context.getState(), (draft: AccountManagementStateModel) => {
            draft.account = response.data;
          })
        );
      })
    );
  }

  @Action(TestWabaLogin)
  testWabaLogin(context: StateContext<AccountManagementStateModel>, { id }: TestWabaLogin) {
    return this.accountManagementApi.wabaLogin(id);
  }

  @Action(RegisterWabaWebhook)
  registerWabaWebhook(context: StateContext<AccountManagementStateModel>, { id }: RegisterWabaWebhook) {
    return this.accountManagementApi.registerWabaWebhook(id).pipe(
      tap((response: ApiResponseModel<AccountDetailsModel>) => {
        context.setState(
          produce(context.getState(), (draft: AccountManagementStateModel) => {
            draft.account = response.data;
          })
        );
      })
    );
  }

  @Action(UnregisterWabaWebhook)
  unregisterWabaWebhook(context: StateContext<AccountManagementStateModel>, { id }: UnregisterWabaWebhook) {
    return this.accountManagementApi.unregisterWabaWebhook(id).pipe(
      tap((response: ApiResponseModel<AccountDetailsModel>) => {
        context.setState(
          produce(context.getState(), (draft: AccountManagementStateModel) => {
            draft.account = response.data;
          })
        );
      })
    );
  }

  @Action(GetAccountWabaTemplates)
  getAccountWabaTemplates(context: StateContext<AccountManagementStateModel>, { id }: GetAccountWabaTemplates) {
    return this.accountManagementApi.getAccountWabaTemplates(id).pipe(
      tap((response: ApiResponseModel<{ templates: WhatsappTemplateModel[] }>) => {
        context.setState(
          produce(context.getState(), (draft: AccountManagementStateModel) => {
            draft.wabaTemplates = response.data.templates;
          })
        );
      })
    );
  }
}
