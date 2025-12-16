import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { WhatsAppState } from '../../../../states/whatsapp/whatsapp.state';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  BaseIndexRowModel,
  getErrorMessage,
  MultiTemplateWhatsappActivityModel,
  MultiTemplateWhatsappTypeModel,
  ProcessStatus,
  WhatsAppRecordModel,
} from '@grabbill/lib';
import { ActivatedRoute, Params } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import {
  DownloadMultiTemplateWhatsAppFile,
  GetMultiTemplateWhatsAppActivity,
  GetMultiTemplateWhatsAppType,
  GetWhatsAppActivity,
  GetWhatsAppType,
  ResetWhatsAppActivity,
} from '../../../../states/whatsapp/whatsapp.state-actions';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { saveAs } from 'file-saver';
import { AppCommonModule } from '../../app-common/app-common.module';
import { CommonModule } from '@angular/common';
import { getStatusTag } from '../../../../utils/get-status-tag';
import { getIndexRowObjects } from '../../../../utils/get-index-row-objects';
import { NzListModule } from 'ng-zorro-antd/list';
import { IconsProviderModule } from '../../../icons-provider.module';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzCardModule } from 'ng-zorro-antd/card';

@Component({
  selector: 'grabbill-client-multi-template-whatsapp-activity-record-detail',
  standalone: true,
  imports: [
    AppCommonModule,
    CommonModule,
    NzListModule,
    NzSkeletonModule,
    NzTabsModule,
    NzCardModule,
    IconsProviderModule,
  ],
  templateUrl: './multi-template-whatsapp-activity-record-detail.component.html',
  styleUrl: './multi-template-whatsapp-activity-record-detail.component.less',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MultiTemplateWhatsappActivityRecordDetailComponent extends NgxsBaseComponent {
  @Select(WhatsAppState.multiTemplateWhatsAppType)
  multiTemplateWhatsAppType$!: Observable<MultiTemplateWhatsappTypeModel>;

  @Select(WhatsAppState.multiTemplateWhatsAppActivity)
  multiTemplateWhatsAppActivity$!: Observable<MultiTemplateWhatsappActivityModel>;

  typeId?: number;
  activityId?: number;
  recordId?: number;
  record?: WhatsAppRecordModel;

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
          this.recordId = parseInt(params['recordId']);
          this.store.dispatch(new GetWhatsAppType(this.typeId!));
          this.store.dispatch(new GetWhatsAppActivity(this.typeId!, this.activityId!));
        })
      ),
      this.multiTemplateWhatsAppActivity$.pipe(
        tap((activity) => {
          if (activity) {
            this.record = activity.records.filter((value) => value.id === this.recordId)[0];
            this.cd.markForCheck();
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
      ),
      this.actions$.pipe(
        ofActionCompleted(DownloadMultiTemplateWhatsAppFile),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(WhatsAppState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      )
    );
  }

  getStatusTag(status: ProcessStatus): string {
    return getStatusTag(status);
  }

  getIndexRowObjects(row: BaseIndexRowModel, type: MultiTemplateWhatsappTypeModel) {
    return getIndexRowObjects(row, type).map((obj) => ({
      span: 2,
      title: obj.label,
      value: obj.value,
      type: 'string' as 'string',
    }));
  }
}
