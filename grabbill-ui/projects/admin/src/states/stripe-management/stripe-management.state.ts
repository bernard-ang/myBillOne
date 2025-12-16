import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
  StripeEventBasicModel,
  StripeEventModel,
} from '@grabbill/lib';
import { StripeManagementStateModel } from './stripe-management-state.model';
import {
  GetStripeEvent,
  QueryStripeEvents,
  ResetStripeEvent,
  ResetStripEvents,
} from './stripe-management.state-actions';
import { StripeEventManagementApi } from '../../api/stripe-event-management.api';

@State<StripeManagementStateModel>({
  name: 'stripe_management',
  defaults: {
    eventPageable: makePageable(10),
    eventSearchResult: makeSearchResultPayload(),
    accountName: '',
    type: '',
    refId: '',
  },
})
@Injectable()
export class StripeManagementState {
  constructor(private paymentTransactionManagementApi: StripeEventManagementApi) {}

  @Selector()
  static eventSearchResult(state: StripeManagementStateModel) {
    return state.eventSearchResult;
  }

  @Selector()
  static eventPageable(state: StripeManagementStateModel) {
    return state.eventPageable;
  }

  @Selector()
  static accountName(state: StripeManagementStateModel) {
    return state.accountName;
  }

  @Selector()
  static type(state: StripeManagementStateModel) {
    return state.type;
  }

  @Selector()
  static refId(state: StripeManagementStateModel) {
    return state.refId;
  }

  @Selector()
  static startDate(state: StripeManagementStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: StripeManagementStateModel) {
    return state.endDate;
  }

  @Selector()
  static event(state: StripeManagementStateModel) {
    return state.event;
  }

  @Action(ResetStripEvents)
  resetPaymentTransactions(context: StateContext<StripeManagementStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.eventSearchResult = makeSearchResultPayload();
        draft.eventPageable = makePageable(10);
        draft.startDate = undefined;
        draft.endDate = undefined;
        draft.accountName = '';
        draft.type = '';
        draft.refId = '';
      })
    );
  }

  @Action(QueryStripeEvents)
  queryPaymentTransactions(
    context: StateContext<StripeManagementStateModel>,
    { pageable, accountName, refId, type, startDate, endDate }: QueryStripeEvents
  ) {
    context.setState(
      produce(context.getState(), (draft: StripeManagementStateModel) => {
        draft.eventPageable = pageable ? pageable : draft.eventPageable;
        draft.accountName = accountName ?? '';
        draft.refId = refId ?? '';
        draft.type = type ?? '';
        draft.startDate = startDate;
        draft.endDate = endDate;
      })
    );

    return this.paymentTransactionManagementApi
      .getEvents(
        context.getState().eventPageable,
        context.getState().accountName,
        context.getState().type,
        context.getState().refId,
        context.getState().startDate,
        context.getState().endDate
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<StripeEventBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft: StripeManagementStateModel) => {
              draft.eventSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(ResetStripeEvent)
  resetPaymentTransaction(context: StateContext<StripeManagementStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.event = undefined;
      })
    );
  }

  @Action(GetStripeEvent)
  getAccount(context: StateContext<StripeManagementStateModel>, { id }: GetStripeEvent) {
    return this.paymentTransactionManagementApi.getEvent(id).pipe(
      tap((response: ApiResponseModel<StripeEventModel>) => {
        context.setState(
          produce(context.getState(), (draft: StripeManagementStateModel) => {
            draft.event = response.data;
          })
        );
      })
    );
  }
}
