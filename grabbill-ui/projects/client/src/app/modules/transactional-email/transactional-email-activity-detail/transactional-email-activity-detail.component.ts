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
import { ActivatedRoute, Params } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import {
  GetTransactionalEmailActivity,
  GetTransactionalEmailType,
  ResetTransactionalEmailActivity,
} from '../../../../states/transactional-email/transactional-email.state-actions';
import { getIndexFieldLabels } from '../../../../utils/get-index-field-labels';
import { getIndexRowValues } from '../../../../utils/get-index-row-values';
import { getStatusTag } from 'projects/client/src/utils/get-status-tag';
import { getCode } from 'projects/client/src/utils/get-code';
import { ShowMessage } from '../../../../states/common/common.state-actions';

@Component({
  selector: 'grabbill-client-transactional-email-activity-detail',
  templateUrl: './transactional-email-activity-detail.component.html',
  styleUrls: ['./transactional-email-activity-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransactionalEmailActivityDetailComponent extends NgxsBaseComponent {
  @Select(TransactionalEmailState.transactionalEmailType)
  transactionalEmailType$!: Observable<TransactionalEmailTypeModel>;

  @Select(TransactionalEmailState.transactionalEmailActivity)
  transactionalEmailActivity$!: Observable<TransactionalEmailActivityModel>;

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
    this.store.dispatch(new ResetTransactionalEmailActivity());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.store.dispatch(new GetTransactionalEmailType(this.typeId!));
          this.store.dispatch(new GetTransactionalEmailActivity(this.typeId!, this.activityId!));
        })
      ),
      this.transactionalEmailActivity$.pipe(
        tap((activity) => {
          if (activity && activity.status === ProcessStatus.DRAFT) {
            this.navigate(['/', 'transactional-email', 'detail', this.typeId!, 'activity', this.activityId!, 'edit']);
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
      )
    );
  }

  getCode(type: TransactionalEmailTypeModel): string {
    return getCode(type.code);
  }

  getStatusTag(status: ProcessStatus): string {
    return getStatusTag(status);
  }

  getIndexFieldLabels(type: TransactionalEmailTypeModel) {
    return getIndexFieldLabels(type);
  }

  getIndexRowValues(row: BaseIndexRowModel, type: TransactionalEmailTypeModel) {
    return getIndexRowValues(row, type);
  }

  doSearchAttachment(event: any) {
    this.email = event.target.value;
  }

  getFilterRecords(records: TransactionalEmailRecordModel[]): TransactionalEmailRecordModel[] {
    return records.filter((record) => (this.email ? record.indexRow.text1.includes(this.email) : true));
  }

  doSearch(event: any) {
    this.email = event.target.value;
    this.cd.markForCheck();
  }
}
