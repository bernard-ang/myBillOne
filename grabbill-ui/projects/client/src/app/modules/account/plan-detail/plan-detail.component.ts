import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { DashboardState } from '../../../../states/dashboard/dashboard.state';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  AccountSubscriptionModel,
  DashboardStatisticsModel,
  getErrorMessage,
  InvoiceBasicModel,
  InvoiceStatus,
  PageableModel,
  resolveErrorMessage,
  SearchResultPayloadModel,
  SmsCreditsPlanOptionPayloadModel,
  StripeSessionType,
  SubscriptionMode,
  updateAndMarkControlAsDirty,
  UserAuthorityModel,
} from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { NzMessageService } from 'ng-zorro-antd/message';
import { GetCurrentStatistics } from '../../../../states/dashboard/dashboard.state-actions';
import prettyBytes from 'pretty-bytes';
import { AuthState } from '../../../../states/auth/auth.state';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
// @ts-ignore
import { Country } from 'country-state-city';
import { PaymentState } from '../../../../states/payment/payment.state';
import { ResetPayment, SetupStripeSession } from '../../../../states/payment/payment.state-actions';
import { InvoiceState } from '../../../../states/invoice/invoice.state';
import { PayInvoice, QueryInvoices, ResetInvoices } from '../../../../states/invoice/invoice.state-actions';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { environment } from '../../../../environments/environment';
import { getInvoiceStatusTag } from '../../../../utils/get-status-tag';
import { ActivatedRoute } from '@angular/router';
import { NzResultStatusType } from 'ng-zorro-antd/result';
import { GetSmsPlanOptions, TopupSmsCredit } from '../../../../states/sms-credit/sms-credit.state-actions';
import { SmsCreditState } from '../../../../states/sms-credit/sms-credit.state';
import { AccountState } from "../../../../states/account/account.state";
import { QueryAccountSubscriptions, ResetAccountSubscriptions } from "../../../../states/account/account.state-actions";

@Component({
  selector: 'grabbill-client-plan-detail',
  templateUrl: './plan-detail.component.html',
  styleUrls: ['./plan-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlanDetailComponent extends NgxsBaseComponent {
  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  @Select(DashboardState.currentStatistics)
  currentStatistics$!: Observable<DashboardStatisticsModel>;

  @Select(InvoiceState.invoiceSearchResult)
  invoiceSearchResult$!: Observable<SearchResultPayloadModel<InvoiceBasicModel>>;

  @Select(InvoiceState.invoicePageable)
  invoicePageable$!: Observable<PageableModel>;

  @Select(AccountState.accountSubscriptionSearchResult)
  accountSubscriptionSearchResult$!: Observable<SearchResultPayloadModel<AccountSubscriptionModel>>;

  @Select(AccountState.accountSubscriptionPageable)
  accountSubscriptionPageable$!: Observable<PageableModel>;

  @Select(SmsCreditState.options)
  smsOptions$!: Observable<SmsCreditsPlanOptionPayloadModel[]>;

  isUpdateBillingInfoLoading = false;

  isInvoiceTableLoading = false;
  isAccountSubscriptionTableLoading = false;

  action?: string;
  refNo?: string;
  status?: NzResultStatusType;
  errDesc?: string;

  isTopUpModalVisible = false;
  topUpForm: UntypedFormGroup;

  canTopUpSms = environment.config.canTopUpSms;

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private route: ActivatedRoute,
    private actions$: Actions,
    private cd: ChangeDetectorRef,
    private fb: UntypedFormBuilder
  ) {
    super(store, messageService);

    this.topUpForm = this.fb.group({
      option: [null, [Validators.required]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetPayment());
    this.store.dispatch(new ResetInvoices());
    this.store.dispatch(new ResetAccountSubscriptions());
    this.store.dispatch(new GetSmsPlanOptions());

    this.autoUnsubscribe(
      this.route.queryParams.pipe(
        tap((params) => {
          this.action = params['action'];
          this.refNo = params['RefNo'];
          this.status = params['status'] === 'true' ? 'success' : 'error';
          this.errDesc = params['errDesc'];
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetCurrentStatistics),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(QueryInvoices),
        switchMap((data: ActionCompletion) => {
          this.isInvoiceTableLoading = false;
          this.cd.markForCheck();
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(QueryAccountSubscriptions),
        switchMap((data: ActionCompletion) => {
          this.isAccountSubscriptionTableLoading = false;
          this.cd.markForCheck();
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(PayInvoice),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else {
            this.store.dispatch(new ShowMessage('info', 'Retry payment in progress'));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(SetupStripeSession),
        switchMap(async (data: ActionCompletion) => {
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.store.dispatch(new SetPageLoading(false));
          } else if (data.result.successful) {
            window.location.href = this.store.selectSnapshot(PaymentState.stripeUrl)!;
          }
          this.isUpdateBillingInfoLoading = false;

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(TopupSmsCredit),
        switchMap(async (data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else {
            this.isTopUpModalVisible = false;
            this.cd.markForCheck();
          }

          return of(false);
        })
      )
    );
    this.store.dispatch(new GetCurrentStatistics());
  }

  formatBytes(bytes: number) {
    return prettyBytes(bytes, { locale: true });
  }

  getPrice(subscription: AccountSubscriptionModel) {
    const total = subscription.storagePrice + subscription.emailCampaignPrice + subscription.transactionalEmailPrice;
    return subscription.mode === SubscriptionMode.ANNUALLY ? total * 12 * 0.9 : total;
  }

  doUpdatePlan() {
    this.navigate(['/', 'register', 'plan-selection']);
  }

  async doManageBillingInfo() {
    this.isUpdateBillingInfoLoading = true;
    this.store.dispatch(
      new ShowMessage(
        'loading',
        `Please note that your payment method will be securely redirected to Stripe, our trusted payment gateway partner, to complete your subscription purchase. If you have any questions or concerns, please feel free to contact our customer support team.`
      )
    );

    setTimeout(() => this.store.dispatch(new SetupStripeSession(StripeSessionType.PAYMENT_UPDATE_SESSION)), 2000);
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doInvoiceQuery(event: NzTableQueryParams) {
    this.isInvoiceTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(InvoiceState.invoicePageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(new QueryInvoices(pageable));
  }

  doAccountSubscriptionQuery(event: NzTableQueryParams) {
    this.isAccountSubscriptionTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(AccountState.accountSubscriptionPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(new QueryAccountSubscriptions(pageable));
  }

  dateFormat() {
    return environment.config.dateFormat;
  }

  getStatusTag(status: InvoiceStatus) {
    return getInvoiceStatusTag(status);
  }

  doPay(invoice: InvoiceBasicModel) {
    this.store.dispatch(new PayInvoice(invoice.id));
  }

  doOpenTopUpModal() {
    this.topUpForm.reset();
    this.isTopUpModalVisible = true;
  }

  doCloseTopUpModal() {
    this.isTopUpModalVisible = false;
  }

  doTopup() {
    if (this.topUpForm.valid) {
      this.store.dispatch(new TopupSmsCredit({ optionId: this.topUpForm.value.option }));
    } else {
      updateAndMarkControlAsDirty(this.topUpForm);
      this.cd.markForCheck();
    }
  }

  doContactUsForSms() {

  }
}
