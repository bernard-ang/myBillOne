import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { TransactionalEmailState } from '../../../../states/transactional-email/transactional-email.state';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  BaseIndexRowModel,
  getErrorMessage,
  ProcessStatus,
  TransactionalEmailActivityModel,
  TransactionalEmailRecordModel,
  TransactionalEmailTypeModel,
} from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActivatedRoute, Params } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import {
  DownloadTransactionalEmailFile,
  GetTransactionalEmailActivity,
  GetTransactionalEmailType,
  ResetTransactionalEmailActivity,
} from '../../../../states/transactional-email/transactional-email.state-actions';
import { getStatusTag } from 'projects/client/src/utils/get-status-tag';
import { getIndexRowObjects } from '../../../../utils/get-index-row-objects';
import { environment } from '../../../../environments/environment';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { saveAs } from 'file-saver';

@Component({
  selector: 'grabbill-client-transactional-email-record-detail',
  templateUrl: './transactional-email-record-detail.component.html',
  styleUrls: ['./transactional-email-record-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransactionalEmailRecordDetailComponent extends NgxsBaseComponent {
  @Select(TransactionalEmailState.transactionalEmailType)
  transactionalEmailType$!: Observable<TransactionalEmailTypeModel>;

  @Select(TransactionalEmailState.transactionalEmailActivity)
  transactionalEmailActivity$!: Observable<TransactionalEmailActivityModel>;

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
    this.store.dispatch(new ResetTransactionalEmailActivity());
    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.recordId = parseInt(params['recordId']);
          this.store.dispatch(new GetTransactionalEmailType(this.typeId!));
          this.store.dispatch(new GetTransactionalEmailActivity(this.typeId!, this.activityId!));
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
        ofActionCompleted(GetTransactionalEmailType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'transactional-email', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetTransactionalEmailActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'transactional-email', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DownloadTransactionalEmailFile),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(TransactionalEmailState.file)!;
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

  getIndexRowObjects(row: BaseIndexRowModel, type: TransactionalEmailTypeModel) {
    return getIndexRowObjects(row, type).map((obj) => ({
      span: 2,
      title: obj.label,
      value: obj.value,
      type: 'string' as 'string',
    }));
  }

  hasFile(row: BaseIndexRowModel, activity: TransactionalEmailActivityModel) {
    return activity.files.filter((file) => file.name === row.text2).length > 0;
  }

  doDownloadFile(row: BaseIndexRowModel) {
    this.store.dispatch(
      new DownloadTransactionalEmailFile(this.typeId!, this.activityId!, row.file!.id, row.file!.name!)
    );
  }

  getDateFormat() {
    return environment.config.dateFormat;
  }
}
