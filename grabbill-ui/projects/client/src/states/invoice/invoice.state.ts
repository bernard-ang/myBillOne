import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  InvoiceBasicModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { InvoiceStateModel } from './invoice.state-model';
import { InvoiceApi } from '../../api/invoice.api';
import {
  GetInvoice,
  GetInvoiceBySubscriptionId,
  PayInvoice,
  QueryInvoices,
  ResetInvoice,
  ResetInvoices
} from "./invoice.state-actions";

@State<InvoiceStateModel>({
  name: 'invoice',
  defaults: {
    invoicePageable: makePageable(10),
    invoiceSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class InvoiceState {
  constructor(private invoiceApi: InvoiceApi) {}

  @Selector()
  static invoiceSearchResult(state: InvoiceStateModel) {
    return state.invoiceSearchResult;
  }

  @Selector()
  static invoicePageable(state: InvoiceStateModel) {
    return state.invoicePageable;
  }

  @Selector()
  static invoice(state: InvoiceStateModel) {
    return state.invoice;
  }

  @Action(ResetInvoices)
  resetInvoices(context: StateContext<InvoiceStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.invoiceSearchResult = makeSearchResultPayload();
        draft.invoicePageable = makePageable(10);
      })
    );
  }

  @Action(ResetInvoice)
  resetInvoice(context: StateContext<InvoiceStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.invoice = undefined;
      })
    );
  }

  @Action(QueryInvoices)
  queryInvoices(context: StateContext<InvoiceStateModel>, { pageable }: QueryInvoices) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.invoicePageable = pageable ? pageable : draft.invoicePageable;
      })
    );

    return this.invoiceApi.getInvoices(context.getState().invoicePageable).pipe(
      tap((response: ApiResponseModel<SearchResultPayloadModel<InvoiceBasicModel>>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.invoiceSearchResult = response.data;
          })
        );
      })
    );
  }

  @Action(GetInvoice)
  getInvoice(context: StateContext<InvoiceStateModel>, { id }: GetInvoice) {
    return this.invoiceApi.getInvoice(id).pipe(
      tap((response) =>
        context.setState(
          produce(context.getState(), (draft) => {
            draft.invoice = response.data;
          })
        )
      )
    );
  }

  @Action(GetInvoiceBySubscriptionId)
  getInvoiceBySubscriptionId(context: StateContext<InvoiceStateModel>, { accountSubscriptionId }: GetInvoiceBySubscriptionId) {
    return this.invoiceApi.getInvoiceByAccountSubscriptionId(accountSubscriptionId).pipe(
      tap((response) =>
        context.setState(
          produce(context.getState(), (draft) => {
            draft.invoice = response.data;
          })
        )
      )
    );
  }

  @Action(PayInvoice)
  payInvoice(context: StateContext<InvoiceStateModel>, { id }: GetInvoice) {
    return this.invoiceApi.retryInvoicePayment(id).pipe(
      tap(() => {
        context.dispatch(QueryInvoices);
      })
    );
  }
}
