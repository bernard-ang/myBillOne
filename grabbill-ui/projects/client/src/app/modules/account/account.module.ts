import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileDetailComponent } from './profile-detail/profile-detail.component';
import { RouterModule } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { ReactiveFormsModule } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { AccountDetailComponent } from './account-detail/account-detail.component';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { PlanDetailComponent } from './plan-detail/plan-detail.component';
import { UserListComponent } from './user-list/user-list.component';
import { BouncedEmailListComponent } from './bounced-email-list/bounced-email-list.component';
import { UnsubscribeEmailListComponent } from './unsubscribe-email-list/unsubscribe-email-list.component';
import { ImageListComponent } from './image-list/image-list.component';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NgxsModule } from '@ngxs/store';
import { AccountState } from '../../../states/account/account.state';
import { AccountApi } from '../../../api/account.api';
import { AccountService } from '../../../services/account.service';
import { RoleApi } from '../../../api/role.api';
import { RoleService } from '../../../services/role.service';
import { UserApi } from '../../../api/user.api';
import { UserService } from '../../../services/user.service';
import { RoleState } from '../../../states/role/role.state';
import { UserState } from '../../../states/user/user.state';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { AuditListComponent } from './audit-list/audit-list.component';
import { AuditState } from '../../../states/audit/audit.state';
import { AuditLogApi } from '../../../api/audit-log.api';
import { AuditLogService } from '../../../services/audit.service';
import { Privilege } from '@grabbill/lib';
import { PrivilegeGuard } from '../../guards/privilege.guard';
import { MailServerDetailComponent } from './mail-server-detail/mail-server-detail.component';
import { MailServerApi } from '../../../api/mail-server.api';
import { MailServerService } from '../../../services/mail-server.service';
import { MailServerState } from '../../../states/mail-server/mail-server.state';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { BouncedEmailApi } from '../../../api/bounced-email.api';
import { BouncedEmailService } from '../../../services/bounced-email.service';
import { BouncedEmailState } from '../../../states/bounced-email/bounced-email.state';
import { UnsubscribedEmailState } from '../../../states/unsubscribed-email/unsubscribed-email.state';
import { UnsubscribedEmailApi } from '../../../api/unsubscribed-email.api';
import { UnsubscribedEmailService } from '../../../services/unsubscribed-email.service';
import { DashboardState } from '../../../states/dashboard/dashboard.state';
import { DashboardApi } from '../../../api/dashboard.api';
import { DashbordService } from '../../../services/dashbord.service';
import { ImageState } from '../../../states/image/image.state';
import { ImageApi } from '../../../api/image.api';
import { ImageService } from '../../../services/image.service';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzResultModule } from 'ng-zorro-antd/result';
import { PaymentState } from '../../../states/payment/payment.state';
import { InvoiceState } from '../../../states/invoice/invoice.state';
import { InvoiceApi } from '../../../api/invoice.api';
import { InvoiceService } from '../../../services/invoice.service';
import { InvoiceDetailComponent } from './invoice-detail/invoice-detail.component';
import { NzNoAnimationModule } from 'ng-zorro-antd/core/no-animation';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { AppCommonModule } from '../app-common/app-common.module';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { StripeSessionApi } from '../../../api/stripe-session.api';
import { StripeSessionService } from '../../../services/stripe-session.service';
import { SmsCreditApi } from "../../../api/sms-credit.api";
import { SmsCreditService } from "../../../services/sms-credit.service";
import { SmsCreditState } from "../../../states/sms-credit/sms-credit.state";
import { AccountSubscriptionDetailComponent } from './account-subscription-detail/account-subscription-detail.component';
import { WhatsAppApi } from "../../../api/whatsapp.api";
import { WhatsAppService } from "../../../services/whats-app.service";
import { WhatsAppState } from "../../../states/whatsapp/whatsapp.state";
import { WhatsappSettingsDetailComponent } from './whatsapp-settings-detail/whatsapp-settings-detail.component';
import { WhatsappReceivedMessageListComponent } from './whatsapp-received-message-list/whatsapp-received-message-list.component';
import { WhatsAppEventApi } from "../../../api/whatsapp-event.api";
import { WhatsappEventService } from "../../../services/whatsapp-event.service";
import { UsageListComponent } from './usage-list/usage-list.component';
import { NzStatisticModule } from "ng-zorro-antd/statistic";
import { SmsApi } from "../../../api/sms.api";
import { SmsService } from "../../../services/sms.service";
import { UsageState } from "../../../states/usage/usage.state";
import { JobListComponent } from './job-list/job-list.component';
import { JobState } from "../../../states/job/job.state";
import { JobApi } from "../../../api/job.api";
import { JobService } from "../../../services/job.service";
import { JobDetailComponent } from './job-detail/job-detail.component';
import { RoleListComponent } from './role-list/role-list.component';
import { MultiTemplateWhatsappApi } from "../../../api/multi-template-whatsapp.api";
import { MultiTemplateWhatsappService } from "../../../services/multi-template-whatsapp.service";
import { SftpDetailComponent } from "./sftp-detail/sftp-detail.component";
import { NzCollapseModule } from "ng-zorro-antd/collapse";

@NgModule({
  declarations: [
    AuditListComponent,
    ProfileDetailComponent,
    AccountDetailComponent,
    PlanDetailComponent,
    UserListComponent,
    BouncedEmailListComponent,
    UnsubscribeEmailListComponent,
    ImageListComponent,
    MailServerDetailComponent,
    InvoiceDetailComponent,
    AccountSubscriptionDetailComponent,
    WhatsappSettingsDetailComponent,
    WhatsappReceivedMessageListComponent,
    UsageListComponent,
    JobListComponent,
    JobDetailComponent,
    RoleListComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      {
        path: 'profile',
        component: ProfileDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_PROFILE },
      },
      {
        path: 'account',
        component: AccountDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_ACCOUNT },
      },
      {
        path: 'usage',
        component: UsageListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_ACCOUNT },
      },
      {
        path: 'plan',
        component: PlanDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_PLAN },
      },
      {
        path: 'users',
        component: UserListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_USER },
      },
      {
        path: 'roles',
        component: RoleListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_USER },
      },
      {
        path: 'mail-server',
        component: MailServerDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_SMTP_IMAP },
      },
      {
        path: 'sftp-settings',
        component: SftpDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_ACCOUNT },
      },
      {
        path: 'images',
        component: ImageListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_IMAGES },
      },
      {
        path: 'bounced-emails',
        component: BouncedEmailListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_BOUNCED_EMAIL },
      },
      {
        path: 'unsubscribed-emails',
        component: UnsubscribeEmailListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_UNSUBSCRIBED_EMAIL },
      },
      {
        path: 'whatsapp-settings',
        component: WhatsappSettingsDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_ACCOUNT },
      },
      {
        path: 'whatsapp-received-message',
        component: WhatsappReceivedMessageListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_ACCOUNT },
      },
      {
        path: 'audits',
        component: AuditListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.AUDIT },
      },
      {
        path: 'invoice/:id',
        component: InvoiceDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_PLAN },
      },
      {
        path: 'account-subscription/:id',
        component: AccountSubscriptionDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_PLAN },
      },
      {
        path: 'jobs',
        component: JobListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_ACCOUNT },
      },
      {
        path: 'jobs/:id',
        component: JobDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.MANAGE_ACCOUNT },
      },
    ]),
    NzBreadCrumbModule,
    NzAvatarModule,
    NzTypographyModule,
    NzFormModule,
    NzButtonModule,
    NzCollapseModule,
    NzInputModule,
    NzSelectModule,
    NzTabsModule,
    NzTagModule,
    NzTableModule,
    NzIconModule,
    NzSwitchModule,
    NzDatePickerModule,
    NzProgressModule,
    NzCardModule,
    NzCheckboxModule,
    NzEmptyModule,
    NzDescriptionsModule,
    NgxsModule.forFeature([
      AccountState,
      BouncedEmailState,
      UnsubscribedEmailState,
      RoleState,
      UserState,
      AuditState,
      MailServerState,
      DashboardState,
      ImageState,
      PaymentState,
      InvoiceState,
      JobState,
      SmsCreditState,
      UsageState,
      WhatsAppState,
    ]),
    NzDrawerModule,
    NzModalModule,
    NzInputNumberModule,
    NzListModule,
    NzUploadModule,
    NzSpinModule,
    NzSkeletonModule,
    NzResultModule,
    NzNoAnimationModule,
    NzToolTipModule,
    NzAlertModule,
    AppCommonModule,
    NzStatisticModule,
  ],
  providers: [
    { provide: AccountApi, useClass: AccountService },
    { provide: AuditLogApi, useClass: AuditLogService },
    { provide: RoleApi, useClass: RoleService },
    { provide: MailServerApi, useClass: MailServerService },
    { provide: UserApi, useClass: UserService },
    { provide: BouncedEmailApi, useClass: BouncedEmailService },
    { provide: UnsubscribedEmailApi, useClass: UnsubscribedEmailService },
    { provide: DashboardApi, useClass: DashbordService },
    { provide: ImageApi, useClass: ImageService },
    { provide: InvoiceApi, useClass: InvoiceService },
    { provide: StripeSessionApi, useClass: StripeSessionService },
    { provide: SmsCreditApi, useClass: SmsCreditService },
    { provide: WhatsAppApi, useClass: WhatsAppService },
    { provide: MultiTemplateWhatsappApi, useClass: MultiTemplateWhatsappService },
    { provide: WhatsAppEventApi, useClass: WhatsappEventService },
    { provide: SmsApi, useClass: SmsService },
    { provide: JobApi, useClass: JobService },
  ],
})
export class AccountModule {}
