import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { IconsProviderModule } from './icons-provider.module';
import { en_US, NZ_I18N } from 'ng-zorro-antd/i18n';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { AuthApi } from '../api/auth.api';
import { AdminAuthService } from '../services/admin-auth.service';
import { NgxsModule } from '@ngxs/store';
import { AdminCommonState } from '../states/admin-common/admin-common.state';
import { AdminAuthState } from '../states/admin-auth/admin-auth.state';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgxsRouterPluginModule } from '@ngxs/router-plugin';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzStatisticModule } from 'ng-zorro-antd/statistic';
import { AdminForgetPasswordComponent } from './components/admin-forget-password/admin-forget-password.component';
import { AdminResetPasswordComponent } from './components/reset-password/admin-reset-password.component';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzMessageModule } from 'ng-zorro-antd/message';
import { AdminAuditListComponent } from './components/admin-audit-list/admin-audit-list.component';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { AdminUserListComponent } from './components/admin-user-list/admin-user-list.component';
import { AdminLoginComponent } from './components/admin-login/admin-login.component';
import { AdminSidebarLayoutComponent } from './layouts/admin-sidebar-layout/admin-sidebar-layout.component';
import { AdminLeftCenterLayoutComponent } from './layouts/admin-left-center-layout/admin-left-center-layout.component';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { AdminAuditState } from '../states/admin-audit/admin-audit.state';
import { environment } from '../environments/environment';
import { AdminAuditLogApi } from '../api/admin-audit-log.api';
import { AdminAuditLogService } from '../services/admin-audit.service';
import { AdminUserApi } from '../api/admin-user.api';
import { AdminUserService } from '../services/admin-user.service';
import { AdminUserState } from '../states/admin-user/admin-user.state';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { HttpErrorInterceptor } from './interceptors/http-error.interceptor';
import { LoadingInterceptor } from './interceptors/loading.interceptor';
import { DashboardManagementApi } from '../api/dashboard-management.api';
import { DashboardManagementService } from '../services/dashboard-management.service';
import { DashboardManagementState } from '../states/dashboard-management/dashboard-management.state';
import { CountriesMapModule } from 'countries-map';
import { AdminAccountApi } from '../api/admin-account.api';
import { AdminAccountService } from '../services/admin-account.service';
import { AdminNotFoundComponent } from './components/admin-not-found/admin-not-found.component';
import { NzResultModule } from 'ng-zorro-antd/result';
import { AdminAffiliateCodeListComponent } from './components/admin-affiliate-code-list/admin-affiliate-code-list.component';
import { AffiliateCodeState } from '../states/affiliate-code/affiliate-code.state';
import { AffiliateCodeManagementApi } from '../api/affiliate-code-management.api';
import { AffiliateCodeManagementService } from '../services/affiliate-code-management.service';
import { AdminTwoFactorAuthenticationComponent } from './components/admin-two-factor-authentication/admin-two-factor-authentication.component';
import { AdminProfileDetailComponent } from './components/admin-profile-detail/admin-profile-detail.component';
import { NzListModule } from 'ng-zorro-antd/list';
import { AdminProfileState } from '../states/admin-profile/admin-profile.state';
import { AdminUserProfileApi } from '../api/admin-user-profile.api';
import { AdminUserProfileService } from '../services/admin-user-profile.service';

@NgModule({
  declarations: [
    AppComponent,
    AdminAuditListComponent,
    AdminDashboardComponent,
    AdminForgetPasswordComponent,
    AdminResetPasswordComponent,
    AdminUserListComponent,
    AdminLoginComponent,
    AdminSidebarLayoutComponent,
    AdminLeftCenterLayoutComponent,
    AdminNotFoundComponent,
    AdminAffiliateCodeListComponent,
    AdminTwoFactorAuthenticationComponent,
    AdminProfileDetailComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    BrowserAnimationsModule,
    CountriesMapModule,
    NgxsRouterPluginModule.forRoot(),
    NgxsModule.forRoot(
      [
        AdminCommonState,
        AdminAuthState,
        AdminProfileState,
        AffiliateCodeState,
        AdminAuditState,
        AdminUserState,
        DashboardManagementState,
      ],
      {
        developmentMode: !environment.production,
      }
    ),
    IconsProviderModule,
    NzDividerModule,
    NzLayoutModule,
    NzMenuModule,
    NzInputModule,
    NzButtonModule,
    NzDropDownModule,
    NzAvatarModule,
    NzCardModule,
    NzTypographyModule,
    NzProgressModule,
    NzSkeletonModule,
    NzGridModule,
    NzMessageModule,
    NzStatisticModule,
    NzFormModule,
    NzTableModule,
    NzDatePickerModule,
    NzSpinModule,
    NzDrawerModule,
    NzModalModule,
    NzResultModule,
    NzListModule,
  ],
  providers: [
    { provide: NZ_I18N, useValue: en_US },
    { provide: HTTP_INTERCEPTORS, useClass: HttpErrorInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true },
    { provide: AuthApi, useClass: AdminAuthService },
    { provide: AdminAccountApi, useClass: AdminAccountService },
    { provide: AdminAuditLogApi, useClass: AdminAuditLogService },
    { provide: AdminUserProfileApi, useClass: AdminUserProfileService },
    { provide: AdminUserApi, useClass: AdminUserService },
    { provide: DashboardManagementApi, useClass: DashboardManagementService },
    { provide: AffiliateCodeManagementApi, useClass: AffiliateCodeManagementService },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
