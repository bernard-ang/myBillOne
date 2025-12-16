import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import {
  EmailCampaignActivityBasicModel,
  EmailCampaignTypeModel,
  getErrorMessage,
  PageableModel,
  Privilege,
  ProcessStatus,
  SearchResultPayloadModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { BehaviorSubject, Observable, of, switchMap, tap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { EmailCampaignState } from '../../../../states/email-campaign/email-campaign.state';
import { AuthState } from '../../../../states/auth/auth.state';
import { UntypedFormBuilder } from '@angular/forms';
import { ActivatedRoute, Params } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { environment } from '../../../../environments/environment';
import {
  DeleteEmailCampaignActivity,
  GetEmailCampaignType,
  LoadMoreEmailCampaignActivities,
  NewEmailCampaignActivity,
  PurgeEmailCampaignActivity,
  QueryEmailCampaignActivities,
  ResetEmailCampaignType,
} from '../../../../states/email-campaign/email-campaign.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { getCode } from 'projects/client/src/utils/get-code';
import { getStatusTag } from 'projects/client/src/utils/get-status-tag';
import { hasPrivilege } from 'projects/client/src/utils/has-privilege';
import { format } from 'date-fns';
import { createEmailCampaign, handleNewEmailCampaign } from "../../../../utils/email-campaign";

@Component({
  selector: 'grabbill-client-email-campaign-detail',
  templateUrl: './email-campaign-detail.component.html',
  styleUrls: ['./email-campaign-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmailCampaignDetailComponent extends NgxsBaseComponent {
  typeId?: number;
  user?: UserAuthorityModel;

  isInitialize = false;
  isActivityListLoading = false;
  listQueryAction = QueryEmailCampaignActivities;
  listLoadMoreAction = LoadMoreEmailCampaignActivities;
  listState = EmailCampaignState;

  activityNameSearchChange$ = new BehaviorSubject('');

  @Select(EmailCampaignState.emailCampaignType)
  emailCampaignType$!: Observable<EmailCampaignTypeModel>;

  @Select(EmailCampaignState.emailCampaignActivityPageable)
  emailCampaignActivityPageable$!: Observable<PageableModel>;

  @Select(EmailCampaignState.emailCampaignActivitySearchResult)
  emailCampaignActivitySearchResult$!: Observable<SearchResultPayloadModel<EmailCampaignActivityBasicModel>>;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  today: Date = new Date();

  data = [
    { name: 'March Product Sales', updatedAt: new Date(), status: 'DRAFT' },
    { name: 'January Product Sales', sentAt: new Date(), totalSent: 10, totalOpened: 3, status: 'COMPLETED' },
  ];

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private modal: NzModalService,
    public actions$: Actions,
    public override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetEmailCampaignType());

    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.store.dispatch(new GetEmailCampaignType(this.typeId!));
          this.store.dispatch(new QueryEmailCampaignActivities(this.typeId!));
        })
      ),
      this.emailCampaignType$.pipe(
        tap((type) => {
          if (type) {
            this.isInitialize = true;
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
      handleNewEmailCampaign(this.actions$, this.store, this.navigate.bind(this)),
      this.actions$.pipe(
        ofActionCompleted(DeleteEmailCampaignActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity deleted`));
          }
          this.isActivityListLoading = false;
          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(PurgeEmailCampaignActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity file(s) deleted`));
            this.store.dispatch(new QueryEmailCampaignActivities(this.typeId!));
          }
          this.isActivityListLoading = false;
          this.cd.markForCheck();
          return of(false);
        })
      )
    );
  }

  public get privilege(): typeof Privilege {
    return Privilege;
  }

  public get dateFormat(): string {
    return environment.config.dateFormat;
  }

  getCode(type: EmailCampaignTypeModel): string {
    return getCode(type.code);
  }

  getStatusTag(activity: EmailCampaignActivityBasicModel): string {
    return getStatusTag(activity.status);
  }

  doEdit(typeId: number) {
    this.navigate(['/', 'email-campaign', 'detail', typeId, 'edit']);
  }

  doViewActivity(item: EmailCampaignActivityBasicModel) {
    if (item.status === ProcessStatus.DRAFT) {
      this.doEditActivity(this.typeId!, item.id);
    } else {
      this.navigate(['/', 'email-campaign', 'detail', this.typeId!, 'activity', item.id], { tab: 'records' });
    }
  }

  doEditActivity(typeId: number, activityId: number) {
    this.navigate(['/', 'email-campaign', 'detail', typeId, 'activity', activityId, 'edit']);
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }

  doCreateEmailCampaignActivity(type: EmailCampaignTypeModel, name: string) {
    createEmailCampaign(this.store, type, name)
  }

  getActivityStatusText(activity: EmailCampaignActivityBasicModel): string {
    if (activity.lastModifiedDate != null) {
      const lastModifiedDate = new Date(activity.lastModifiedDate);
      return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
        activity.lastModifiedBy === this.user?.email ? 'me' : activity.lastModifiedBy
      }`;
    }
    if (activity.createdDate != null) {
      const createdDate = new Date(activity.createdDate);
      return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
        activity.createdBy === this.user?.email ? 'me' : activity.createdBy
      }`;
    }

    return '';
  }

  doDeleteActivity(typeId: number, activity: EmailCampaignActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${activity.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isActivityListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteEmailCampaignActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }
}
