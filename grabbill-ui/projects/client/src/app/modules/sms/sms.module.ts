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
import { PrivilegeGuard } from '../../guards/privilege.guard';
import { Privilege } from '@grabbill/lib';
import { NgxsModule } from '@ngxs/store';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { AppCommonModule } from '../app-common/app-common.module';
import { NzCollapseModule } from 'ng-zorro-antd/collapse';
import { NzStepsModule } from 'ng-zorro-antd/steps';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { SmsApi } from '../../../api/sms.api';
import { SmsService } from '../../../services/sms.service';
import { SmsState } from '../../../states/sms/sms.state';
import { SmsTypeDetailComponent } from './sms-type-detail/sms-type-detail.component';
import { SmsTypeListComponent } from './sms-type-list/sms-type-list.component';
import { SmsTypeActivityDetailComponent } from './sms-type-activity-detail/sms-type-activity-detail.component';
import { SmsTypeEditDetailComponent } from './sms-type-edit-detail/sms-type-edit-detail.component';
import { SmsTypeEditActivityDetailComponent } from './sms-type-edit-activity-detail/sms-type-edit-activity-detail.component';
import { ContactFieldState } from '../../../states/contact-field/contact-field.state';
import { ContactFieldApi } from '../../../api/contact-field.api';
import { ContactFieldService } from '../../../services/contact-field.service';
import { ContactGroupApi } from "../../../api/contact-group.api";
import { ContactGroupService } from "../../../services/contact-group.service";
import { ContactGroupState } from "../../../states/contact-group/contact-group.state";

@NgModule({
  declarations: [
    SmsTypeDetailComponent,
    SmsTypeListComponent,
    SmsTypeActivityDetailComponent,
    SmsTypeEditDetailComponent,
    SmsTypeEditActivityDetailComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      {
        path: 'list',
        component: SmsTypeListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.SMS_VIEW },
      },
      {
        path: 'detail/new',
        component: SmsTypeEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.SMS_EDIT },
      },
      {
        path: 'detail/:id',
        component: SmsTypeDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.SMS_VIEW },
      },
      {
        path: 'detail/:id/edit',
        component: SmsTypeEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.SMS_EDIT },
      },
      {
        path: 'detail/:id/activity/:activityId',
        component: SmsTypeActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.SMS_ACTIVITY_VIEW },
      },
      {
        path: 'detail/:id/activity/:activityId/edit',
        component: SmsTypeEditActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.SMS_ACTIVITY_EDIT },
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
    NgxsModule.forFeature([SmsState, ContactFieldState, ContactGroupState]),
    NzDrawerModule,
    NzToolTipModule,
    AppCommonModule,
    NzCollapseModule,
    NzStepsModule,
    NzCardModule,
    NzSkeletonModule,
  ],
  providers: [
    { provide: SmsApi, useClass: SmsService },
    { provide: ContactFieldApi, useClass: ContactFieldService },
    { provide: ContactGroupApi, useClass: ContactGroupService },
  ],
})
export class SmsModule {}
