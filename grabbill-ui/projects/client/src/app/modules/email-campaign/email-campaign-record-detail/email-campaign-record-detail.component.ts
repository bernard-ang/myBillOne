import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { Observable, of, switchMap, tap } from 'rxjs';
import { saveAs } from 'file-saver';
import { environment } from '../../../../environments/environment';
import {
  BaseFileModel,
  EmailCampaignActivityModel,
  EmailCampaignRecordModel,
  EmailCampaignTypeModel,
  getErrorMessage,
  ProcessStatus,
} from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { NzMessageService } from 'ng-zorro-antd/message';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { EmailCampaignState } from '../../../../states/email-campaign/email-campaign.state';
import {
  DownloadEmailCampaignFile,
  GetEmailCampaignActivity,
  GetEmailCampaignType,
  ResetEmailCampaignActivity,
} from '../../../../states/email-campaign/email-campaign.state-actions';
import { getStatusTag } from '../../../../utils/get-status-tag';

@Component({
  selector: 'grabbill-client-email-campaign-record-detail',
  templateUrl: './email-campaign-record-detail.component.html',
  styleUrls: ['./email-campaign-record-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmailCampaignRecordDetailComponent extends NgxsBaseComponent {
  @Select(EmailCampaignState.emailCampaignType)
  emailCampaignType$!: Observable<EmailCampaignTypeModel>;

  @Select(EmailCampaignState.emailCampaignActivity)
  emailCampaignActivity$!: Observable<EmailCampaignActivityModel>;

  typeId?: number;
  activityId?: number;
  recordId?: number;
  record?: EmailCampaignRecordModel;

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
    this.store.dispatch(new ResetEmailCampaignActivity());
    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.recordId = parseInt(params['recordId']);
          this.store.dispatch(new GetEmailCampaignType(this.typeId!));
          this.store.dispatch(new GetEmailCampaignActivity(this.typeId!, this.activityId!));
        })
      ),
      this.emailCampaignActivity$.pipe(
        tap((activity) => {
          if (activity) {
            this.record = activity.records.filter((value) => value.id === this.recordId)[0];
            this.cd.markForCheck();
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

  getStatusTag(status: ProcessStatus): string {
    return getStatusTag(status);
  }

  doDownloadFile(file: BaseFileModel, ori: any) {
    ori.store.dispatch(new DownloadEmailCampaignFile(ori.typeId, ori.activityId, file.id, file.name));
  }

  getDateFormat() {
    return environment.config.dateFormat;
  }
}
