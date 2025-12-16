import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { WhatsappTypeListComponent } from "./whatsapp-type-list/whatsapp-type-list.component";
import { WhatsappTypeDetailComponent } from "./whatsapp-type-detail/whatsapp-type-detail.component";
import { WhatsappTypeEditDetailComponent } from "./whatsapp-type-edit-detail/whatsapp-type-edit-detail.component";
import { WhatsappActivityDetailComponent } from "./whatsapp-activity-detail/whatsapp-activity-detail.component";
import {
  WhatsappActivityEditDetailComponent
} from "./whatsapp-activity-edit-detail/whatsapp-activity-edit-detail.component";
import { ReactiveFormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { PrivilegeGuard } from "../../guards/privilege.guard";
import { Privilege } from "@grabbill/lib";
import { NgxsModule } from "@ngxs/store";
import { WhatsAppState } from "../../../states/whatsapp/whatsapp.state";
import { WhatsAppApi } from "../../../api/whatsapp.api";
import { WhatsAppService } from "../../../services/whats-app.service";
import { AppCommonModule } from "../app-common/app-common.module";
import { NzDividerModule } from "ng-zorro-antd/divider";
import { NzToolTipModule } from "ng-zorro-antd/tooltip";
import { NzListModule } from "ng-zorro-antd/list";
import { IconsProviderModule } from "../../icons-provider.module";
import { WhatsAppEventApi } from "../../../api/whatsapp-event.api";
import { WhatsappEventService } from "../../../services/whatsapp-event.service";
import { NzButtonModule } from "ng-zorro-antd/button";
import { NzStepsModule } from "ng-zorro-antd/steps";
import { NzSkeletonModule } from "ng-zorro-antd/skeleton";
import { NzFormModule } from "ng-zorro-antd/form";
import { NzSwitchModule } from "ng-zorro-antd/switch";
import { NzInputModule } from "ng-zorro-antd/input";
import { NzTypographyModule } from "ng-zorro-antd/typography";
import { NzSelectModule } from "ng-zorro-antd/select";
import { NzTableModule } from "ng-zorro-antd/table";
import { NzCheckboxModule } from "ng-zorro-antd/checkbox";
import { NzTabsModule } from "ng-zorro-antd/tabs";
import { NzTagModule } from "ng-zorro-antd/tag";
import { NzCardModule } from "ng-zorro-antd/card";
import { NzModalModule } from "ng-zorro-antd/modal";
import { NzInputNumberModule } from "ng-zorro-antd/input-number";
import { NzDatePickerModule } from "ng-zorro-antd/date-picker";
import { NzDrawerModule } from "ng-zorro-antd/drawer";
import { NzEmptyModule } from "ng-zorro-antd/empty";
import { NzPaginationModule } from "ng-zorro-antd/pagination";
import { NzCollapseModule } from "ng-zorro-antd/collapse";
import { NzSpinModule } from "ng-zorro-antd/spin";
import { NzUploadModule } from "ng-zorro-antd/upload";
import { WhatsappRecordDetailComponent } from "./whatsapp-record-detail/whatsapp-record-detail.component";
import { MultiTemplateWhatsappApi } from "../../../api/multi-template-whatsapp.api";
import { MultiTemplateWhatsappService } from "../../../services/multi-template-whatsapp.service";

@NgModule({
  declarations: [
    WhatsappTypeListComponent,
    WhatsappTypeDetailComponent,
    WhatsappTypeEditDetailComponent,
    WhatsappActivityDetailComponent,
    WhatsappActivityEditDetailComponent,
    WhatsappRecordDetailComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild([
      {
        path: "list",
        component: WhatsappTypeListComponent,
        canActivate: [ PrivilegeGuard ],
        data: { privilege: Privilege.WA_VIEW }
      },
      {
        path: "detail/new",
        component: WhatsappTypeEditDetailComponent,
        canActivate: [ PrivilegeGuard ],
        data: { privilege: Privilege.WA_EDIT }
      },
      {
        path: "detail/:id",
        component: WhatsappTypeDetailComponent,
        canActivate: [ PrivilegeGuard ],
        data: { privilege: Privilege.WA_VIEW }
      },
      {
        path: "detail/:id/edit",
        component: WhatsappTypeEditDetailComponent,
        canActivate: [ PrivilegeGuard ],
        data: { privilege: Privilege.WA_EDIT }
      },
      {
        path: "detail/:id/activity/:activityId",
        component: WhatsappActivityDetailComponent,
        canActivate: [ PrivilegeGuard ],
        data: { privilege: Privilege.WA_VIEW }
      },
      {
        path: "detail/:id/activity/:activityId/edit",
        component: WhatsappActivityEditDetailComponent,
        canActivate: [ PrivilegeGuard ],
        data: { privilege: Privilege.WA_EDIT }
      },
      {
        path: 'detail/:id/activity/:activityId/record/:recordId',
        component: WhatsappRecordDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.WA_VIEW },
      },
    ]),
    NgxsModule.forFeature([
      WhatsAppState
    ]),
    AppCommonModule,
    NzDividerModule,
    NzToolTipModule,
    NzListModule,
    IconsProviderModule,
    NzButtonModule,
    NzStepsModule,
    NzSkeletonModule,
    NzFormModule,
    NzSwitchModule,
    NzInputModule,
    NzTypographyModule,
    NzSelectModule,
    NzTableModule,
    NzCheckboxModule,
    NzTabsModule,
    NzTagModule,
    NzCardModule,
    NzModalModule,
    NzInputNumberModule,
    NzDatePickerModule,
    NzDrawerModule,
    NzEmptyModule,
    NzPaginationModule,
    NzCollapseModule,
    NzSpinModule,
    NzUploadModule
  ],
  providers: [
    { provide: WhatsAppApi, useClass: WhatsAppService },
    { provide: WhatsAppEventApi, useClass: WhatsappEventService },
    { provide: MultiTemplateWhatsappApi, useClass: MultiTemplateWhatsappService },
  ],
})
export class WhatsappModule {
}
