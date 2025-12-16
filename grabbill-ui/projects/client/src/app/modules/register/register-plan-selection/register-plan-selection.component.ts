import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { AbstractControl, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { Observable, of, switchMap, tap } from 'rxjs';
import prettyBytes from 'pretty-bytes';
import {
  AccountSubscriptionModel,
  getErrorMessage,
  PlanModel,
  resolveErrorMessage,
  StripeSessionType,
  SubscriptionMode,
  UserAuthorityModel,
  UserPlanUpdateRequestModel,
} from '@grabbill/lib';
import { environment } from '../../../../environments/environment';
import { AuthState } from '../../../../states/auth/auth.state';
import { Me } from '../../../../states/auth/auth.state-actions';
import { PreviewPlanSwitch, UpdatePlan } from '../../../../states/account/account.state-actions';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { PlanState } from '../../../../states/plan/plan.state';
import { GetPlans } from '../../../../states/plan/plan.state-actions';
import { doDeleteAllIndex } from '../../../../utils/manage-form-array';
import { NzModalService } from 'ng-zorro-antd/modal';
import { AccountState } from '../../../../states/account/account.state';
import { SetupStripeSession } from '../../../../states/payment/payment.state-actions';
import { PaymentState } from '../../../../states/payment/payment.state';

@Component({
  selector: 'grabbill-client-register-plan-selection',
  templateUrl: './register-plan-selection.component.html',
  styleUrls: ['./register-plan-selection.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPlanSelectionComponent extends NgxsBaseComponent {
  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  @Select(PlanState.plans)
  plans$!: Observable<PlanModel[]>;

  isLoading = false;
  form: UntypedFormGroup;
  user?: UserAuthorityModel;
  planId?: number;
  subscription?: AccountSubscriptionModel;
  planRequest?: UserPlanUpdateRequestModel;

  switchPlanModalVisible = false;
  isUpgradePlan = false;
  switchPlanText = '';
  promoCodeForm: UntypedFormGroup;

  newPlanModalVisible = false;

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private fb: UntypedFormBuilder,
    private modal: NzModalService,
    private cd: ChangeDetectorRef,
    private actions$: Actions
  ) {
    super(store);
    this.form = this.fb.group({
      mode: [SubscriptionMode.MONTHLY, [Validators.required]],
      plans: this.fb.array([]),
    });
    this.promoCodeForm = this.fb.group({
      code: [null, [Validators.maxLength(255)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.autoUnsubscribe(
      this.user$.pipe(
        tap((user) => {
          if (user) {
            this.user = user;
            this.store.dispatch(new GetPlans());
          }
        })
      ),
      this.plans$.pipe(
        tap((plans) => {
          doDeleteAllIndex(this.formPlans);

          for (const plan of plans) {
            this.doAddPlan(plan);
            this.cd.markForCheck();
          }
          this.isLoading = false;
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdatePlan),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.autoUnsubscribe(
              this.store.dispatch(new Me()).pipe(
                tap(() => {
                  return this.navigate(['/']);
                })
              )
            );
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(SetupStripeSession),
        switchMap(async (data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.store.dispatch(new SetPageLoading(false));
          } else if (data.result.successful) {
            window.location.href = this.store.selectSnapshot(PaymentState.stripeUrl)!;
          }

          return of(false);
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
            if (this.subscription && (invoice?.paymentMethodAvailable || this.planId === 1)) {
              return this.navigate(['/', 'register', 'switch-plan']);
            } else {
              this.createStripeSession();
            }
          }

          return of(false);
        })
      )
    );

    this.store.dispatch(new Me());
  }

  get formPlans() {
    return this.form.controls['plans'] as UntypedFormArray;
  }

  private createStripeSession() {
    this.store.dispatch(new SetPageLoading(true));
    this.store.dispatch(
      new ShowMessage(
        'info',
        `Thank you for signing up with MyBillOne! ` +
          `Please note that your payment method will be securely redirected to Stripe, our trusted payment gateway partner, to complete your subscription purchase. If you have any questions or concerns, please feel free to contact our customer support team.`
      )
    );

    setTimeout(
      () => this.store.dispatch(new SetupStripeSession(StripeSessionType.PAYMENT_SETUP_SESSION, this.planRequest!)),
      2000
    );
  }

  doSelectPlan(plan: PlanModel, planIndex: number): void {
    this.isLoading = true;
    this.cd.markForCheck();
    const formPlan = this.formPlans.at(planIndex).value;

    this.planId = plan.id;

    this.subscription = this.user!.subscription;
    this.planRequest = {
      planId: plan.id.toString(),
      subscriptionMode: this.form.get('mode')?.value,
      storageSize: formPlan.storageOption.size,
      transactionalEmailSize: formPlan.transactionalEmailOption.size,
      emailCampaignSize: formPlan.emailCampaignOption.size,
    };

    this.promoCodeForm.reset();

    if (this.subscription) {
      let previousSubscriptionPrice =
        (this.subscription.transactionalEmailPrice +
          this.subscription.storagePrice +
          this.subscription.emailCampaignPrice) *
        (this.subscription.mode === SubscriptionMode.MONTHLY ? 1 : 12);
      if (this.subscription.mode === SubscriptionMode.ANNUALLY) {
        previousSubscriptionPrice = previousSubscriptionPrice * 0.9;
      }
      const currentSubscriptionPrice = formPlan.totalPrice;

      const isLowerPricePlan = currentSubscriptionPrice < previousSubscriptionPrice;
      this.isUpgradePlan = !isLowerPricePlan;
      this.switchPlanText =
        'Your current plan will be cancelled and replaced with a new plan.\n\n' +
        (isLowerPricePlan
          ? 'The previous subscription fee will be forfeited if the new subscription is at a lower price than the current subscription.'
          : 'The previous subscription fee will be prorated and offset to new subscription fees if is at a higher price than current subscription.');

      this.switchPlanModalVisible = true;
    } else {
      if (this.planId === 1) {
        this.store.dispatch(new UpdatePlan(this.planRequest!));
      } else {
        this.newPlanModalVisible = true;
      }
    }
  }

  doSwitchPlan() {
    this.switchPlanModalVisible = false;
    this.store.dispatch(
      new PreviewPlanSwitch({
        ...this.planRequest!,
        promoCode: this.promoCodeForm.get('code')?.value || undefined,
      })
    );
  }

  doNewPlan() {
    this.newPlanModalVisible = false;
    this.planRequest = {
      ...this.planRequest!,
      promoCode: this.promoCodeForm.get('code')?.value || undefined,
    };
    this.store.dispatch(new PreviewPlanSwitch(this.planRequest));
  }

  updatePlansPrice(mode: SubscriptionMode) {
    this.form.get('mode')!.setValue(mode);
    for (let control of this.formPlans.controls) {
      this.updatePlanPrice(control);
    }
    this.cd.markForCheck();
  }

  updatePlanPrice(planForm: AbstractControl) {
    let totalPrice = 0;

    if (planForm.get('storageOption')?.value) {
      totalPrice += planForm.get('storageOption')?.value.price;
    }
    if (planForm.get('transactionalEmailOption')?.value) {
      totalPrice += planForm.get('transactionalEmailOption')?.value.price;
    }
    if (planForm.get('emailCampaignOption')?.value) {
      totalPrice += planForm.get('emailCampaignOption')?.value.price;
    }

    if (this.form.get('mode')?.value === SubscriptionMode.ANNUALLY) {
      totalPrice = totalPrice * 12 * 0.9;
    }

    planForm.get('totalPrice')?.setValue(totalPrice);
  }

  doAddPlan(plan: PlanModel): void {
    const planForm = this.fb.group({
      storageOption: [plan.storageOptions.length > 0 ? plan.storageOptions[0] : undefined, [Validators.required]],
      transactionalEmailOption: [
        plan.transactionalEmailOptions.length > 0 ? plan.transactionalEmailOptions[0] : undefined,
        [Validators.required],
      ],
      emailCampaignOption: [
        plan.emailCampaignOptions.length > 0 ? plan.emailCampaignOptions[0] : undefined,
        [Validators.required],
      ],
      totalPrice: [0, [Validators.required]],
    });
    this.updatePlanPrice(planForm);
    this.formPlans.push(planForm);
  }

  formatBytes(bytes: number) {
    return prettyBytes(bytes, { locale: true });
  }

  doEmailSupport() {
    const mail = document.createElement('a');
    mail.href = `mailto:${environment.config.supportEmail}`;
    mail.click();
  }

  get subscriptionMode() {
    return SubscriptionMode;
  }

  isPaidPlanEnabled() {
    return environment.config.isPaidPlanEnabled;
  }

  doCloseSwitchPlanModal() {
    this.switchPlanModalVisible = false;
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doCloseNewPlanModal() {
    this.newPlanModalVisible = false;
  }
}
