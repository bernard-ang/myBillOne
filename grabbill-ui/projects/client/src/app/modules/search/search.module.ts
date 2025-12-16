import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchListComponent } from './search-list/search-list.component';
import { RouterModule } from '@angular/router';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { ReactiveFormsModule } from '@angular/forms';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { PrivilegeGuard } from '../../guards/privilege.guard';
import { Privilege } from '@grabbill/lib';
import { DigitalFilingApi } from '../../../api/digital-filing.api';
import { DigitalFilingService } from '../../../services/digital-filing.service';
import { AppCommonModule } from '../app-common/app-common.module';
import { TransactionalEmailApi } from '../../../api/transactional-email.api';
import { TransactionalEmailService } from '../../../services/transactional-email.service';
import { NgxsModule } from '@ngxs/store';
import { SearchState } from '../../../states/search/search.state';
import { IndexRowFilterTableComponent } from './index-row-filter-table/index-row-filter-table.component';
import { IconsProviderModule } from '../../icons-provider.module';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { EmailCampaignApi } from '../../../api/email-campaign.api';
import { EmailCampaignService } from '../../../services/email-campaign.service';
import { ContactFieldApi } from '../../../api/contact-field.api';
import { ContactFieldService } from '../../../services/contact-field.service';
import { ContactFieldState } from '../../../states/contact-field/contact-field.state';
import { NzCardModule } from 'ng-zorro-antd/card';
import { SmsApi } from "../../../api/sms.api";
import { SmsService } from "../../../services/sms.service";
import { WhatsAppApi } from "../../../api/whatsapp.api";
import { WhatsAppService } from "../../../services/whats-app.service";
import { MultiTemplateWhatsappApi } from "../../../api/multi-template-whatsapp.api";
import { MultiTemplateWhatsappService } from "../../../services/multi-template-whatsapp.service";
import { MultiTemplateTransactionalEmailApi } from "../../../api/multi-template-transactional-email.api";
import { MultiTemplateTransactionalEmailService } from "../../../services/multi-template-transactional-email.service";

@NgModule({
  declarations: [SearchListComponent, IndexRowFilterTableComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      {
        path: '',
        component: SearchListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.SEARCH_RECORD },
      },
    ]),
    NgxsModule.forFeature([SearchState, ContactFieldState]),
    AppCommonModule,
    NzTypographyModule,
    NzButtonModule,
    NzInputModule,
    NzTagModule,
    NzTableModule,
    NzIconModule,
    NzSelectModule,
    NzEmptyModule,
    NzTableModule,
    IconsProviderModule,
    NzModalModule,
    NzFormModule,
    NzInputNumberModule,
    NzDatePickerModule,
    NzCardModule,
  ],
  providers: [
    { provide: DigitalFilingApi, useClass: DigitalFilingService },
    { provide: TransactionalEmailApi, useClass: TransactionalEmailService },
    { provide: MultiTemplateTransactionalEmailApi, useClass: MultiTemplateTransactionalEmailService },
    { provide: EmailCampaignApi, useClass: EmailCampaignService },
    { provide: SmsApi, useClass: SmsService },
    { provide: ContactFieldApi, useClass: ContactFieldService },
    { provide: WhatsAppApi, useClass: WhatsAppService },
    { provide: MultiTemplateWhatsappApi, useClass: MultiTemplateWhatsappService },
  ],
})
export class SearchModule {}
