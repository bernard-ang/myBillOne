import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { Observable, of, switchMap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { format } from 'date-fns';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import {
  DigitalFilingTypeBasicModel,
  getErrorMessage,
  PageableModel,
  Privilege,
  SearchResultPayloadModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { environment } from '../../../../environments/environment';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { AuthState } from '../../../../states/auth/auth.state';
import { DigitalFilingState } from '../../../../states/digital-filing/digital-filing.state';
import {
  DeleteDigitalFilingType,
  DuplicateDigitalFilingType,
  LoadMoreDigitalFilingTypes,
  QueryDigitalFilingTypes,
  ResetDigitalFilingTypes,
} from '../../../../states/digital-filing/digital-filing.state-actions';
import { hasPrivilege } from '../../../../utils/has-privilege';

@Component({
  selector: 'grabbill-client-digital-filing-list',
  templateUrl: './digital-filing-list.component.html',
  styleUrls: ['./digital-filing-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DigitalFilingListComponent extends NgxsBaseComponent {
  @Select(DigitalFilingState.digitalFilingTypeSearchResult)
  digitalFilingTypeSearchResult$!: Observable<SearchResultPayloadModel<DigitalFilingTypeBasicModel>>;

  @Select(DigitalFilingState.digitalFilingTypePageable)
  digitalFilingTypePageable$!: Observable<PageableModel>;

  user?: UserAuthorityModel;

  isListLoading = false;
  queryAction = QueryDigitalFilingTypes;
  resetAction = ResetDigitalFilingTypes;
  loadMoreAction = LoadMoreDigitalFilingTypes;
  state = DigitalFilingState;

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
        ofActionCompleted(DeleteDigitalFilingType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Digital filing deleted`));
          }
          this.isListLoading = false;
          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DuplicateDigitalFilingType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Digital filing duplicated`));
            this.navigate([
              '/',
              'digital-filing',
              'detail',
              this.store.selectSnapshot(DigitalFilingState.digitalFilingType)!.id,
              'edit',
            ]);
          }
          return of(false);
        })
      )
    );
  }

  doSelect(type: DigitalFilingTypeBasicModel) {
    this.navigate(['/', 'digital-filing', 'detail', type.id], { tab: 'files' });
  }

  doDelete(digitalFilingType: DigitalFilingTypeBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${digitalFilingType.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteDigitalFilingType(digitalFilingType.id));
      },
      nzCancelText: 'No',
    });
  }

  doDuplicate(digitalFilingType: DigitalFilingTypeBasicModel) {
    this.modal.confirm({
      nzTitle: `Duplicate ${digitalFilingType.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new SetPageLoading(true));
        this.store.dispatch(new DuplicateDigitalFilingType(digitalFilingType.id));
      },
      nzCancelText: 'No',
    });
  }

  getMessage() {
    return (digitalFilingType: DigitalFilingTypeBasicModel): string => {
      if (digitalFilingType.lastUploadDate != null) {
        const lastUploadDate = new Date(digitalFilingType.lastUploadDate);
        return `Last uploaded at ${format(lastUploadDate, environment.config.dateFormat)} by ${
          digitalFilingType.lastUploadBy === this.user?.email ? 'me' : digitalFilingType.lastUploadBy
        }`;
      }
      if (digitalFilingType.lastModifiedDate != null) {
        const lastModifiedDate = new Date(digitalFilingType.lastModifiedDate);
        return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
          digitalFilingType.lastModifiedBy === this.user?.email ? 'me' : digitalFilingType.lastModifiedBy
        }`;
      }
      if (digitalFilingType.createdDate != null) {
        const createdDate = new Date(digitalFilingType.createdDate);
        return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
          digitalFilingType.createdBy === this.user?.email ? 'me' : digitalFilingType.createdBy
        }`;
      }

      return '';
    };
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }
}
