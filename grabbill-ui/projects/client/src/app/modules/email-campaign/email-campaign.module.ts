import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmailCampaignListComponent } from './email-campaign-list/email-campaign-list.component';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { EmailCampaignEditDetailComponent } from './email-campaign-edit-detail/email-campaign-edit-detail.component';
import { EmailCampaignDetailComponent } from './email-campaign-detail/email-campaign-detail.component';
import { EmailCampaignActivityDetailComponent } from './email-campaign-activity-detail/email-campaign-activity-detail.component';
import { EmailCampaignEditActivityDetailComponent } from './email-campaign-edit-activity-detail/email-campaign-edit-activity-detail.component';
import { EmailCampaignRecordDetailComponent } from './email-campaign-record-detail/email-campaign-record-detail.component';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzStatisticModule } from 'ng-zorro-antd/statistic';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { PrivilegeGuard } from '../../guards/privilege.guard';
import { Privilege } from '@grabbill/lib';
import { EmailCampaignApi } from '../../../api/email-campaign.api';
import { EmailCampaignService } from '../../../services/email-campaign.service';
import { MailServerApi } from '../../../api/mail-server.api';
import { MailServerService } from '../../../services/mail-server.service';
import { NgxsModule } from '@ngxs/store';
import { EmailCampaignState } from '../../../states/email-campaign/email-campaign.state';
import { MailServerState } from '../../../states/mail-server/mail-server.state';
import { AppCommonModule } from '../app-common/app-common.module';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { ContactGroupState } from '../../../states/contact-group/contact-group.state';
import { ContactFieldState } from '../../../states/contact-field/contact-field.state';
import { ContactGroupApi } from '../../../api/contact-group.api';
import { ContactGroupService } from '../../../services/contact-group.service';
import { ContactFieldApi } from '../../../api/contact-field.api';
import { ContactFieldService } from '../../../services/contact-field.service';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzStepsModule } from 'ng-zorro-antd/steps';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';

@NgModule({
  declarations: [
    EmailCampaignListComponent,
    EmailCampaignEditDetailComponent,
    EmailCampaignDetailComponent,
    EmailCampaignActivityDetailComponent,
    EmailCampaignEditActivityDetailComponent,
    EmailCampaignRecordDetailComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      {
        path: 'list',
        component: EmailCampaignListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.EMAIL_CAMPAIGN_VIEW },
      },
      {
        path: 'detail/new',
        component: EmailCampaignEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.EMAIL_CAMPAIGN_EDIT },
      },
      {
        path: 'detail/:id',
        component: EmailCampaignDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.EMAIL_CAMPAIGN_VIEW },
      },
      {
        path: 'detail/:id/edit',
        component: EmailCampaignEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.EMAIL_CAMPAIGN_EDIT },
      },
      {
        path: 'detail/:id/activity/:activityId',
        component: EmailCampaignActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.EMAIL_CAMPAIGN_ACTIVITY_VIEW },
      },
      {
        path: 'detail/:id/activity/:activityId/edit',
        component: EmailCampaignEditActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.EMAIL_CAMPAIGN_ACTIVITY_EDIT },
      },
      {
        path: 'detail/:id/activity/:activityId/record/:recordId',
        component: EmailCampaignRecordDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.EMAIL_CAMPAIGN_ACTIVITY_VIEW },
      },
    ]),
    NgxsModule.forFeature([EmailCampaignState, MailServerState, ContactGroupState, ContactFieldState]),
    NzListModule,
    NzInputModule,
    NzSelectModule,
    NzAvatarModule,
    NzTypographyModule,
    NzIconModule,
    NzDividerModule,
    NzButtonModule,
    NzBreadCrumbModule,
    NzFormModule,
    NzSwitchModule,
    NzInputNumberModule,
    NzTableModule,
    NzCheckboxModule,
    NzDescriptionsModule,
    NzTabsModule,
    NzTagModule,
    NzStatisticModule,
    NzUploadModule,
    NzPaginationModule,
    AppCommonModule,
    NzEmptyModule,
    NzSkeletonModule,
    NzDrawerModule,
    NzSpinModule,
    NzCardModule,
    NzModalModule,
    NzDatePickerModule,
    NzStepsModule,
    NzToolTipModule,
  ],
  providers: [
    { provide: EmailCampaignApi, useClass: EmailCampaignService },
    { provide: MailServerApi, useClass: MailServerService },
    { provide: ContactGroupApi, useClass: ContactGroupService },
    { provide: ContactFieldApi, useClass: ContactFieldService },
  ],
})
export class EmailCampaignModule {}
