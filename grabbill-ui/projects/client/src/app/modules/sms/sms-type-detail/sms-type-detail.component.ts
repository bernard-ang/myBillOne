import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import {
  BaseIndexRowModel,
  getErrorMessage,
  PageableModel,
  Privilege,
  ProcessStatus,
  SearchResultPayloadModel,
  SmsActivityBasicModel, SmsFieldType,
  SmsTypeModel,
  UserAuthorityModel
} from "@grabbill/lib";
import { BehaviorSubject, Observable, of, switchMap, tap } from 'rxjs';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { SmsState } from '../../../../states/sms/sms.state';
import { AuthState } from '../../../../states/auth/auth.state';
import {
  DeleteSmsActivity,
  GetSmsType,
  LoadMoreSmsActivities,
  QuerySmsActivities,
  ResetSmsType,
} from '../../../../states/sms/sms.state-actions';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ActivatedRoute, Params } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { environment } from '../../../../environments/environment';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { createSms, handleNewSms } from '../../../../utils/sms';
import { getCsvSeparatorLabel } from '../../../../utils/csv-separator';
import { format } from 'date-fns';
import { getCode } from 'projects/client/src/utils/get-code';
import { getStatusTag } from 'projects/client/src/utils/get-status-tag';
import { getIndexFieldLabels } from 'projects/client/src/utils/get-index-field-labels';
import { getIndexRowValues } from 'projects/client/src/utils/get-index-row-values';
import { hasPrivilege } from 'projects/client/src/utils/has-privilege';

@Component({
  selector: 'grabbill-client-sms-type-detail',
  templateUrl: './sms-type-detail.component.html',
  styleUrls: ['./sms-type-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SmsTypeDetailComponent extends NgxsBaseComponent {
  typeId?: number;
  user?: UserAuthorityModel;

  isInitialize = false;

  isFilterModalVisible = false;
  filterForm: UntypedFormGroup;
  fileFilters: { [index: string]: any } = {};
  attachmentFieldIndex = -1;
  passwordFieldIndex = -1;

  @Select(SmsState.smsType)
  smsType$!: Observable<SmsTypeModel>;

  @Select(SmsState.smsActivityPageable)
  smsActivityPageable$!: Observable<PageableModel>;

  @Select(SmsState.smsActivitySearchResult)
  smsActivitySearchResult$!: Observable<SearchResultPayloadModel<SmsActivityBasicModel>>;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  isListLoading = false;
  listQueryAction = QuerySmsActivities;
  listLoadMoreAction = LoadMoreSmsActivities;
  listState = SmsState;
  smsFieldType = SmsFieldType;

  constructor(
    protected override messageService: NzMessageService,
    public override store: Store,
    public actions$: Actions,
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private modal: NzModalService
  ) {
    super(store, messageService);
    this.filterForm = this.fb.group({
      field: ['', [Validators.required]],
      type: [''],
      textValue: [undefined, [Validators.required]],
      numberValue: [undefined, [Validators.required]],
      dateValue: [undefined, [Validators.required]],
    });
  }

  public get privilege(): typeof Privilege {
    return Privilege;
  }

  public get dateFormat(): string {
    return environment.config.dateFormat;
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetSmsType());

    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.store.dispatch(new GetSmsType(this.typeId!));
          this.store.dispatch(new QuerySmsActivities(this.typeId!));
        })
      ),
      this.smsType$.pipe(
        tap((type) => {
          if (type) {
            this.isInitialize = true;
            const indexFieldLabels = getIndexFieldLabels(type);
            for (let i = 0; i < indexFieldLabels.length; i++) {
              const indexFieldLabel = indexFieldLabels[i];
              if (indexFieldLabel === 'Attachment Filename') {
                this.attachmentFieldIndex = i;
              }

              if (indexFieldLabel === 'Attachment Password') {
                this.passwordFieldIndex = i;
              }
            }
            this.cd.markForCheck();
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
      handleNewSms(this.actions$, this.store, this.navigate.bind(this)),
      this.actions$.pipe(
        ofActionCompleted(DeleteSmsActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity deleted`));
          }
          return of(false);
        })
      )
    );
  }

  getCode(smsType: SmsTypeModel): string {
    return getCode(smsType.code);
  }

  getStatusTag(activity: SmsActivityBasicModel): string {
    return getStatusTag(activity.status);
  }

  doEdit(typeId: number) {
    this.navigate(['/', 'sms', 'detail', typeId, 'edit']);
  }

  doViewActivity(item: SmsActivityBasicModel) {
    if (item.status === ProcessStatus.DRAFT) {
      this.doEditActivity(this.typeId!, item.id);
    } else {
      this.navigate(['/', 'sms', 'detail', this.typeId!, 'activity', item.id], { tab: 'records' });
    }
  }

  doEditActivity(typeId: number, activityId: number) {
    this.navigate(['/', 'sms', 'detail', typeId, 'activity', activityId, 'edit']);
  }

  doCreateSms(type: SmsTypeModel, smsName: string) {
    createSms(this.store, type, smsName);
  }

  doDeleteActivity(typeId: number, activity: SmsActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${activity.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteSmsActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  getIndexFieldLabels(smsType: SmsTypeModel) {
    return getIndexFieldLabels(smsType);
  }

  getIndexRowValues(row: BaseIndexRowModel, smsType: SmsTypeModel) {
    return getIndexRowValues(row, smsType);
  }

  getActivityStatusText(smsActivity: SmsActivityBasicModel): string {
    if (smsActivity.lastModifiedDate != null) {
      const lastModifiedDate = new Date(smsActivity.lastModifiedDate);
      return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
        smsActivity.lastModifiedBy === this.user?.email ? 'me' : smsActivity.lastModifiedBy
      }`;
    }
    if (smsActivity.createdDate != null) {
      const createdDate = new Date(smsActivity.createdDate);
      return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
        smsActivity.createdBy === this.user?.email ? 'me' : smsActivity.createdBy
      }`;
    }

    return '';
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }

  getApplicableFields(type: SmsTypeModel) {
    return type.indexFields.filter((field) => field.applicable);
  }
}
