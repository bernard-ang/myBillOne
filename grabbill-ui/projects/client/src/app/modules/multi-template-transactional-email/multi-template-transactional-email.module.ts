import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
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
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzAffixModule } from 'ng-zorro-antd/affix';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NgxsModule } from '@ngxs/store';
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
import { WhatsAppApi } from '../../../api/whatsapp.api';
import { WhatsAppService } from '../../../services/whats-app.service';
import { WhatsAppState } from '../../../states/whatsapp/whatsapp.state';
import { WhatsAppEventApi } from '../../../api/whatsapp-event.api';
import { WhatsappEventService } from '../../../services/whatsapp-event.service';
import { MultiTemplateWhatsappApi } from '../../../api/multi-template-whatsapp.api';
import { MultiTemplateWhatsappService } from '../../../services/multi-template-whatsapp.service';
import { MultiTemplateTransactionalEmailApi } from '../../../api/multi-template-transactional-email.api';
import { MultiTemplateTransactionalEmailService } from '../../../services/multi-template-transactional-email.service';
import { MultiTemplateTransactionalEmailState } from '../../../states/multi-template-transactional-email/multi-template-transactional-email.state';
import { MultiTemplateTransactionalEmailListComponent } from './multi-template-transactional-email-list/multi-template-transactional-email-list.component';
import { PrivilegeGuard } from '../../guards/privilege.guard';
import { Privilege } from '@grabbill/lib';
import { MultiTemplateTransactionalEmailEditDetailComponent } from './multi-template-transactional-email-edit-detail/multi-template-transactional-email-edit-detail.component';
import { MultiTemplateTransactionalEmailDetailComponent } from './multi-template-transactional-email-detail/multi-template-transactional-email-detail.component';
import { MultiTemplateTransactionalEmailEditActivityDetailComponent } from './multi-template-transactional-email-edit-activity-detail/multi-template-transactional-email-edit-activity-detail.component';
import { MultiTemplateTransactionalEmailActivityDetailComponent } from './multi-template-transactional-email-activity-detail/multi-template-transactional-email-activity-detail.component';
import { MultiTemplateTransactionalEmailRecordDetailComponent } from './multi-template-transactional-email-record-detail/multi-template-transactional-email-record-detail.component';

@NgModule({
  declarations: [
    MultiTemplateTransactionalEmailListComponent,
    MultiTemplateTransactionalEmailEditDetailComponent,
    MultiTemplateTransactionalEmailDetailComponent,
    MultiTemplateTransactionalEmailActivityDetailComponent,
    MultiTemplateTransactionalEmailEditActivityDetailComponent,
    MultiTemplateTransactionalEmailRecordDetailComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      {
        path: 'list',
        component: MultiTemplateTransactionalEmailListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_VIEW },
      },
      {
        path: 'detail/new',
        component: MultiTemplateTransactionalEmailEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_EDIT },
      },
      {
        path: 'detail/:id',
        component: MultiTemplateTransactionalEmailDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_VIEW },
      },
      {
        path: 'detail/:id/edit',
        component: MultiTemplateTransactionalEmailEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_EDIT },
      },
      {
        path: 'detail/:id/activity/:activityId/edit',
        component: MultiTemplateTransactionalEmailEditActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_ACTIVITY_EDIT },
      },
      {
        path: 'detail/:id/activity/:activityId',
        component: MultiTemplateTransactionalEmailActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.TRX_EMAIL_ACTIVITY_VIEW },
      },
      {
        path: 'detail/:id/activity/:activityId/record/:recordId',
        component: MultiTemplateTransactionalEmailRecordDetailComponent,
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
    NgxsModule.forFeature([MultiTemplateTransactionalEmailState, MailServerState, WhatsAppState]),
    NzDrawerModule,
    NzToolTipModule,
    AppCommonModule,
    NzCollapseModule,
    NzStepsModule,
    NzCardModule,
    NzSkeletonModule,
  ],
  providers: [
    { provide: MultiTemplateTransactionalEmailApi, useClass: MultiTemplateTransactionalEmailService },
    { provide: MailServerApi, useClass: MailServerService },
    { provide: WhatsAppApi, useClass: WhatsAppService },
    { provide: WhatsAppEventApi, useClass: WhatsappEventService },
    { provide: MultiTemplateWhatsappApi, useClass: MultiTemplateWhatsappService },
  ],
})
export class MultiTemplateTransactionalEmailModule {}
