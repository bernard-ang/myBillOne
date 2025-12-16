import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";

import en from "@angular/common/locales/en";
import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";
import { en_US, NZ_I18N } from "ng-zorro-antd/i18n";
import { registerLocaleData } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { HTTP_INTERCEPTORS, HttpClientModule } from "@angular/common/http";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { IconsProviderModule } from "./icons-provider.module";
import { NzLayoutModule } from "ng-zorro-antd/layout";
import { NzMenuModule } from "ng-zorro-antd/menu";
import { SidebarLayoutComponent } from "./layouts/sidebar-layout/sidebar-layout.component";
import { DashboardComponent } from "./components/dashboard/dashboard.component";
import { NzAvatarModule } from "ng-zorro-antd/avatar";
import { NzDropDownModule } from "ng-zorro-antd/dropdown";
import { NzDividerModule } from "ng-zorro-antd/divider";
import { NzTypographyModule } from "ng-zorro-antd/typography";
import { NgxChartsModule } from "@swimlane/ngx-charts";
import { NzProgressModule } from "ng-zorro-antd/progress";
import { NzSkeletonModule } from "ng-zorro-antd/skeleton";
import { NzMessageModule } from "ng-zorro-antd/message";
import { NgxsModule } from "@ngxs/store";
import { CommonState } from "../states/common/common.state";
import { LoginComponent } from "./components/login/login.component";
import { NzFormModule } from "ng-zorro-antd/form";
import { NzInputModule } from "ng-zorro-antd/input";
import { NzCheckboxModule } from "ng-zorro-antd/checkbox";
import { NzButtonModule } from "ng-zorro-antd/button";
import { ForgetPasswordComponent } from "./components/forget-password/forget-password.component";
import { LeftCenterLayoutComponent } from "./layouts/left-center-layout/left-center-layout.component";
import { NgxsRouterPluginModule } from "@ngxs/router-plugin";
import { CenterLayoutComponent } from "./layouts/center-layout/center-layout.component";
import { ResetPasswordComponent } from "./components/reset-password/reset-password.component";
import { NzDatePickerModule } from "ng-zorro-antd/date-picker";
import { NzTableModule } from "ng-zorro-antd/table";
import { AuthApi } from "../api/auth.api";
import { AuthService } from "../services/auth.service";
import { NzCardModule } from "ng-zorro-antd/card";
import { AuthState } from "../states/auth/auth.state";
import { HttpErrorInterceptor } from "./interceptors/http-error.interceptor";
import { NzSpinModule } from "ng-zorro-antd/spin";
import { ApiHttpService } from "../services/api-http.service";
import { environment } from "../environments/environment";
import { ExperimentalFeatureGuard } from "./guards/experimental-feature.guard";
import { ExperimentalFeatureComponent } from "./components/experimental-feature/experimental-feature.component";
import { NzResultModule } from "ng-zorro-antd/result";
import { UnauthorizedComponent } from "./components/unauthorized/unauthorized.component";
import { PrivilegeGuard } from "./guards/privilege.guard";
import { AccountApi } from "../api/account.api";
import { AccountService } from "../services/account.service";
import { UnsubscribeEmailComponent } from "./components/unsubscribe-email/unsubscribe-email.component";
import { NzRadioModule } from "ng-zorro-antd/radio";
import { UnsubscribeEmailState } from "../states/unsubscribe-email/unsubscribe-email.state";
import { EmailUnsubscriptionApi } from "../api/email-unsubscription.api";
import { EmailUnsubscriptionService } from "../services/email-unsubscription.service";
import { DashboardApi } from "../api/dashboard.api";
import { DashbordService } from "../services/dashbord.service";
import { DashboardState } from "../states/dashboard/dashboard.state";
import { NzListModule } from "ng-zorro-antd/list";
import { NzEmptyModule } from "ng-zorro-antd/empty";
import { NzModalModule } from "ng-zorro-antd/modal";
import { LoadingInterceptor } from "./interceptors/loading.interceptor";
import { NotFoundComponent } from "./components/not-found/not-found.component";
import { NzAlertModule } from "ng-zorro-antd/alert";
import { AppCommonModule } from "./modules/app-common/app-common.module";
import { MarkdownModule } from "ngx-markdown";
import { HelpModalComponent } from "./components/help-modal/help-modal.component";
import { SafePipe } from "./pipes/safe.pipe";
import { ProfileState } from "../states/profile/profile.state";
import { ProfileApi } from "../api/profile.api";
import { ProfileService } from "../services/profile.service";
import { NzSpaceModule } from "ng-zorro-antd/space";
import {
  TwoFactorAuthenticationComponent
} from "./components/two-factor-authentication/two-factor-authentication.component";
import { AccountState } from "../states/account/account.state";
import { WhatsappAckComponent } from "./components/whatsapp-ack/whatsapp-ack.component";
import { NzToolTipModule } from "ng-zorro-antd/tooltip";

registerLocaleData(en);

@NgModule({
  declarations: [
    AppComponent,
    SidebarLayoutComponent,
    DashboardComponent,
    LoginComponent,
    ForgetPasswordComponent,
    LeftCenterLayoutComponent,
    CenterLayoutComponent,
    ResetPasswordComponent,
    ExperimentalFeatureComponent,
    UnauthorizedComponent,
    UnsubscribeEmailComponent,
    NotFoundComponent,
    HelpModalComponent,
    SafePipe,
    TwoFactorAuthenticationComponent,
    WhatsappAckComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    BrowserAnimationsModule,
    NgxsRouterPluginModule.forRoot(),
    NgxsModule.forRoot([ CommonState, ProfileState, AuthState, AccountState, UnsubscribeEmailState, DashboardState ], {
      developmentMode: !environment.production
    }),
    IconsProviderModule,
    NzAvatarModule,
    NzDividerModule,
    NzDropDownModule,
    NzLayoutModule,
    NzMessageModule,
    NzMenuModule,
    NzProgressModule,
    NzSkeletonModule,
    NzTypographyModule,
    NgxChartsModule,
    NzFormModule,
    NzInputModule,
    NzCheckboxModule,
    NzButtonModule,
    NzDatePickerModule,
    NzTableModule,
    NzCardModule,
    NzSpinModule,
    NzResultModule,
    NzRadioModule,
    NzListModule,
    NzEmptyModule,
    NzModalModule,
    NzAlertModule,
    AppCommonModule,
    MarkdownModule.forRoot(),
    NzSpaceModule,
    NzToolTipModule
  ],
  providers: [
    ApiHttpService,
    ExperimentalFeatureGuard,
    PrivilegeGuard,
    { provide: NZ_I18N, useValue: en_US },
    { provide: HTTP_INTERCEPTORS, useClass: HttpErrorInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true },
    { provide: AuthApi, useClass: AuthService },
    { provide: ProfileApi, useClass: ProfileService },
    { provide: AccountApi, useClass: AccountService },
    { provide: EmailUnsubscriptionApi, useClass: EmailUnsubscriptionService },
    { provide: DashboardApi, useClass: DashbordService },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
