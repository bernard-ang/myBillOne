import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
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
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { PromoCodeListComponent } from './promo-code-list/promo-code-list.component';
import { PromoCodeState } from '../../../states/promo-code/promo-code-state';
import { PromoCodeManagementApi } from '../../../api/promo-code-management.api';
import { PromoCodeManagementService } from '../../../services/promo-code-management.service';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzToolTipModule } from "ng-zorro-antd/tooltip";

@NgModule({
  declarations: [PromoCodeListComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([{ path: 'list', component: PromoCodeListComponent }]),
    NgxsModule.forFeature([PromoCodeState]),
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
    NzDrawerModule,
    NzInputNumberModule,
    NzSwitchModule,
    NzToolTipModule,
  ],
  providers: [{ provide: PromoCodeManagementApi, useClass: PromoCodeManagementService }],
})
export class PromoCodeModule {}
