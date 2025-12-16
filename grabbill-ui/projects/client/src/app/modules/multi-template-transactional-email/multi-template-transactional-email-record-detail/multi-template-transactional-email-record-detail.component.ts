import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  BaseIndexRowModel,
  getErrorMessage,
  ProcessStatus,
  MultiTemplateTransactionalEmailActivityModel,
  TransactionalEmailRecordModel,
  MultiTemplateTransactionalEmailTypeModel,
} from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActivatedRoute, Params } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import { getStatusTag } from 'projects/client/src/utils/get-status-tag';
import { getIndexRowObjects } from '../../../../utils/get-index-row-objects';
import { environment } from '../../../../environments/environment';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { saveAs } from 'file-saver';
import {
  MultiTemplateTransactionalEmailState
} from "../../../../states/multi-template-transactional-email/multi-template-transactional-email.state";
import {
  DownloadMultiTemplateTransactionalEmailFile,
  GetMultiTemplateTransactionalEmailActivity,
  GetMultiTemplateTransactionalEmailType,
  ResetMultiTemplateTransactionalEmailActivity
} from "../../../../states/multi-template-transactional-email/multi-template-transactional-email.state-actions";

@Component({
  selector: 'grabbill-client-multi-template-transactional-email-record-detail',
  templateUrl: './multi-template-transactional-email-record-detail.component.html',
  styleUrls: ['./multi-template-transactional-email-record-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MultiTemplateTransactionalEmailRecordDetailComponent extends NgxsBaseComponent {
  @Select(MultiTemplateTransactionalEmailState.transactionalEmailType)
  transactionalEmailType$!: Observable<MultiTemplateTransactionalEmailTypeModel>;

  @Select(MultiTemplateTransactionalEmailState.transactionalEmailActivity)
  transactionalEmailActivity$!: Observable<MultiTemplateTransactionalEmailActivityModel>;

  typeId?: number;
  activityId?: number;
  recordId?: number;
  record?: TransactionalEmailRecordModel;

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
    this.store.dispatch(new ResetMultiTemplateTransactionalEmailActivity());
    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.recordId = parseInt(params['recordId']);
          this.store.dispatch(new GetMultiTemplateTransactionalEmailType(this.typeId!));
          this.store.dispatch(new GetMultiTemplateTransactionalEmailActivity(this.typeId!, this.activityId!));
        })
      ),
      this.transactionalEmailActivity$.pipe(
        tap((activity) => {
          if (activity) {
            this.record = activity.records.filter((value) => value.id === this.recordId)[0];
            this.cd.markForCheck();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetMultiTemplateTransactionalEmailType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'mt-transactional-email', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetMultiTemplateTransactionalEmailActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'mt-transactional-email', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DownloadMultiTemplateTransactionalEmailFile),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(MultiTemplateTransactionalEmailState.file)!;
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

  getIndexRowObjects(row: BaseIndexRowModel, type: MultiTemplateTransactionalEmailTypeModel) {
    return getIndexRowObjects(row, type).map((obj) => ({
      span: 2,
      title: obj.label,
      value: obj.value,
      type: 'string' as 'string',
    }));
  }

  hasFile(row: BaseIndexRowModel, activity: MultiTemplateTransactionalEmailActivityModel) {
    return activity.files.filter((file) => file.name === row.text2).length > 0;
  }

  doDownloadFile(row: BaseIndexRowModel) {
    this.store.dispatch(
      new DownloadMultiTemplateTransactionalEmailFile(this.typeId!, this.activityId!, row.file!.id, row.file!.name!)
    );
  }

  getDateFormat() {
    return environment.config.dateFormat;
  }
}
