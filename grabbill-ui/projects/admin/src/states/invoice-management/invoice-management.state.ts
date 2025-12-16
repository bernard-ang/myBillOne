import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  InvoiceBasicModel,
  InvoiceModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { InvoiceManagementStateModel } from './invoice-management.state-model';
import {
  DownloadInvoiceReport,
  GetInvoice,
  QueryInvoices,
  ResetInvoice,
  ResetInvoices,
} from './invoice-management.state-actions';
import { InvoiceManagementApi } from '../../api/invoice-management.api';

@State<InvoiceManagementStateModel>({
  name: 'invoice_management',
  defaults: {
    accountName: '',
    status: '',
    invoiceNo: '',
    planName: '',
    invoicePageable: makePageable(10),
    invoiceSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class InvoiceManagementState {
  constructor(private invoiceManagementApi: InvoiceManagementApi) {}

  @Selector()
  static invoiceSearchResult(state: InvoiceManagementStateModel) {
    return state.invoiceSearchResult;
  }

  @Selector()
  static invoicePageable(state: InvoiceManagementStateModel) {
    return state.invoicePageable;
  }

  @Selector()
  static accountName(state: InvoiceManagementStateModel) {
    return state.accountName;
  }

  @Selector()
  static invoiceNo(state: InvoiceManagementStateModel) {
    return state.invoiceNo;
  }

  @Selector()
  static status(state: InvoiceManagementStateModel) {
    return state.status;
  }

  @Selector()
  static planName(state: InvoiceManagementStateModel) {
    return state.planName;
  }

  @Selector()
  static startDate(state: InvoiceManagementStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: InvoiceManagementStateModel) {
    return state.endDate;
  }

  @Selector()
  static invoice(state: InvoiceManagementStateModel) {
    return state.invoice;
  }

  @Selector()
  static file(state: InvoiceManagementStateModel) {
    return state.file;
  }

  @Action(ResetInvoices)
  resetInvoices(context: StateContext<InvoiceManagementStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.invoiceSearchResult = makeSearchResultPayload();
        draft.invoicePageable = makePageable(10);
        draft.startDate = undefined;
        draft.endDate = undefined;
        draft.accountName = '';
        draft.invoiceNo = '';
        draft.planName = '';
        draft.status = '';
      })
    );
  }

  @Action(QueryInvoices)
  queryInvoices(
    context: StateContext<InvoiceManagementStateModel>,
    { pageable, accountName, invoiceNo, planName, status, startDate, endDate }: QueryInvoices
  ) {
    context.setState(
      produce(context.getState(), (draft: InvoiceManagementStateModel) => {
        draft.invoicePageable = pageable ? pageable : draft.invoicePageable;
        draft.accountName = accountName ?? '';
        draft.invoiceNo = invoiceNo ?? '';
        draft.planName = planName ?? '';
        draft.status = status ?? '';
        draft.startDate = startDate;
        draft.endDate = endDate;
      })
    );

    return this.invoiceManagementApi
      .getInvoices(
        context.getState().invoicePageable,
        context.getState().accountName,
        context.getState().invoiceNo,
        context.getState().planName,
        context.getState().status,
        context.getState().startDate,
        context.getState().endDate
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<InvoiceBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft: InvoiceManagementStateModel) => {
              draft.invoiceSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(ResetInvoice)
  resetInvoice(context: StateContext<InvoiceManagementStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.invoice = undefined;
      })
    );
  }

  @Action(GetInvoice)
  getAccount(context: StateContext<InvoiceManagementStateModel>, { id }: GetInvoice) {
    return this.invoiceManagementApi.getInvoice(id).pipe(
      tap((response: ApiResponseModel<InvoiceModel>) => {
        context.setState(
          produce(context.getState(), (draft: InvoiceManagementStateModel) => {
            draft.invoice = response.data;
          })
        );
      })
    );
  }

  @Action(DownloadInvoiceReport)
  downloadInvoiceReport(
    context: StateContext<InvoiceManagementStateModel>,
    { startDate, endDate, affiliateCode, accountName }: DownloadInvoiceReport
  ) {
    return this.invoiceManagementApi.downloadReport(startDate, endDate, affiliateCode, accountName).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft: InvoiceManagementStateModel) => {
            draft.file = file;
          })
        );
      })
    );
  }
}
