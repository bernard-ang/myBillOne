import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { SmsCreditApi } from '../../api/sms-credit.api';
import { GetSmsPlanOptions, TopupSmsCredit } from './sms-credit.state-actions';
import { SmsCreditStateModel } from './sms-credit.state-model';
import { GetCurrentStatistics } from '../dashboard/dashboard.state-actions';
import produce from 'immer';
import { QueryInvoices } from "../invoice/invoice.state-actions";

@State<SmsCreditStateModel>({
  name: 'SmsCredit',
  defaults: {
    options: [],
  },
})
@Injectable()
export class SmsCreditState {
  constructor(private SmsCreditApi: SmsCreditApi) {}

  @Selector()
  static options(state: SmsCreditStateModel) {
    return state.options;
  }

  @Action(TopupSmsCredit)
  getSmsCredits(context: StateContext<SmsCreditStateModel>, { request }: TopupSmsCredit) {
    return this.SmsCreditApi.topup(request).pipe(
      tap(() => {
        context.dispatch(new GetCurrentStatistics());
        context.dispatch(new QueryInvoices());
      })
    );
  }

  @Action(GetSmsPlanOptions)
  getSmsPlanOptions(context: StateContext<SmsCreditStateModel>) {
    return this.SmsCreditApi.getCreditsPlanOptions().pipe(
      tap((response) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.options = response.data.options;
          })
        );
      })
    );
  }
}
