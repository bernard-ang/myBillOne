import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ResetPayment } from '../../../../states/payment/payment.state-actions';
import { ActivatedRoute } from '@angular/router';
import { of, switchMap, tap } from 'rxjs';
import { PreviewPlanSwitch } from '../../../../states/account/account.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { getErrorMessage } from '@grabbill/lib';
import { AccountState } from '../../../../states/account/account.state';
import { AuthState } from '../../../../states/auth/auth.state';

@Component({
  selector: 'grabbill-client-register-payment-method-setup-summary',
  templateUrl: './register-payment-method-setup-summary.component.html',
  styleUrls: ['./register-payment-method-setup-summary.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPaymentMethodSetupSummaryComponent extends NgxsBaseComponent {
  status = '';
  isLoading = false;

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private route: ActivatedRoute,
    private actions$: Actions,
    private cd: ChangeDetectorRef
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.store.dispatch(new ResetPayment());
    this.autoUnsubscribe(
      this.route.queryParams.pipe(
        tap((params) => {
          this.status = params['status'];
          if (this.isSuccess()) {
            const emailCampaignSize = params['emailCampaignSize'];
            const planId = params['planId'];
            const storageSize = params['storageSize'];
            const subscriptionMode = params['subscriptionMode'];
            const transactionalEmailSize = params['transactionalEmailSize'];
            const promoCode = params['promoCode'];

            this.isLoading = true;

            this.store.dispatch(
              new PreviewPlanSwitch({
                planId: planId,
                subscriptionMode: subscriptionMode,
                emailCampaignSize: Number(emailCampaignSize),
                storageSize: Number(storageSize),
                transactionalEmailSize: Number(transactionalEmailSize),
                promoCode: promoCode && promoCode.trim().length > 0 ? promoCode : undefined,
              })
            );
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(PreviewPlanSwitch),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const invoice = this.store.selectSnapshot(AccountState.prePlanSwitchInvoice);
            const user = this.store.selectSnapshot(AuthState.user)!;

            if (!user.paymentMethodRequired && invoice?.paymentMethodAvailable) {
              return this.navigate(['/', 'register', 'switch-plan']);
            } else {
              this.status = 'error';
              this.isLoading = false;
            }
          }

          return of(false);
        })
      )
    );
  }

  getStatus() {
    if (this.isSuccess()) {
      return 'success';
    } else {
      return 'error';
    }
  }

  getStatusMessage() {
    if (this.isSuccess()) {
      return 'Successfully add payment method';
    } else {
      return 'Failed to add payment method';
    }
  }

  getDescription() {
    if (this.isSuccess()) {
      return 'You may start using your subscribed plan.';
    } else {
      return 'Setup cancel';
    }
  }

  isSuccess() {
    return this.status === 'success';
  }
}
