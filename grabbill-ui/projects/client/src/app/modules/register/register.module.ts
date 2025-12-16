import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { RegisterUserComponent } from './register-user/register-user.component';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { IconsProviderModule } from '../../icons-provider.module';
import { RegisterVerifyEmailComponent } from './register-verify-email/register-verify-email.component';
import { RegisterAccountComponent } from './register-account/register-account.component';
import { RegisterPlanSelectionComponent } from './register-plan-selection/register-plan-selection.component';
import { LeftCenterLayoutComponent } from '../../layouts/left-center-layout/left-center-layout.component';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { CenterLayoutComponent } from '../../layouts/center-layout/center-layout.component';
import { NgxsModule } from '@ngxs/store';
import { AccountState } from '../../../states/account/account.state';
import { AccountApi } from '../../../api/account.api';
import { AccountService } from '../../../services/account.service';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { PlanState } from '../../../states/plan/plan.state';
import { PlanApi } from '../../../api/plan.api';
import { PlanService } from '../../../services/plan.service';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { LoggedInGuard } from '../../guards/logged-in.guard';
import { PaymentState } from '../../../states/payment/payment.state';
import { RegisterPaymentMethodSetupSummaryComponent } from './register-payment-method-setup-summary/register-payment-method-setup-summary.component';
import { NzResultModule } from 'ng-zorro-antd/result';
import { BillingInfoApi } from '../../../api/billing-info.api';
import { BillingInfoService } from '../../../services/billing-info.service';
import { RegisterSwitchPlanSummaryComponent } from './register-switch-plan-summary/register-switch-plan-summary.component';
import { StripeSessionApi } from '../../../api/stripe-session.api';
import { StripeSessionService } from '../../../services/stripe-session.service';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzModalModule } from "ng-zorro-antd/modal";

@NgModule({
  declarations: [
    RegisterUserComponent,
    RegisterVerifyEmailComponent,
    RegisterAccountComponent,
    RegisterPlanSelectionComponent,
    RegisterPaymentMethodSetupSummaryComponent,
    RegisterSwitchPlanSummaryComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NgxsModule.forFeature([AccountState, PlanState, PaymentState]),
    RouterModule.forChild([
      {
        path: '',
        component: LeftCenterLayoutComponent,
        children: [
          { path: 'user', component: RegisterUserComponent },
          { path: 'verify-email', component: RegisterVerifyEmailComponent },
          { path: 'account', component: RegisterAccountComponent, canActivate: [LoggedInGuard] },
        ],
      },
      {
        path: '',
        component: CenterLayoutComponent,
        children: [
          { path: 'switch-plan', component: RegisterSwitchPlanSummaryComponent, canActivate: [LoggedInGuard] },
          {
            path: 'payment-method-summary',
            component: RegisterPaymentMethodSetupSummaryComponent,
            canActivate: [LoggedInGuard],
          },
        ],
      },
      {
        path: '',
        children: [
          {
            path: 'plan-selection',
            component: RegisterPlanSelectionComponent,
            canActivate: [LoggedInGuard],
          },
        ],
      },
    ]),
    NzFormModule,
    NzInputModule,
    NzCheckboxModule,
    NzButtonModule,
    NzDividerModule,
    IconsProviderModule,
    NzRadioModule,
    NzSelectModule,
    NzTableModule,
    NzSkeletonModule,
    NzResultModule,
    NzSpinModule,
    NzModalModule,
  ],
  providers: [
    { provide: AccountApi, useClass: AccountService },
    { provide: PlanApi, useClass: PlanService },
    { provide: BillingInfoApi, useClass: BillingInfoService },
    { provide: StripeSessionApi, useClass: StripeSessionService },
  ],
})
export class RegisterModule {}
