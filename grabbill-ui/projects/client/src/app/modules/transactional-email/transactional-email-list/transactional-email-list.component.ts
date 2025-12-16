import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { Observable, of, switchMap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { format } from 'date-fns';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import {
  getErrorMessage,
  PageableModel,
  Privilege,
  SearchResultPayloadModel,
  TransactionalEmailTypeBasicModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { environment } from '../../../../environments/environment';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { AuthState } from '../../../../states/auth/auth.state';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { TransactionalEmailState } from '../../../../states/transactional-email/transactional-email.state';
import {
  DeleteTransactionalEmailType,
  DuplicateTransactionalEmailType,
  LoadMoreTransactionalEmailTypes,
  QueryTransactionalEmailTypes,
  ResetTransactionalEmailTypes,
} from '../../../../states/transactional-email/transactional-email.state-actions';
import { hasPrivilege } from '../../../../utils/has-privilege';

@Component({
  selector: 'grabbill-client-transactional-email-list',
  templateUrl: './transactional-email-list.component.html',
  styleUrls: ['./transactional-email-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransactionalEmailListComponent extends NgxsBaseComponent {
  @Select(TransactionalEmailState.transactionalEmailTypeSearchResult)
  transactionalEmailTypeSearchResult$!: Observable<SearchResultPayloadModel<TransactionalEmailTypeBasicModel>>;

  @Select(TransactionalEmailState.transactionalEmailTypePageable)
  transactionalEmailTypePageable$!: Observable<PageableModel>;

  user?: UserAuthorityModel;

  isListLoading = false;
  queryAction = QueryTransactionalEmailTypes;
  resetAction = ResetTransactionalEmailTypes;
  loadMoreAction = LoadMoreTransactionalEmailTypes;
  state = TransactionalEmailState;

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
        ofActionCompleted(DeleteTransactionalEmailType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Transactional email deleted`));
          }
          this.isListLoading = false;
          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DuplicateTransactionalEmailType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Transactional email duplicated`));
            this.navigate([
              '/',
              'transactional-email',
              'detail',
              this.store.selectSnapshot(TransactionalEmailState.transactionalEmailType)!.id,
              'edit',
            ]);
          }
          return of(false);
        })
      )
    );
  }

  doSelect(type: TransactionalEmailTypeBasicModel) {
    this.navigate(['/', 'transactional-email', 'detail', type.id], { tab: 'activities' });
  }

  doDelete(transactionalEmailType: TransactionalEmailTypeBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${transactionalEmailType.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteTransactionalEmailType(transactionalEmailType.id));
      },
      nzCancelText: 'No',
    });
  }

  doDuplicate(transactionalEmailType: TransactionalEmailTypeBasicModel) {
    this.modal.confirm({
      nzTitle: `Duplicate ${transactionalEmailType.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new SetPageLoading(true));
        this.store.dispatch(new DuplicateTransactionalEmailType(transactionalEmailType.id));
      },
      nzCancelText: 'No',
    });
  }

  getMessage() {
    return (transactionalEmailType: TransactionalEmailTypeBasicModel): string => {
      if (transactionalEmailType.lastSentDate != null) {
        const lastUploadDate = new Date(transactionalEmailType.lastSentDate);
        return `Last sent at ${format(lastUploadDate, environment.config.dateFormat)} by ${
          transactionalEmailType.lastSentBy === this.user?.email ? 'me' : transactionalEmailType.lastSentBy
        }`;
      }
      if (transactionalEmailType.lastModifiedDate != null) {
        const lastModifiedDate = new Date(transactionalEmailType.lastModifiedDate);
        return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
          transactionalEmailType.lastModifiedBy === this.user?.email ? 'me' : transactionalEmailType.lastModifiedBy
        }`;
      }
      if (transactionalEmailType.createdDate != null) {
        const createdDate = new Date(transactionalEmailType.createdDate);
        return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
          transactionalEmailType.createdBy === this.user?.email ? 'me' : transactionalEmailType.createdBy
        }`;
      }

      return '';
    };
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }
}
