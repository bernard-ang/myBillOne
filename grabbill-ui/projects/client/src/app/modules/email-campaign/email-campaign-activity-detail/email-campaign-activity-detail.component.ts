import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { EmailCampaignState } from '../../../../states/email-campaign/email-campaign.state';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  BaseFileModel,
  BaseIndexRowModel,
  EmailCampaignActivityModel,
  EmailCampaignRecordModel,
  EmailCampaignTypeModel,
  getErrorMessage,
  ProcessStatus,
} from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActivatedRoute, Params } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import {
  DownloadEmailCampaignFile,
  GetEmailCampaignActivity,
  GetEmailCampaignType,
  ResetEmailCampaignActivity,
} from '../../../../states/email-campaign/email-campaign.state-actions';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { getCode } from 'projects/client/src/utils/get-code';
import { getStatusTag } from 'projects/client/src/utils/get-status-tag';
import { getIndexFieldLabels } from 'projects/client/src/utils/get-index-field-labels';
import { getIndexRowValues } from 'projects/client/src/utils/get-index-row-values';
import { saveAs } from 'file-saver';

@Component({
  selector: 'grabbill-client-email-campaign-activity-detail',
  templateUrl: './email-campaign-activity-detail.component.html',
  styleUrls: ['./email-campaign-activity-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmailCampaignActivityDetailComponent extends NgxsBaseComponent {
  @Select(EmailCampaignState.emailCampaignType)
  emailCampaignType$!: Observable<EmailCampaignTypeModel>;

  @Select(EmailCampaignState.emailCampaignActivity)
  emailCampaignActivity$!: Observable<EmailCampaignActivityModel>;

  typeId?: number;
  activityId?: number;
  email?: string;

  constructor(
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    public override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetEmailCampaignActivity());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.store.dispatch(new GetEmailCampaignType(this.typeId!));
          this.store.dispatch(new GetEmailCampaignActivity(this.typeId!, this.activityId!));
        })
      ),
      this.emailCampaignActivity$.pipe(
        tap((activity) => {
          if (activity && activity.status === ProcessStatus.DRAFT) {
            this.navigate(['/', 'email-campaign', 'detail', this.typeId!, 'activity', this.activityId!, 'edit']);
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetEmailCampaignType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'email-campaign', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetEmailCampaignActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'email-campaign', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DownloadEmailCampaignFile),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(EmailCampaignState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      )
    );
  }

  getCode(type: EmailCampaignTypeModel): string {
    return getCode(type.code);
  }

  getStatusTag(status: ProcessStatus): string {
    return getStatusTag(status);
  }

  getIndexFieldLabels(type: EmailCampaignTypeModel) {
    return getIndexFieldLabels(type);
  }

  getIndexRowValues(row: BaseIndexRowModel, type: EmailCampaignTypeModel) {
    return getIndexRowValues(row, type);
  }

  doSearchAttachment(event: any) {
    this.email = event.target.value;
  }

  getFilterRecords(records: EmailCampaignRecordModel[]): EmailCampaignRecordModel[] {
    return records.filter((record) => (this.email ? record.name.includes(this.email) : true));
  }

  doSearch(event: any) {
    this.email = event.target.value;
    this.cd.markForCheck();
  }

  doDownloadFile(file: BaseFileModel, ori: any) {
    ori.store.dispatch(new DownloadEmailCampaignFile(ori.typeId!, ori.activityId!, file.id, file.name!));
  }
}
