import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { PlanApi } from '../../../api/plan.api';
import { Subscription, tap } from 'rxjs';
import { PlanModel, SubscriptionMode } from '@grabbill/lib';
import prettyBytes from 'pretty-bytes';
import { AbstractControl, UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { capitalize } from 'lodash';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-pricing',
  templateUrl: './pricing.component.html',
  styleUrls: ['./pricing.component.less'],
})
export class PricingComponent implements OnInit, OnDestroy {
  planSubscription!: Subscription;
  plans: PlanModel[] = [];

  isLoading = false;
  form: UntypedFormGroup;

  get subscriptionModes(): SubscriptionMode[] {
    return [SubscriptionMode.MONTHLY, SubscriptionMode.ANNUALLY];
  }

  get subscriptionMode() {
    return SubscriptionMode;
  }

  get formPlans() {
    return this.form.controls['plans'] as UntypedFormArray;
  }

  capitalize(value: string) {
    return capitalize(value);
  }

  constructor(private planApi: PlanApi, private fb: UntypedFormBuilder, private cd: ChangeDetectorRef) {
    this.form = this.fb.group({
      mode: [SubscriptionMode.MONTHLY, [Validators.required]],
      plans: this.fb.array([]),
    });
  }

  ngOnInit(): void {
    this.planSubscription = this.planApi
      .getPlans()
      .pipe(
        tap((response) => {
          this.plans = response.data.plans;

          for (const plan of this.plans) {
            this.doAddPlan(plan);
            this.cd.markForCheck();
          }
          this.isLoading = false;
        })
      )
      .subscribe();
  }

  ngOnDestroy(): void {
    this.planSubscription.unsubscribe();
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
      totalPrice = Math.round(totalPrice * 12 * 0.9);
    }

    planForm.get('totalPrice')?.setValue(totalPrice);
  }

  formatBytes(bytes: number) {
    return prettyBytes(bytes, { locale: true });
  }

  doEmailSupport() {
    const mail = document.createElement('a');
    mail.href = `mailto:${environment.config.supportEmail}`;
    mail.click();
  }

  getRegisterUrl() {
    return environment.config.appRegisterUrl;
  }

  isPaidPlanEnabled() {
    return environment.config.isPaidPlanEnabled;
  }
}
