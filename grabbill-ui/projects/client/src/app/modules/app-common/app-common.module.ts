import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmailEditorComponent } from './components/email-editor/email-editor.component';
import { SafeHtmlPipe } from './pipes/safe-html.pipe';
import { HtmlPreviewComponent } from './components/html-preview/html-preview.component';
import { ImageApi } from '../../../api/image.api';
import { ImageService } from '../../../services/image.service';
import { NgxsModule } from '@ngxs/store';
import { ImageState } from '../../../states/image/image.state';
import { DrawerFormComponent } from './components/drawer-form/drawer-form.component';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzFormModule } from 'ng-zorro-antd/form';
import { ReactiveFormsModule } from '@angular/forms';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzInputModule } from 'ng-zorro-antd/input';
import { ModalFilterFormComponent } from './components/modal-fliter-form/modal-filter-form.component';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { PageHeaderComponent } from './components/page-header/page-header.component';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { RouterModule } from '@angular/router';
import { IconsProviderModule } from '../../icons-provider.module';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { ListComponent } from './components/list/list.component';
import { EmailStatComponent } from './components/email-stat/email-stat.component';
import { NzStatisticModule } from 'ng-zorro-antd/statistic';
import { DescriptionsComponent } from './components/descriptions/descriptions.component';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { UsageStatComponent } from './components/usage-stat/usage-stat.component';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzProgressModule } from 'ng-zorro-antd/progress';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { BreadcrumbComponent } from './components/breadcrumb/breadcrumb.component';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { SmsStatComponent } from './components/sms-stat/sms-stat.component';
import { WhatsappStatComponent } from './components/whatsapp-stat/whatsapp-stat.component';

@NgModule({
  declarations: [
    EmailEditorComponent,
    SafeHtmlPipe,
    HtmlPreviewComponent,
    DrawerFormComponent,
    ModalFilterFormComponent,
    PageHeaderComponent,
    ListComponent,
    EmailStatComponent,
    DescriptionsComponent,
    UsageStatComponent,
    BreadcrumbComponent,
    SmsStatComponent,
    WhatsappStatComponent,
  ],
  imports: [
    CommonModule,
    RouterModule.forChild([]),
    NgxsModule.forFeature([ImageState]),
    NzDrawerModule,
    NzFormModule,
    ReactiveFormsModule,
    NzButtonModule,
    NzInputModule,
    NzInputNumberModule,
    NzDatePickerModule,
    NzModalModule,
    NzSelectModule,
    NzTypographyModule,
    NzDividerModule,
    IconsProviderModule,
    NzListModule,
    NzAvatarModule,
    NzEmptyModule,
    NzStatisticModule,
    NzDescriptionsModule,
    NzCardModule,
    NzProgressModule,
    NzTagModule,
    NzBreadCrumbModule,
  ],
  exports: [
    EmailEditorComponent,
    SafeHtmlPipe,
    HtmlPreviewComponent,
    DrawerFormComponent,
    ModalFilterFormComponent,
    PageHeaderComponent,
    ListComponent,
    EmailStatComponent,
    DescriptionsComponent,
    UsageStatComponent,
    BreadcrumbComponent,
    SmsStatComponent,
    WhatsappStatComponent
  ],
  providers: [{ provide: ImageApi, useClass: ImageService }],
})
export class AppCommonModule {}
