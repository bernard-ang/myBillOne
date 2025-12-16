import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MultiTemplateWhatsappTypeListComponent } from './multi-template-whatsapp-type-list/multi-template-whatsapp-type-list.component';
import { MultiTemplateWhatsappActivityDetailComponent } from './multi-template-whatsapp-activity-detail/multi-template-whatsapp-activity-detail.component';
import { MultiTemplateWhatsappTypeEditDetailComponent } from './multi-template-whatsapp-type-edit-detail/multi-template-whatsapp-type-edit-detail.component';
import { MultiTemplateWhatsappActivityEditDetailComponent } from './multi-template-whatsapp-activity-edit-detail/multi-template-whatsapp-activity-edit-detail.component';
import { MultiTemplateWhatsappActivityRecordDetailComponent } from './multi-template-whatsapp-activity-record-detail/multi-template-whatsapp-activity-record-detail.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PrivilegeGuard } from '../../guards/privilege.guard';
import { Privilege } from '@grabbill/lib';
import { MultiTemplateWhatsappTypeDetailComponent } from './multi-template-whatsapp-type-detail/multi-template-whatsapp-type-detail.component';
import { WhatsAppApi } from '../../../api/whatsapp.api';
import { WhatsAppService } from '../../../services/whats-app.service';
import { WhatsAppEventApi } from '../../../api/whatsapp-event.api';
import { WhatsappEventService } from '../../../services/whatsapp-event.service';
import { MultiTemplateWhatsappApi } from '../../../api/multi-template-whatsapp.api';
import { MultiTemplateWhatsappService } from '../../../services/multi-template-whatsapp.service';
import { NgxsModule } from "@ngxs/store";
import { WhatsAppState } from "../../../states/whatsapp/whatsapp.state";

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      {
        path: 'list',
        component: MultiTemplateWhatsappTypeListComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.WA_VIEW },
      },
      {
        path: 'detail/new',
        component: MultiTemplateWhatsappTypeEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.WA_EDIT },
      },
      {
        path: 'detail/:id',
        component: MultiTemplateWhatsappTypeDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.WA_VIEW },
      },
      {
        path: 'detail/:id/edit',
        component: MultiTemplateWhatsappTypeEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.WA_EDIT },
      },
      {
        path: 'detail/:id/activity/:activityId',
        component: MultiTemplateWhatsappActivityDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.WA_VIEW },
      },
      {
        path: 'detail/:id/activity/:activityId/edit',
        component: MultiTemplateWhatsappActivityEditDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.WA_EDIT },
      },
      {
        path: 'detail/:id/activity/:activityId/record/:recordId',
        component: MultiTemplateWhatsappActivityRecordDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.WA_VIEW },
      },
    ]),
    NgxsModule.forFeature([
      WhatsAppState
    ]),
    MultiTemplateWhatsappTypeListComponent,
    MultiTemplateWhatsappTypeDetailComponent,
    MultiTemplateWhatsappTypeEditDetailComponent,
    MultiTemplateWhatsappActivityDetailComponent,
    MultiTemplateWhatsappActivityEditDetailComponent,
    MultiTemplateWhatsappActivityRecordDetailComponent,
  ],
  providers: [
    { provide: WhatsAppApi, useClass: WhatsAppService },
    { provide: WhatsAppEventApi, useClass: WhatsappEventService },
    { provide: MultiTemplateWhatsappApi, useClass: MultiTemplateWhatsappService },
  ],
})
export class MultiTemplateWhatsappModule {}
