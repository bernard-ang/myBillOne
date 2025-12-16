import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { ApiResponseModel, HostedPaymentUiModel } from '@grabbill/lib';
import { PaymentStateModel } from './payment-state.model';
import { ResetPayment, SetupStripeSession } from './payment.state-actions';
import { StripeSessionApi } from '../../api/stripe-session.api';

@State<PaymentStateModel>({
  name: 'payment_method',
  defaults: {},
})
@Injectable()
export class PaymentState {
  constructor(private stripeSessionApi: StripeSessionApi) {}

  @Selector()
  static stripeUrl(state: PaymentStateModel) {
    return state.stripeUrl;
  }

  @Action(ResetPayment)
  reset(context: StateContext<PaymentStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.stripeUrl = undefined;
      })
    );
  }

  @Action(SetupStripeSession)
  setupStripeSession(context: StateContext<PaymentStateModel>, { sessionType, request }: SetupStripeSession) {
    return this.stripeSessionApi.createSetupSession(sessionType, request).pipe(
      tap((response: ApiResponseModel<HostedPaymentUiModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.stripeUrl = response.data.url;
          })
        );
      })
    );
  }
}
