import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StripeEventListComponent } from './strie-event-list/stripe-event-list.component';
import { StripEventDetailComponent } from './stripe-event-detail/strip-event-detail.component';
import { RouterModule } from '@angular/router';
import { StripeManagementState } from '../../../states/stripe-management/stripe-management.state';
import { ReactiveFormsModule } from '@angular/forms';
import { IconsProviderModule } from '../../icons-provider.module';
import { NgxsModule } from '@ngxs/store';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { StripeEventManagementApi } from '../../../api/stripe-event-management.api';
import { StripeEventManagementService } from '../../../services/stripe-event-management.service';

@NgModule({
  declarations: [StripeEventListComponent, StripEventDetailComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      { path: 'list', component: StripeEventListComponent },
      { path: 'detail/:id', component: StripEventDetailComponent },
    ]),
    NgxsModule.forFeature([StripeManagementState]),
    ReactiveFormsModule,
    IconsProviderModule,
    NzButtonModule,
    NzInputModule,
    NzDatePickerModule,
    NzTagModule,
    NzTableModule,
    NzModalModule,
    NzFormModule,
    NzSelectModule,
    NzTypographyModule,
    NzBreadCrumbModule,
    NzSkeletonModule,
    NzDescriptionsModule,
  ],
  providers: [{ provide: StripeEventManagementApi, useClass: StripeEventManagementService }],
})
export class StripeEventModule {}
