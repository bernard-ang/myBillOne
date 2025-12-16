import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportDetailComponent } from './report-detail/report-detail.component';
import { DigitalFilingApi } from '../../../api/digital-filing.api';
import { DigitalFilingService } from '../../../services/digital-filing.service';
import { TransactionalEmailApi } from '../../../api/transactional-email.api';
import { TransactionalEmailService } from '../../../services/transactional-email.service';
import { EmailCampaignApi } from '../../../api/email-campaign.api';
import { EmailCampaignService } from '../../../services/email-campaign.service';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PrivilegeGuard } from '../../guards/privilege.guard';
import { Privilege } from '@grabbill/lib';
import { AppCommonModule } from '../app-common/app-common.module';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { IconsProviderModule } from '../../icons-provider.module';
import { NgxsModule } from '@ngxs/store';
import { ReportState } from '../../../states/report/report.state';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { SmsApi } from "../../../api/sms.api";
import { SmsService } from "../../../services/sms.service";
import { WhatsAppApi } from "../../../api/whatsapp.api";
import { WhatsAppService } from "../../../services/whats-app.service";
import { MultiTemplateTransactionalEmailApi } from "../../../api/multi-template-transactional-email.api";
import { MultiTemplateTransactionalEmailService } from "../../../services/multi-template-transactional-email.service";
import { MultiTemplateWhatsappApi } from "../../../api/multi-template-whatsapp.api";
import { MultiTemplateWhatsappService } from "../../../services/multi-template-whatsapp.service";

@NgModule({
  declarations: [ReportDetailComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      {
        path: '',
        component: ReportDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.SEARCH_RECORD },
      },
    ]),
    NgxsModule.forFeature([ReportState]),
    AppCommonModule,
    NzCardModule,
    NzFormModule,
    NzInputModule,
    NzButtonModule,
    NzSelectModule,
    IconsProviderModule,
    NzCheckboxModule,
  ],
  providers: [
    { provide: DigitalFilingApi, useClass: DigitalFilingService },
    { provide: TransactionalEmailApi, useClass: TransactionalEmailService },
    { provide: MultiTemplateTransactionalEmailApi, useClass: MultiTemplateTransactionalEmailService },
    { provide: EmailCampaignApi, useClass: EmailCampaignService },
    { provide: SmsApi, useClass: SmsService },
    { provide: WhatsAppApi, useClass: WhatsAppService },
    { provide: MultiTemplateWhatsappApi, useClass: MultiTemplateWhatsappService },
  ],
})
export class ReportModule {}
