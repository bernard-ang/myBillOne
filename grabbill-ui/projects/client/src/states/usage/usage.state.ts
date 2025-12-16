import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { AccountApi } from '../../api/account.api';
import {
  AccountStatementModel,
  ApiResponseModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel, SmsUsageSummaryPayloadModel
} from "@grabbill/lib";
import { UsageStateModel } from './usage-state.model';
import { GetSmsUsage, QueryAccountStatements, ResetAccountStatements, ResetSmsUsage } from "./usage.state-actions";
import { SmsApi } from "../../api/sms.api";

@State<UsageStateModel>({
  name: 'usage',
  defaults: {
    accountStatementPageable: makePageable(10),
    accountStatementSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class UsageState {
  constructor(private accountApi: AccountApi, private smsApi: SmsApi) {}

  @Selector()
  static accountStatementPageable(state: UsageStateModel) {
    return state.accountStatementPageable;
  }

  @Selector()
  static accountStatementSearchResult(state: UsageStateModel) {
    return state.accountStatementSearchResult;
  }

  @Selector()
  static startDate(state: UsageStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: UsageStateModel) {
    return state.endDate;
  }

  @Selector()
  static smsUsageSummary(state: UsageStateModel) {
    return state.smsUsageSummary;
  }

  @Action(ResetAccountStatements)
  resetAccountStatements(context: StateContext<UsageStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.accountStatementSearchResult = makeSearchResultPayload();
        draft.accountStatementPageable = makePageable(10);
        draft.startDate = undefined;
        draft.endDate = undefined;
      })
    );
  }

  @Action(QueryAccountStatements)
  queryAccountStatements(context: StateContext<UsageStateModel>, { pageable, startDate, endDate }: QueryAccountStatements) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.accountStatementPageable = pageable ? pageable : draft.accountStatementPageable;
        draft.startDate = startDate;
        draft.endDate = endDate;
      })
    );

    return this.accountApi
      .getStatements(
        context.getState().accountStatementPageable,
        context.getState().startDate,
        context.getState().endDate
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<AccountStatementModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.accountStatementSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(ResetSmsUsage)
  resetSmsUsage(context: StateContext<UsageStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.smsUsageSummary = undefined;
      })
    );
  }

  @Action(GetSmsUsage)
  getSmsUsage(context: StateContext<UsageStateModel>, { startDate, endDate }: GetSmsUsage) {

    return this.smsApi
      .getUsageSummary(startDate, endDate)
      .pipe(
        tap((response: ApiResponseModel<SmsUsageSummaryPayloadModel>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.smsUsageSummary = response.data;
            })
          );
        })
      );
  }
}
