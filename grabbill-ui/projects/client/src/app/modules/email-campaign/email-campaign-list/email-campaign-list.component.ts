import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { Observable, of, switchMap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { format } from 'date-fns';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import {
  EmailCampaignTypeBasicModel,
  getErrorMessage,
  PageableModel,
  Privilege,
  SearchResultPayloadModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { EmailCampaignState } from '../../../../states/email-campaign/email-campaign.state';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import {
  DeleteEmailCampaignType,
  DuplicateEmailCampaignType,
  LoadMoreEmailCampaignTypes,
  QueryEmailCampaignTypes,
  ResetEmailCampaignTypes,
} from '../../../../states/email-campaign/email-campaign.state-actions';
import { AuthState } from '../../../../states/auth/auth.state';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { environment } from '../../../../environments/environment';
import { hasPrivilege } from '../../../../utils/has-privilege';

@Component({
  selector: 'grabbill-client-email-campaign-list',
  templateUrl: './email-campaign-list.component.html',
  styleUrls: ['./email-campaign-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmailCampaignListComponent extends NgxsBaseComponent {
  @Select(EmailCampaignState.emailCampaignTypeSearchResult)
  emailCampaignTypeSearchResult$!: Observable<SearchResultPayloadModel<EmailCampaignTypeBasicModel>>;

  @Select(EmailCampaignState.emailCampaignTypePageable)
  emailCampaignTypePageable$!: Observable<PageableModel>;

  user?: UserAuthorityModel;

  isListLoading = false;
  queryAction = QueryEmailCampaignTypes;
  resetAction = ResetEmailCampaignTypes;
  loadMoreAction = LoadMoreEmailCampaignTypes;
  state = EmailCampaignState;

  constructor(
    protected override messageService: NzMessageService,
    public override store: Store,
    public actions$: Actions,
    private cd: ChangeDetectorRef,
    private modal: NzModalService
  ) {
    super(store, messageService);
  }

  public get privilege(): typeof Privilege {
    return Privilege;
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(DeleteEmailCampaignType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Email campaign deleted`));
          }
          this.isListLoading = false;
          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DuplicateEmailCampaignType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Email campaign duplicated`));
            this.navigate([
              '/',
              'email-campaign',
              'detail',
              this.store.selectSnapshot(EmailCampaignState.emailCampaignType)!.id,
              'edit',
            ]);
          }
          return of(false);
        })
      )
    );
  }

  doSelect(type: EmailCampaignTypeBasicModel) {
    this.navigate(['/', 'email-campaign', 'detail', type.id], { tab: 'activities' });
  }

  doDelete(emailCampaignType: EmailCampaignTypeBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${emailCampaignType.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteEmailCampaignType(emailCampaignType.id));
      },
      nzCancelText: 'No',
    });
  }

  doDuplicate(emailCampaignType: EmailCampaignTypeBasicModel) {
    this.modal.confirm({
      nzTitle: `Duplicate ${emailCampaignType.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new SetPageLoading(true));
        this.store.dispatch(new DuplicateEmailCampaignType(emailCampaignType.id));
      },
      nzCancelText: 'No',
    });
  }

  getMessage() {
    return (emailCampaignType: EmailCampaignTypeBasicModel): string => {
      if (emailCampaignType.lastSentDate != null) {
        const lastUploadDate = new Date(emailCampaignType.lastSentDate);
        return `Last sent at ${format(lastUploadDate, environment.config.dateFormat)} by ${
          emailCampaignType.lastSentBy === this.user?.email ? 'me' : emailCampaignType.lastSentBy
        }`;
      }
      if (emailCampaignType.lastModifiedDate != null) {
        const lastModifiedDate = new Date(emailCampaignType.lastModifiedDate);
        return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
          emailCampaignType.lastModifiedBy === this.user?.email ? 'me' : emailCampaignType.lastModifiedBy
        }`;
      }
      if (emailCampaignType.createdDate != null) {
        const createdDate = new Date(emailCampaignType.createdDate);
        return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
          emailCampaignType.createdBy === this.user?.email ? 'me' : emailCampaignType.createdBy
        }`;
      }

      return '';
    };
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }
}
