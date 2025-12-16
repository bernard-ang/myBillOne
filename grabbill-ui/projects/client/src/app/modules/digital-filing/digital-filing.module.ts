import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DigitalFilingListComponent } from './digital-filing-list/digital-filing-list.component';
import { DigitalFilingDetailComponent } from './digital-filing-detail/digital-filing-detail.component';
import { DigitalFilingEditDetailComponent } from './digital-filing-edit-detail/digital-filing-edit-detail.component';
import { DigitalFilingEditActivityDetailComponent } from './digital-filing-edit-activity-detail/digital-filing-edit-activity-detail.component';
import { DigitalFilingActivityDetailComponent } from './digital-filing-activity-detail/digital-filing-activity-detail.component';
import { RouterModule } from '@angular/router';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { ReactiveFormsModule } from '@angular/forms';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzStatisticModule } from 'ng-zorro-antd/statistic';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { NzAffixModule } from 'ng-zorro-antd/affix';
import { DigitalFilingApi } from '../../../api/digital-filing.api';
import { DigitalFilingService } from '../../../services/digital-filing.service';
import { NgxsModule } from '@ngxs/store';
import { DigitalFilingState } from '../../../states/digital-filing/digital-filing.state';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { PrivilegeGuard } from '../../guards/privilege.guard';
import { Privilege } from '@grabbill/lib';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzCollapseModule } from 'ng-zorro-antd/collapse';
import { NzStepsModule } from 'ng-zorro-antd/steps';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { AppCommonModule } from '../app-common/app-common.module';
import { NzCardModule } from 'ng-zorro-antd/card';

@NgModule({
  declarations: [
    DigitalFilingListComponent,
    DigitalFilingDetailComponent,
    DigitalFilingEditDetailComponent,
    DigitalFilingEditActivityDetailComponent,
    DigitalFilingActivityDetailComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      {
        path: 'list',
        component: DigitalFilingListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.DGTL_FILING_VIEW },
      },
      {
        path: 'detail/new',
        component: DigitalFilingEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.DGTL_FILING_EDIT },
      },
      {
        path: 'detail/:id',
        component: DigitalFilingDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.DGTL_FILING_VIEW },
      },
      {
        path: 'detail/:id/edit',
        component: DigitalFilingEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.DGTL_FILING_EDIT },
      },

      {
        path: 'detail/:id/activity/:activityId',
        component: DigitalFilingActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.DGTL_FILING_ACTIVITY_VIEW },
      },
      {
        path: 'detail/:id/activity/:activityId/edit',
        component: DigitalFilingEditActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.DGTL_FILING_ACTIVITY_EDIT },
      },
    ]),
    NzAvatarModule,
    NzListModule,
    NzSelectModule,
    NzInputModule,
    NzDividerModule,
    NzTypographyModule,
    NzIconModule,
    NzButtonModule,
    NzDescriptionsModule,
    NzTableModule,
    NzBreadCrumbModule,
    NzStatisticModule,
    NzTabsModule,
    NzTagModule,
    NzFormModule,
    NzSwitchModule,
    NzInputNumberModule,
    NzCheckboxModule,
    NzUploadModule,
    NzPaginationModule,
    NzAffixModule,
    NgxsModule.forFeature([DigitalFilingState]),
    NzEmptyModule,
    NzModalModule,
    NzDrawerModule,
    NzDatePickerModule,
    NzSpinModule,
    NzToolTipModule,
    NzCollapseModule,
    NzStepsModule,
    NzSkeletonModule,
    AppCommonModule,
    NzCardModule,
  ],
  providers: [{ provide: DigitalFilingApi, useClass: DigitalFilingService }],
})
export class DigitalFilingModule {}
