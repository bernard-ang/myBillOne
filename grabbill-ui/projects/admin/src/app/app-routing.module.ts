import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { AdminForgetPasswordComponent } from './components/admin-forget-password/admin-forget-password.component';
import { AdminResetPasswordComponent } from './components/reset-password/admin-reset-password.component';
import { AdminAuditListComponent } from './components/admin-audit-list/admin-audit-list.component';
import { AdminUserListComponent } from './components/admin-user-list/admin-user-list.component';
import { AdminLeftCenterLayoutComponent } from './layouts/admin-left-center-layout/admin-left-center-layout.component';
import { AdminLoginComponent } from './components/admin-login/admin-login.component';
import { AdminSidebarLayoutComponent } from './layouts/admin-sidebar-layout/admin-sidebar-layout.component';
import { AdminNotFoundComponent } from './components/admin-not-found/admin-not-found.component';
import { AdminAffiliateCodeListComponent } from './components/admin-affiliate-code-list/admin-affiliate-code-list.component';
import { AdminTwoFactorAuthenticationComponent } from './components/admin-two-factor-authentication/admin-two-factor-authentication.component';
import { AdminProfileDetailComponent } from './components/admin-profile-detail/admin-profile-detail.component';

const routes: Routes = [
  {
    path: '',
    component: AdminSidebarLayoutComponent,
    children: [
      { path: '', component: AdminDashboardComponent },
      { path: 'profile', component: AdminProfileDetailComponent },
      { path: 'audits', component: AdminAuditListComponent },
      { path: 'affiliate-codes', component: AdminAffiliateCodeListComponent },
      { path: 'admin-users', component: AdminUserListComponent },
      { path: 'account', loadChildren: () => import('./modules/account/account.module').then((m) => m.AccountModule) },
      { path: 'job', loadChildren: () => import('./modules/job/job.module').then((m) => m.JobModule) },
      { path: 'invoice', loadChildren: () => import('./modules/invoice/invoice.module').then((m) => m.InvoiceModule) },
      { path: 'promo-codes', loadChildren: () => import('./modules/promo-code/promo-code.module').then((m) => m.PromoCodeModule) },
      {
        path: 'stripe-event',
        loadChildren: () => import('./modules/stripe-event/stripe-event.module').then((m) => m.StripeEventModule),
      },
    ],
  },
  {
    path: '',
    component: AdminLeftCenterLayoutComponent,
    children: [
      { path: 'login', component: AdminLoginComponent },
      { path: 'forget-password', component: AdminForgetPasswordComponent },
      { path: 'reset-password', component: AdminResetPasswordComponent },
      { path: 'two-factor-auth', component: AdminTwoFactorAuthenticationComponent },
    ],
  },
  { path: '**', component: AdminNotFoundComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
