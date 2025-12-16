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
import { hasPrivilege } from '../../../../utils/has-privilege';
import { MultiTemplateTransactionalEmailState } from '../../../../states/multi-template-transactional-email/multi-template-transactional-email.state';
import {
  DeleteMultiTemplateTransactionalEmailType, DuplicateMultiTemplateTransactionalEmailType,
  LoadMoreMultiTemplateTransactionalEmailTypes,
  QueryMultiTemplateTransactionalEmailTypes, ResetMultiTemplateTransactionalEmailTypes
} from "../../../../states/multi-template-transactional-email/multi-template-transactional-email.state-actions";

@Component({
  selector: 'grabbill-client-multi-template-transactional-email-list',
  templateUrl: './multi-template-transactional-email-list.component.html',
  styleUrls: ['./multi-template-transactional-email-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MultiTemplateTransactionalEmailListComponent extends NgxsBaseComponent {
  @Select(MultiTemplateTransactionalEmailState.transactionalEmailTypeSearchResult)
  transactionalEmailTypeSearchResult$!: Observable<SearchResultPayloadModel<TransactionalEmailTypeBasicModel>>;

  @Select(MultiTemplateTransactionalEmailState.transactionalEmailTypePageable)
  transactionalEmailTypePageable$!: Observable<PageableModel>;

  user?: UserAuthorityModel;

  isListLoading = false;
  queryAction = QueryMultiTemplateTransactionalEmailTypes;
  resetAction = ResetMultiTemplateTransactionalEmailTypes;
  loadMoreAction = LoadMoreMultiTemplateTransactionalEmailTypes;
  state = MultiTemplateTransactionalEmailState;

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
        ofActionCompleted(DeleteMultiTemplateTransactionalEmailType),
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
        ofActionCompleted(DuplicateMultiTemplateTransactionalEmailType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Transactional email duplicated`));
            this.navigate([
              '/',
              'mt-transactional-email',
              'detail',
              this.store.selectSnapshot(MultiTemplateTransactionalEmailState.transactionalEmailType)!.id,
              'edit',
            ]);
          }
          return of(false);
        })
      )
    );
  }

  doSelect(type: TransactionalEmailTypeBasicModel) {
    this.navigate(['/', 'mt-transactional-email', 'detail', type.id], { tab: 'activities' });
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
        this.store.dispatch(new DeleteMultiTemplateTransactionalEmailType(transactionalEmailType.id));
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
        this.store.dispatch(new DuplicateMultiTemplateTransactionalEmailType(transactionalEmailType.id));
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
