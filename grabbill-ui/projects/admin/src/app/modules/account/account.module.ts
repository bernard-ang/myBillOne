import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminAccountListComponent } from './admin-account-list/admin-account-list.component';
import { AdminAccountDetailComponent } from './admin-account-detail/admin-account-detail.component';
import { RouterModule } from '@angular/router';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzInputModule } from 'ng-zorro-antd/input';
import { IconsProviderModule } from '../../icons-provider.module';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NgxsModule } from '@ngxs/store';
import { AccountManagementState } from '../../../states/account-management/account-management.state';
import { AccountManagementApi } from '../../../api/account-management.api';
import { AccountManagementService } from '../../../services/account-management.service';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { AdminPlanApi } from '../../../api/admin-plan.api';
import { AdminPlanService } from '../../../services/admin-plan.service';
import { AdminPlanState } from '../../../states/admin-plan/admin-plan.state';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { ReactiveFormsModule } from '@angular/forms';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzMessageModule } from "ng-zorro-antd/message";

@NgModule({
  declarations: [AdminAccountListComponent, AdminAccountDetailComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      { path: 'list', component: AdminAccountListComponent },
      { path: 'detail/:id', component: AdminAccountDetailComponent },
    ]),
    NgxsModule.forFeature([AccountManagementState, AdminPlanState]),
    NzMessageModule,
    IconsProviderModule,
    NzButtonModule,
    NzInputModule,
    NzTypographyModule,
    NzTableModule,
    NzTabsModule,
    NzTagModule,
    NzListModule,
    NzAvatarModule,
    NzDescriptionsModule,
    NzBreadCrumbModule,
    NzDividerModule,
    NzCardModule,
    NzEmptyModule,
    NzProgressModule,
    NzSkeletonModule,
    NzToolTipModule,
    NzModalModule,
    NzFormModule,
    NzSelectModule,
    ReactiveFormsModule,
    NzDatePickerModule,
  ],
  providers: [
    { provide: AccountManagementApi, useClass: AccountManagementService },
    { provide: AdminPlanApi, useClass: AdminPlanService },
  ],
})
export class AccountModule {}
