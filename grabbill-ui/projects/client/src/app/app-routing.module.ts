import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SidebarLayoutComponent } from './layouts/sidebar-layout/sidebar-layout.component';
import { LoginComponent } from './components/login/login.component';
import { ForgetPasswordComponent } from './components/forget-password/forget-password.component';
import { LeftCenterLayoutComponent } from './layouts/left-center-layout/left-center-layout.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { ExperimentalFeatureComponent } from './components/experimental-feature/experimental-feature.component';
import { UnauthorizedComponent } from './components/unauthorized/unauthorized.component';
import { PrivilegeGuard } from './guards/privilege.guard';
import { Privilege } from '@grabbill/lib';
import { UnsubscribeEmailComponent } from './components/unsubscribe-email/unsubscribe-email.component';
import { NotFoundComponent } from './components/not-found/not-found.component';
import { LoggedInGuard } from './guards/logged-in.guard';
import { TwoFactorAuthenticationComponent } from './components/two-factor-authentication/two-factor-authentication.component';
import { ExperimentalFeatureGuard } from "./guards/experimental-feature.guard";
import { WhatsappAckComponent } from "./components/whatsapp-ack/whatsapp-ack.component";
import {
  MultiTemplateTransactionalEmailModule
} from "./modules/multi-template-transactional-email/multi-template-transactional-email.module";

const routes: Routes = [
  {
    path: '',
    component: SidebarLayoutComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: DashboardComponent,
        data: { privilege: Privilege.DASHBOARD },
        canActivate: [LoggedInGuard, PrivilegeGuard],
      },
      { path: 'experimental-feature', component: ExperimentalFeatureComponent },
      { path: 'unauthorized', component: UnauthorizedComponent },
      {
        path: 'digital-filing',
        loadChildren: () => import('./modules/digital-filing/digital-filing.module').then((m) => m.DigitalFilingModule),
      },
      {
        path: 'transactional-email',
        loadChildren: () =>
          import('./modules/transactional-email/transactional-email.module').then((m) => m.TransactionalEmailModule),
      },
      {
        path: 'mt-transactional-email',
        loadChildren: () =>
          import('./modules/multi-template-transactional-email/multi-template-transactional-email.module').then((m) => m.MultiTemplateTransactionalEmailModule),
      },
      {
        path: 'sms',
        loadChildren: () =>
          import('./modules/sms/sms.module').then((m) => m.SmsModule),
        canActivate: [ExperimentalFeatureGuard]
      },
      {
        path: 'whatsapp',
        loadChildren: () =>
          import('./modules/whatsapp/whatsapp.module').then((m) => m.WhatsappModule),
      },
      {
        path: 'mt-whatsapp',
        loadChildren: () =>
          import('./modules/multi-template-whatsapp/multi-template-whatsapp.module').then((m) => m.MultiTemplateWhatsappModule),
      },
      {
        path: 'contact',
        loadChildren: () => import('./modules/contact/contact.module').then((m) => m.ContactModule),
      },
      {
        path: 'email-campaign',
        loadChildren: () => import('./modules/email-campaign/email-campaign.module').then((m) => m.EmailCampaignModule),
      },
      {
        path: 'search',
        loadChildren: () => import('./modules/search/search.module').then((m) => m.SearchModule),
      },
      {
        path: 'report',
        loadChildren: () => import('./modules/report/report.module').then((m) => m.ReportModule),
      },
      {
        path: 'account',
        loadChildren: () => import('./modules/account/account.module').then((m) => m.AccountModule),
      },
    ],
  },
  {
    path: '',
    component: LeftCenterLayoutComponent,
    children: [
      { path: 'login', component: LoginComponent },
      { path: 'two-factor-auth', component: TwoFactorAuthenticationComponent },
      { path: 'forget-password', component: ForgetPasswordComponent },
      { path: 'reset-password', component: ResetPasswordComponent },
      { path: 'unsubscribe-email', component: UnsubscribeEmailComponent },
      { path: 'whatsapp-ack', component: WhatsappAckComponent },
    ],
  },
  {
    path: '',
    children: [
      {
        path: 'register',
        loadChildren: () => import('./modules/register/register.module').then((m) => m.RegisterModule),
      },
    ],
  },
  { path: '**', component: NotFoundComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { anchorScrolling: 'enabled' })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
