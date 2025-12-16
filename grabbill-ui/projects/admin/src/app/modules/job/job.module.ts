import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { JobListComponent } from "./job-list/job-list.component";
import { JobDetailComponent } from "./job-detail/job-detail.component";
import { RouterModule } from "@angular/router";
import { IconsProviderModule } from "../../icons-provider.module";
import { NzButtonModule } from "ng-zorro-antd/button";
import { NzTypographyModule } from "ng-zorro-antd/typography";
import { NzTableModule } from "ng-zorro-antd/table";
import { NzDatePickerModule } from "ng-zorro-antd/date-picker";
import { NzInputModule } from "ng-zorro-antd/input";
import { NzTagModule } from "ng-zorro-antd/tag";
import { NzDescriptionsModule } from "ng-zorro-antd/descriptions";
import { NzBreadCrumbModule } from "ng-zorro-antd/breadcrumb";
import { NzStatisticModule } from "ng-zorro-antd/statistic";
import { NzTabsModule } from "ng-zorro-antd/tabs";
import { NgxsModule } from "@ngxs/store";
import { JobManagementState } from "../../../states/job-management/job-management.state";
import { JobManagementApi } from "../../../api/job-management.api";
import { JobManagementService } from "../../../services/job-management.service";
import { ReactiveFormsModule } from "@angular/forms";
import { NzModalModule } from "ng-zorro-antd/modal";
import { NzFormModule } from "ng-zorro-antd/form";
import { NzSelectModule } from "ng-zorro-antd/select";
import { NzSkeletonModule } from "ng-zorro-antd/skeleton";
import { HtmlPreviewComponent } from "./html-preview/html-preview.component";
import { NzToolTipModule } from "ng-zorro-antd/tooltip";

@NgModule({
  declarations: [HtmlPreviewComponent, JobListComponent, JobDetailComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      { path: "list", component: JobListComponent },
      { path: "detail/:id", component: JobDetailComponent }
    ]),
    NgxsModule.forFeature([ JobManagementState ]),
    IconsProviderModule,
    NzButtonModule,
    NzTypographyModule,
    NzTableModule,
    NzDatePickerModule,
    NzInputModule,
    NzTagModule,
    NzDescriptionsModule,
    NzBreadCrumbModule,
    NzStatisticModule,
    NzTabsModule,
    ReactiveFormsModule,
    NzModalModule,
    NzFormModule,
    NzSelectModule,
    NzSkeletonModule,
    NzToolTipModule
  ],
  providers: [{ provide: JobManagementApi, useClass: JobManagementService }],
})
export class JobModule {}
