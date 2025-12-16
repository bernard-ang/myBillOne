import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InvoiceListComponent } from './invoice-list/invoice-list.component';
import { InvoiceDetailComponent } from './invoice-detail/invoice-detail.component';
import { RouterModule } from '@angular/router';
import { NgxsModule } from '@ngxs/store';
import { ReactiveFormsModule } from '@angular/forms';
import { IconsProviderModule } from '../../icons-provider.module';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { InvoiceManagementApi } from '../../../api/invoice-management.api';
import { InvoiceManagementService } from '../../../services/invoice-management.service';
import { InvoiceManagementState } from '../../../states/invoice-management/invoice-management.state';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';

@NgModule({
  declarations: [InvoiceListComponent, InvoiceDetailComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      { path: 'list', component: InvoiceListComponent },
      { path: 'detail/:id', component: InvoiceDetailComponent },
    ]),
    NgxsModule.forFeature([InvoiceManagementState]),
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
  providers: [{ provide: InvoiceManagementApi, useClass: InvoiceManagementService }],
})
export class InvoiceModule {}
