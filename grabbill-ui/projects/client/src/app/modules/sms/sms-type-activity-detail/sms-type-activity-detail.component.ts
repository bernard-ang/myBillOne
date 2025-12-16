import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { SmsState } from '../../../../states/sms/sms.state';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  BaseIndexRowModel,
  BaseRecordModel,
  getErrorMessage,
  ProcessStatus,
  SmsActivityModel,
  SmsRecordModel,
  SmsTypeModel,
} from '@grabbill/lib';
import { ActivatedRoute, Params } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import { GetSmsActivity, GetSmsType, ResetSmsActivity } from '../../../../states/sms/sms.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { getIndexRowValues } from 'projects/client/src/utils/get-index-row-values';
import { getIndexFieldLabels } from 'projects/client/src/utils/get-index-field-labels';
import { getStatusTag } from 'projects/client/src/utils/get-status-tag';
import { getCode } from 'projects/client/src/utils/get-code';

@Component({
  selector: 'grabbill-client-sms-type-activity-detail',
  templateUrl: './sms-type-activity-detail.component.html',
  styleUrls: ['./sms-type-activity-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SmsTypeActivityDetailComponent extends NgxsBaseComponent {
  @Select(SmsState.smsType)
  smsType$!: Observable<SmsTypeModel>;

  @Select(SmsState.smsActivity)
  smsActivity$!: Observable<SmsActivityModel>;

  typeId?: number;
  activityId?: number;
  mobileNo?: string;

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
    this.store.dispatch(new ResetSmsActivity());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.store.dispatch(new GetSmsType(this.typeId!));
          this.store.dispatch(new GetSmsActivity(this.typeId!, this.activityId!));
        })
      ),
      this.smsActivity$.pipe(
        tap((activity) => {
          if (activity && activity.status === ProcessStatus.DRAFT) {
            this.navigate(['/', 'sms', 'detail', this.typeId!, 'activity', this.activityId!, 'edit']);
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetSmsType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'sms', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetSmsActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'sms', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      )
    );
  }

  getCode(type: SmsTypeModel): string {
    return getCode(type.code);
  }

  getStatusTag(status: ProcessStatus): string {
    return getStatusTag(status);
  }

  getIndexFieldLabels(type: SmsTypeModel) {
    return getIndexFieldLabels(type);
  }

  getIndexRowValues(row: BaseIndexRowModel, type: SmsTypeModel) {
    return getIndexRowValues(row, type);
  }

  getFilterRecords(records: BaseRecordModel[]): SmsRecordModel[] {
    return (records as SmsRecordModel[]).filter((record) =>
      this.mobileNo ? record.name.includes(this.mobileNo) : true
    );
  }

  doSearch(event: any) {
    this.mobileNo = event.target.value;
    this.cd.markForCheck();
  }
}
