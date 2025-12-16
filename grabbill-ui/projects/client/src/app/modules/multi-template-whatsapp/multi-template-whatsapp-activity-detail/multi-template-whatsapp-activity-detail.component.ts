import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { WhatsAppState } from '../../../../states/whatsapp/whatsapp.state';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  getErrorMessage,
  MultiTemplateWhatsappActivityModel,
  MultiTemplateWhatsappTypeModel,
  ProcessStatus,
  WhatsAppRecordModel,
} from '@grabbill/lib';
import { ActivatedRoute, Params, RouterModule } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import {
  GetMultiTemplateWhatsAppActivity,
  GetMultiTemplateWhatsAppType,
  ResetWhatsAppActivity,
} from '../../../../states/whatsapp/whatsapp.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { AppCommonModule } from '../../app-common/app-common.module';
import { CommonModule } from '@angular/common';
import { getCode } from 'projects/client/src/utils/get-code';
import { getStatusTag } from 'projects/client/src/utils/get-status-tag';
import { IconsProviderModule } from '../../../icons-provider.module';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzInputModule } from "ng-zorro-antd/input";
import { NzTagModule } from "ng-zorro-antd/tag";
import { NzToolTipModule } from "ng-zorro-antd/tooltip";
import { NzCardModule } from "ng-zorro-antd/card";

@Component({
  selector: 'grabbill-client-multi-template-whatsapp-activity-detail',
  standalone: true,
  imports: [
    AppCommonModule,
    CommonModule,
    IconsProviderModule,
    NzInputModule,
    NzTagModule,
    NzToolTipModule,
    NzSkeletonModule,
    NzTabsModule,
    NzTableModule,
    NzCardModule,
    RouterModule,
  ],
  templateUrl: './multi-template-whatsapp-activity-detail.component.html',
  styleUrl: './multi-template-whatsapp-activity-detail.component.less',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MultiTemplateWhatsappActivityDetailComponent extends NgxsBaseComponent {
  @Select(WhatsAppState.multiTemplateWhatsAppType)
  multiTemplateWhatsAppType$!: Observable<MultiTemplateWhatsappTypeModel>;

  @Select(WhatsAppState.multiTemplateWhatsAppActivity)
  multiTemplateWhatsAppActivity$!: Observable<MultiTemplateWhatsappActivityModel>;

  typeId?: number;
  activityId?: number;
  email?: string;

  constructor(
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetWhatsAppActivity());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.store.dispatch(new GetMultiTemplateWhatsAppType(this.typeId!));
          this.store.dispatch(new GetMultiTemplateWhatsAppActivity(this.typeId!, this.activityId!));
        })
      ),
      this.multiTemplateWhatsAppActivity$.pipe(
        tap((activity) => {
          if (activity && activity.status === ProcessStatus.DRAFT) {
            this.navigate(['/', 'mt-whatsapp', 'detail', this.typeId!, 'activity', this.activityId!, 'edit']);
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetMultiTemplateWhatsAppType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'mt-whatsapp', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetMultiTemplateWhatsAppActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'mt-whatsapp', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      )
    );
  }

  getCode(type: MultiTemplateWhatsappTypeModel): string {
    return getCode(type.code);
  }

  getStatusTag(status: ProcessStatus): string {
    return getStatusTag(status);
  }

  getFilterRecords(records: WhatsAppRecordModel[]): WhatsAppRecordModel[] {
    return records.filter((record) => (this.email ? record.indexRow.text1.includes(this.email) : true));
  }

  doSearch(event: any) {
    this.email = event.target.value;
    this.cd.markForCheck();
  }
}
