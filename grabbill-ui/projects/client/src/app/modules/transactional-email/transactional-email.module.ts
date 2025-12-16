import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TransactionalEmailListComponent } from './transactional-email-list/transactional-email-list.component';
import { TransactionalEmailDetailComponent } from './transactional-email-detail/transactional-email-detail.component';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { ReactiveFormsModule } from '@angular/forms';
import { NzStatisticModule } from 'ng-zorro-antd/statistic';
import { TransactionalEmailEditDetailComponent } from './transactional-email-edit-detail/transactional-email-edit-detail.component';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzAffixModule } from 'ng-zorro-antd/affix';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { TransactionalEmailEditActivityDetailComponent } from './transactional-email-edit-activity-detail/transactional-email-edit-activity-detail.component';
import { TransactionalEmailActivityDetailComponent } from './transactional-email-activity-detail/transactional-email-activity-detail.component';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { TransactionalEmailRecordDetailComponent } from './transactional-email-record-detail/transactional-email-record-detail.component';
import { PrivilegeGuard } from '../../guards/privilege.guard';
import { Privilege } from '@grabbill/lib';
import { TransactionalEmailApi } from '../../../api/transactional-email.api';
import { TransactionalEmailService } from '../../../services/transactional-email.service';
import { NgxsModule } from '@ngxs/store';
import { TransactionalEmailState } from '../../../states/transactional-email/transactional-email.state';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { MailServerState } from '../../../states/mail-server/mail-server.state';
import { MailServerApi } from '../../../api/mail-server.api';
import { MailServerService } from '../../../services/mail-server.service';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { AppCommonModule } from '../app-common/app-common.module';
import { NzCollapseModule } from 'ng-zorro-antd/collapse';
import { NzStepsModule } from 'ng-zorro-antd/steps';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { WhatsAppApi } from "../../../api/whatsapp.api";
import { WhatsAppService } from "../../../services/whats-app.service";
import { WhatsAppState } from "../../../states/whatsapp/whatsapp.state";
import { WhatsAppEventApi } from "../../../api/whatsapp-event.api";
import { WhatsappEventService } from "../../../services/whatsapp-event.service";
import { MultiTemplateWhatsappApi } from "../../../api/multi-template-whatsapp.api";
import { MultiTemplateWhatsappService } from "../../../services/multi-template-whatsapp.service";

@NgModule({
  declarations: [
    TransactionalEmailListComponent,
    TransactionalEmailDetailComponent,
    TransactionalEmailEditDetailComponent,
    TransactionalEmailActivityDetailComponent,
    TransactionalEmailEditActivityDetailComponent,
    TransactionalEmailRecordDetailComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      {
        path: 'list',
        component: TransactionalEmailListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_VIEW },
      },
      {
        path: 'detail/new',
        component: TransactionalEmailEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_EDIT },
      },
      {
        path: 'detail/:id',
        component: TransactionalEmailDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_VIEW },
      },
      {
        path: 'detail/:id/edit',
        component: TransactionalEmailEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_EDIT },
      },
      {
        path: 'detail/:id/activity/:activityId',
        component: TransactionalEmailActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_ACTIVITY_VIEW },
      },
      {
        path: 'detail/:id/activity/:activityId/edit',
        component: TransactionalEmailEditActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_ACTIVITY_EDIT },
      },
      {
        path: 'detail/:id/activity/:activityId/record/:recordId',
        component: TransactionalEmailRecordDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_ACTIVITY_VIEW },
      },
    ]),
    NzAvatarModule,
    NzFormModule,
    NzTypographyModule,
    NzListModule,
    NzDividerModule,
    NzInputModule,
    NzIconModule,
    NzButtonModule,
    NzSelectModule,
    NzStatisticModule,
    NzSwitchModule,
    NzInputNumberModule,
    NzAffixModule,
    NzTableModule,
    NzCheckboxModule,
    NzTabsModule,
    NzDescriptionsModule,
    NzUploadModule,
    NzPaginationModule,
    NzBreadCrumbModule,
    NzTagModule,
    NzEmptyModule,
    NzModalModule,
    NzDatePickerModule,
    NzSpinModule,
    NgxsModule.forFeature([TransactionalEmailState, MailServerState, WhatsAppState]),
    NzDrawerModule,
    NzToolTipModule,
    AppCommonModule,
    NzCollapseModule,
    NzStepsModule,
    NzCardModule,
    NzSkeletonModule,
  ],
  providers: [
    { provide: TransactionalEmailApi, useClass: TransactionalEmailService },
    { provide: MailServerApi, useClass: MailServerService },
    { provide: WhatsAppApi, useClass: WhatsAppService },
    { provide: WhatsAppEventApi, useClass: WhatsappEventService },
    { provide: MultiTemplateWhatsappApi, useClass: MultiTemplateWhatsappService },
  ],
})
export class TransactionalEmailModule {}
