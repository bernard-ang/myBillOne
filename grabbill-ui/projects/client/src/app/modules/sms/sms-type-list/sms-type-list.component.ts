import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { SmsState } from '../../../../states/sms/sms.state';
import { Observable, of, switchMap } from 'rxjs';
import {
  getErrorMessage,
  PageableModel,
  Privilege,
  SearchResultPayloadModel,
  SmsTypeBasicModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import {
  DeleteSmsType,
  DuplicateSmsType,
  LoadMoreSmsTypes,
  QuerySmsTypes,
  ResetSmsTypes,
} from '../../../../states/sms/sms.state-actions';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { AuthState } from '../../../../states/auth/auth.state';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { format } from 'date-fns';
import { environment } from '../../../../environments/environment';
import { hasPrivilege } from 'projects/client/src/utils/has-privilege';
import { isSaasMode } from "../../../../utils/deployment-mode";

@Component({
  selector: 'grabbill-client-sms-type-list',
  templateUrl: './sms-type-list.component.html',
  styleUrls: ['./sms-type-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SmsTypeListComponent extends NgxsBaseComponent {
  @Select(SmsState.smsTypeSearchResult)
  smsTypeSearchResult$!: Observable<SearchResultPayloadModel<SmsTypeBasicModel>>;

  @Select(SmsState.smsTypePageable)
  smsTypePageable$!: Observable<PageableModel>;

  user?: UserAuthorityModel;

  isListLoading = false;
  queryAction = QuerySmsTypes;
  resetAction = ResetSmsTypes;
  loadMoreAction = LoadMoreSmsTypes;
  state = SmsState;
  canTopUpSms = environment.config.canTopUpSms;
  isSaas = isSaasMode()

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
        ofActionCompleted(DeleteSmsType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Sms deleted`));
          }
          this.isListLoading = false;
          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DuplicateSmsType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Sms duplicated`));
            this.navigate(['/', 'sms', 'detail', this.store.selectSnapshot(SmsState.smsType)!.id, 'edit']);
          }
          return of(false);
        })
      )
    );
  }

  doSelect(type: SmsTypeBasicModel) {
    this.navigate(['/', 'sms', 'detail', type.id], { tab: 'activities' });
  }

  doDelete(smsType: SmsTypeBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${smsType.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteSmsType(smsType.id));
      },
      nzCancelText: 'No',
    });
  }

  doDuplicate(smsType: SmsTypeBasicModel) {
    this.modal.confirm({
      nzTitle: `Duplicate ${smsType.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new SetPageLoading(true));
        this.store.dispatch(new DuplicateSmsType(smsType.id));
      },
      nzCancelText: 'No',
    });
  }

  getMessage() {
    return (smsType: SmsTypeBasicModel): string => {
      if (smsType.lastSentDate != null) {
        const lastUploadDate = new Date(smsType.lastSentDate);
        return `Last sent at ${format(lastUploadDate, environment.config.dateFormat)} by ${
          smsType.lastSentBy === this.user?.email ? 'me' : smsType.lastSentBy
        }`;
      }
      if (smsType.lastModifiedDate != null) {
        const lastModifiedDate = new Date(smsType.lastModifiedDate);
        return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
          smsType.lastModifiedBy === this.user?.email ? 'me' : smsType.lastModifiedBy
        }`;
      }
      if (smsType.createdDate != null) {
        const createdDate = new Date(smsType.createdDate);
        return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
          smsType.createdBy === this.user?.email ? 'me' : smsType.createdBy
        }`;
      }

      return '';
    };
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }
}
