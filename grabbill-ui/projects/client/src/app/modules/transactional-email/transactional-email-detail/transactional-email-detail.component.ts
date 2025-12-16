import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Params } from '@angular/router';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import { saveAs } from 'file-saver';
import { format } from 'date-fns';
import produce from 'immer';
import {
  BaseIndexRowModel,
  DataType,
  getErrorMessage,
  PageableModel,
  Privilege,
  ProcessStatus,
  SearchResultPayloadModel,
  TransactionalEmailActivityBasicModel,
  TransactionalEmailTypeModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { environment } from '../../../../environments/environment';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { AuthState } from '../../../../states/auth/auth.state';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { TransactionalEmailState } from '../../../../states/transactional-email/transactional-email.state';
import {
  DeleteTransactionalEmailActivity,
  DeleteTransactionalEmailFile,
  DownloadTransactionalEmailFile,
  ExportTransactionalEmailActivity,
  GetTransactionalEmailType,
  LoadMoreTransactionalEmailActivities,
  PurgeTransactionalEmailActivity,
  QueryTransactionalEmailActivities,
  QueryTransactionalEmailTypeFiles,
  ResetTransactionalEmailType,
} from '../../../../states/transactional-email/transactional-email.state-actions';
import { getCsvSeparatorLabel } from '../../../../utils/csv-separator';
import { getCode } from '../../../../utils/get-code';
import { getIndexFieldLabels } from '../../../../utils/get-index-field-labels';
import { getIndexRowValues } from '../../../../utils/get-index-row-values';
import { getStatusTag } from '../../../../utils/get-status-tag';
import { hasPrivilege } from '../../../../utils/has-privilege';
import { filter } from 'rxjs/operators';
import { createTransactionalEmail, handleNewTransactionEmail } from '../../../../utils/transactional-email';

@Component({
  selector: 'grabbill-client-transactional-email-detail',
  templateUrl: './transactional-email-detail.component.html',
  styleUrls: ['./transactional-email-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransactionalEmailDetailComponent extends NgxsBaseComponent {
  typeId?: number;
  user?: UserAuthorityModel;

  isInitialize = false;
  isFileTableLoading = false;

  isFilterModalVisible = false;
  filenameSearchChange$ = new BehaviorSubject('');
  filterForm: UntypedFormGroup;
  fileFilters: { [index: string]: any } = {};
  showPassword = false;
  attachmentFieldIndex = -1;
  passwordFieldIndex = -1;

  @Select(TransactionalEmailState.transactionalEmailType)
  transactionalEmailType$!: Observable<TransactionalEmailTypeModel>;

  @Select(TransactionalEmailState.transactionalEmailActivityPageable)
  transactionalEmailActivityPageable$!: Observable<PageableModel>;

  @Select(TransactionalEmailState.transactionalEmailActivitySearchResult)
  transactionalEmailActivitySearchResult$!: Observable<SearchResultPayloadModel<TransactionalEmailActivityBasicModel>>;

  @Select(TransactionalEmailState.transactionalEmailFilePageable)
  transactionalEmailFilePageable$!: Observable<PageableModel>;

  @Select(TransactionalEmailState.transactionalEmailFileSearchResult)
  fileSearchResult$!: Observable<SearchResultPayloadModel<BaseIndexRowModel>>;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  isListLoading = false;
  listQueryAction = QueryTransactionalEmailActivities;
  listLoadMoreAction = LoadMoreTransactionalEmailActivities;
  listState = TransactionalEmailState;

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
      textValue: [undefined, [Validators.required, Validators.maxLength(255)]],
      numberValue: [undefined, [Validators.required, Validators.max(99999999999)]],
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
    this.store.dispatch(new ResetTransactionalEmailType());

    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.store.dispatch(new GetTransactionalEmailType(this.typeId!));
          this.store.dispatch(new QueryTransactionalEmailActivities(this.typeId!));
        })
      ),
      this.transactionalEmailType$.pipe(
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
      this.fileSearchResult$.pipe(
        tap(() => {
          this.isFileTableLoading = false;
        })
      ),
      this.filenameSearchChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((name: string) => {
            this.isFileTableLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new QueryTransactionalEmailTypeFiles(
                this.typeId!,
                this.store.selectSnapshot(TransactionalEmailState.transactionalEmailFilePageable),
                name,
                this.store.selectSnapshot(TransactionalEmailState.fileFilters)
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(GetTransactionalEmailType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'transactional-email', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(QueryTransactionalEmailTypeFiles),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.isFileTableLoading = false;
          }
          return of(false);
        })
      ),
      handleNewTransactionEmail(this.actions$, this.store, this.navigate.bind(this)),
      this.actions$.pipe(
        ofActionCompleted(ExportTransactionalEmailActivity, DownloadTransactionalEmailFile),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(TransactionalEmailState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteTransactionalEmailActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity deleted`));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(PurgeTransactionalEmailActivity, DeleteTransactionalEmailFile),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity file(s) deleted`));
            this.store.dispatch(new QueryTransactionalEmailActivities(this.typeId!));
            this.store.dispatch(new QueryTransactionalEmailTypeFiles(this.typeId!));
          }
          return of(false);
        })
      )
    );
  }

  getCode(transactionalEmailType: TransactionalEmailTypeModel): string {
    return getCode(transactionalEmailType.code);
  }

  getStatusTag(activity: TransactionalEmailActivityBasicModel): string {
    return getStatusTag(activity.status);
  }

  getSeparatorLabel(csvSeparator: string): string {
    return getCsvSeparatorLabel(csvSeparator);
  }

  doEdit(typeId: number) {
    this.navigate(['/', 'transactional-email', 'detail', typeId, 'edit']);
  }

  doViewActivity(item: TransactionalEmailActivityBasicModel) {
    if (item.status === ProcessStatus.DRAFT) {
      this.doEditActivity(this.typeId!, item.id);
    } else {
      this.navigate(['/', 'transactional-email', 'detail', this.typeId!, 'activity', item.id], { tab: 'records' });
    }
  }

  doEditActivity(typeId: number, activityId: number) {
    this.navigate(['/', 'transactional-email', 'detail', typeId, 'activity', activityId, 'edit']);
  }

  doCreateTransactionalEmail(type: TransactionalEmailTypeModel, transactionalEmailName: string) {
    createTransactionalEmail(this.store, type, transactionalEmailName);
  }

  doDeleteActivity(typeId: number, activity: TransactionalEmailActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${activity.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteTransactionalEmailActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  doExportActivity(typeId: number, activity: TransactionalEmailActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Export ${activity.name} Files`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new SetPageLoading(true));
        this.store.dispatch(new ExportTransactionalEmailActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  doPurgeActivity(typeId: number, activity: TransactionalEmailActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Purge ${activity.name} Files`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new PurgeTransactionalEmailActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  getIndexFieldLabels(transactionalEmailType: TransactionalEmailTypeModel) {
    return getIndexFieldLabels(transactionalEmailType);
  }

  getIndexRowValues(row: BaseIndexRowModel, transactionalEmailType: TransactionalEmailTypeModel) {
    return getIndexRowValues(row, transactionalEmailType);
  }

  getActivityStatusText(transactionalEmailActivity: TransactionalEmailActivityBasicModel): string {
    if (transactionalEmailActivity.lastModifiedDate != null) {
      const lastModifiedDate = new Date(transactionalEmailActivity.lastModifiedDate);
      return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
        transactionalEmailActivity.lastModifiedBy === this.user?.email
          ? 'me'
          : transactionalEmailActivity.lastModifiedBy
      }`;
    }
    if (transactionalEmailActivity.createdDate != null) {
      const createdDate = new Date(transactionalEmailActivity.createdDate);
      return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
        transactionalEmailActivity.createdBy === this.user?.email ? 'me' : transactionalEmailActivity.createdBy
      }`;
    }

    return '';
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }

  doSearchFilename(event: any) {
    this.filenameSearchChange$.next(event.target.value);
  }

  doQueryFile(event: NzTableQueryParams) {
    this.isFileTableLoading = true;
    this.cd.markForCheck();
    const pageable = produce(
      this.store.selectSnapshot(TransactionalEmailState.transactionalEmailFilePageable),
      (draft) => {
        draft.page = event.pageIndex;
      }
    );
    this.store.dispatch(
      new QueryTransactionalEmailTypeFiles(
        this.typeId!,
        pageable,
        this.store.selectSnapshot(TransactionalEmailState.fileName),
        this.store.selectSnapshot(TransactionalEmailState.fileFilters)
      )
    );
  }

  doDeleteFile(indexRow: BaseIndexRowModel, transactionalEmailType: TransactionalEmailTypeModel) {
    this.modal.confirm({
      nzTitle: `Delete ${indexRow.text1}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isFileTableLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(
          new DeleteTransactionalEmailFile(transactionalEmailType.id, indexRow.activityId, indexRow.file!.id!)
        );
      },
      nzCancelText: 'No',
    });
  }

  doDownloadFile(row: BaseIndexRowModel) {
    this.store.dispatch(new SetPageLoading(true));
    this.store.dispatch(
      new DownloadTransactionalEmailFile(this.typeId!, row.activityId, row.file?.id!, row.file?.name!)
    );
  }

  doOpenFilterModal(type: TransactionalEmailTypeModel) {
    this.filterForm.reset();
    this.filterForm.get('field')!.setValue('idxf2');
    this.updateFilterType(type, 'idxf2');
    this.isFilterModalVisible = true;
    this.cd.markForCheck();
  }

  updateFilterType(type: TransactionalEmailTypeModel, fieldName: string) {
    const indexField = this.getIndexFieldByFieldName(fieldName, type);
    const value = indexField.dataType;
    this.filterForm.get('type')!.setValue(value);
    this.cd.markForCheck();
  }

  getIndexFieldByFieldName(fieldName: string, type: TransactionalEmailTypeModel) {
    const idx = parseInt(fieldName.replace('idxf', ''));
    return type.indexFields[idx - 1];
  }

  getIndexFieldValue(value: any) {
    if (value instanceof Date) {
      return format(value, 'dd/MM/yyyy');
    }
    return value;
  }

  doCloseFilterModal() {
    this.isFilterModalVisible = false;
    this.cd.markForCheck();
  }

  doFilterIndexFieldChange(type: TransactionalEmailTypeModel, value: string) {
    this.updateFilterType(type, value);
  }

  doAddFilter() {
    this.isFileTableLoading = true;
    this.cd.markForCheck();
    let updateFilters = { ...this.fileFilters };
    const field = this.filterForm.get('field')!.value;
    const type = this.filterForm.get('type')!.value;
    if (type === DataType.TEXT) {
      updateFilters[field] = this.filterForm.get('textValue')!.value;
    } else if (type === DataType.NUMBER) {
      updateFilters[field] = this.filterForm.get('numberValue')!.value;
    } else if (type === DataType.DATE) {
      const dateValue = this.filterForm.get('dateValue')!.value;
      const targetDate = new Date(dateValue);
      targetDate.setHours(0, 0, 0, 0);
      updateFilters[field] = targetDate;
    }

    this.fileFilters = updateFilters;

    this.isFilterModalVisible = false;
    this.cd.markForCheck();

    this.store.dispatch(
      new QueryTransactionalEmailTypeFiles(
        this.typeId!,
        {
          ...this.store.selectSnapshot(TransactionalEmailState.transactionalEmailFilePageable),
          page: 0,
        },
        this.store.selectSnapshot(TransactionalEmailState.fileName),
        this.fileFilters
      )
    );
  }

  getApplicableFields(type: TransactionalEmailTypeModel) {
    return type.indexFields.filter((field) => field.applicable);
  }

  getFilters() {
    return Object.entries(this.fileFilters);
  }

  doRemoveFilter(filter: string) {
    this.isFileTableLoading = true;
    this.cd.markForCheck();
    let updateFilters: { [index: string]: any } = {};
    for (const entry of Object.entries(this.fileFilters)) {
      if (entry[0] !== filter) {
        updateFilters[entry[0]] = entry[1];
      }
    }
    this.fileFilters = updateFilters;

    this.store.dispatch(
      new QueryTransactionalEmailTypeFiles(
        this.typeId!,
        this.store.selectSnapshot(TransactionalEmailState.transactionalEmailFilePageable),
        this.store.selectSnapshot(TransactionalEmailState.fileName),
        this.fileFilters
      )
    );
  }

  togglePasswordVisible() {
    this.showPassword = !this.showPassword;
    this.cd.markForCheck();
  }
}
