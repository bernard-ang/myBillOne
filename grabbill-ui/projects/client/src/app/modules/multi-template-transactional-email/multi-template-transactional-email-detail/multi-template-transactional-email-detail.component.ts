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
  MultiTemplateTransactionalEmailTypeModel,
  PageableModel,
  Privilege,
  ProcessStatus,
  SearchResultPayloadModel,
  TransactionalEmailActivityBasicModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { environment } from '../../../../environments/environment';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { AuthState } from '../../../../states/auth/auth.state';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { getCsvSeparatorLabel } from '../../../../utils/csv-separator';
import { getCode } from '../../../../utils/get-code';
import { getIndexFieldLabels } from '../../../../utils/get-index-field-labels';
import { getIndexRowValues } from '../../../../utils/get-index-row-values';
import { getStatusTag } from '../../../../utils/get-status-tag';
import { hasPrivilege } from '../../../../utils/has-privilege';
import { filter } from 'rxjs/operators';
import {
  createMultiTemplateTransactionalEmail,
  handleNewMultiTemplateTransactionEmail,
} from '../../../../utils/transactional-email';
import { MultiTemplateTransactionalEmailState } from '../../../../states/multi-template-transactional-email/multi-template-transactional-email.state';
import {
  DeleteMultiTemplateTransactionalEmailActivity,
  DeleteMultiTemplateTransactionalEmailFile,
  DownloadMultiTemplateTransactionalEmailFile,
  ExportMultiTemplateTransactionalEmailActivity,
  GetMultiTemplateTransactionalEmailType,
  LoadMoreMultiTemplateTransactionalEmailActivities,
  PurgeMultiTemplateTransactionalEmailActivity,
  QueryMultiTemplateTransactionalEmailActivities,
  QueryMultiTemplateTransactionalEmailTypeFiles,
  ResetMultiTemplateTransactionalEmailType,
} from '../../../../states/multi-template-transactional-email/multi-template-transactional-email.state-actions';

@Component({
  selector: 'grabbill-client-multi-template-transactional-email-detail',
  templateUrl: './multi-template-transactional-email-detail.component.html',
  styleUrls: ['./multi-template-transactional-email-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MultiTemplateTransactionalEmailDetailComponent extends NgxsBaseComponent {
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

  @Select(MultiTemplateTransactionalEmailState.transactionalEmailType)
  transactionalEmailType$!: Observable<MultiTemplateTransactionalEmailTypeModel>;

  @Select(MultiTemplateTransactionalEmailState.transactionalEmailActivityPageable)
  transactionalEmailActivityPageable$!: Observable<PageableModel>;

  @Select(MultiTemplateTransactionalEmailState.transactionalEmailActivitySearchResult)
  transactionalEmailActivitySearchResult$!: Observable<SearchResultPayloadModel<TransactionalEmailActivityBasicModel>>;

  @Select(MultiTemplateTransactionalEmailState.transactionalEmailFilePageable)
  transactionalEmailFilePageable$!: Observable<PageableModel>;

  @Select(MultiTemplateTransactionalEmailState.transactionalEmailFileSearchResult)
  fileSearchResult$!: Observable<SearchResultPayloadModel<BaseIndexRowModel>>;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  isListLoading = false;
  listQueryAction = QueryMultiTemplateTransactionalEmailActivities;
  listLoadMoreAction = LoadMoreMultiTemplateTransactionalEmailActivities;
  listState = MultiTemplateTransactionalEmailState;

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
    this.store.dispatch(new ResetMultiTemplateTransactionalEmailType());

    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.store.dispatch(new GetMultiTemplateTransactionalEmailType(this.typeId!));
          this.store.dispatch(new QueryMultiTemplateTransactionalEmailActivities(this.typeId!));
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
              new QueryMultiTemplateTransactionalEmailTypeFiles(
                this.typeId!,
                this.store.selectSnapshot(MultiTemplateTransactionalEmailState.transactionalEmailFilePageable),
                name,
                this.store.selectSnapshot(MultiTemplateTransactionalEmailState.fileFilters)
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(GetMultiTemplateTransactionalEmailType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'mt-transactional-email', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(QueryMultiTemplateTransactionalEmailTypeFiles),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.isFileTableLoading = false;
          }
          return of(false);
        })
      ),
      handleNewMultiTemplateTransactionEmail(this.actions$, this.store, this.navigate.bind(this)),
      this.actions$.pipe(
        ofActionCompleted(ExportMultiTemplateTransactionalEmailActivity, DownloadMultiTemplateTransactionalEmailFile),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(MultiTemplateTransactionalEmailState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteMultiTemplateTransactionalEmailActivity),
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
        ofActionCompleted(PurgeMultiTemplateTransactionalEmailActivity, DeleteMultiTemplateTransactionalEmailFile),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity file(s) deleted`));
            this.store.dispatch(new QueryMultiTemplateTransactionalEmailActivities(this.typeId!));
            this.store.dispatch(new QueryMultiTemplateTransactionalEmailTypeFiles(this.typeId!));
          }
          return of(false);
        })
      )
    );
  }

  getCode(transactionalEmailType: MultiTemplateTransactionalEmailTypeModel): string {
    return getCode(transactionalEmailType.code);
  }

  getStatusTag(activity: TransactionalEmailActivityBasicModel): string {
    return getStatusTag(activity.status);
  }

  getSeparatorLabel(csvSeparator: string): string {
    return getCsvSeparatorLabel(csvSeparator);
  }

  doEdit(typeId: number) {
    this.navigate(['/', 'mt-transactional-email', 'detail', typeId, 'edit']);
  }

  doViewActivity(item: TransactionalEmailActivityBasicModel) {
    if (item.status === ProcessStatus.DRAFT) {
      this.doEditActivity(this.typeId!, item.id);
    } else {
      this.navigate(['/', 'mt-transactional-email', 'detail', this.typeId!, 'activity', item.id], { tab: 'records' });
    }
  }

  doEditActivity(typeId: number, activityId: number) {
    this.navigate(['/', 'mt-transactional-email', 'detail', typeId, 'activity', activityId, 'edit']);
  }

  doCreateTransactionalEmail(type: MultiTemplateTransactionalEmailTypeModel, transactionalEmailName: string) {
    createMultiTemplateTransactionalEmail(this.store, type, transactionalEmailName);
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
        this.store.dispatch(new DeleteMultiTemplateTransactionalEmailActivity(typeId, activity.id));
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
        this.store.dispatch(new ExportMultiTemplateTransactionalEmailActivity(typeId, activity.id));
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
        this.store.dispatch(new PurgeMultiTemplateTransactionalEmailActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  getIndexFieldLabels(transactionalEmailType: MultiTemplateTransactionalEmailTypeModel) {
    return getIndexFieldLabels(transactionalEmailType);
  }

  getIndexRowValues(row: BaseIndexRowModel, transactionalEmailType: MultiTemplateTransactionalEmailTypeModel) {
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
      this.store.selectSnapshot(MultiTemplateTransactionalEmailState.transactionalEmailFilePageable),
      (draft) => {
        draft.page = event.pageIndex;
      }
    );
    this.store.dispatch(
      new QueryMultiTemplateTransactionalEmailTypeFiles(
        this.typeId!,
        pageable,
        this.store.selectSnapshot(MultiTemplateTransactionalEmailState.fileName),
        this.store.selectSnapshot(MultiTemplateTransactionalEmailState.fileFilters)
      )
    );
  }

  doDeleteFile(indexRow: BaseIndexRowModel, transactionalEmailType: MultiTemplateTransactionalEmailTypeModel) {
    this.modal.confirm({
      nzTitle: `Delete ${indexRow.text1}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isFileTableLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(
          new DeleteMultiTemplateTransactionalEmailFile(
            transactionalEmailType.id,
            indexRow.activityId,
            indexRow.file!.id!
          )
        );
      },
      nzCancelText: 'No',
    });
  }

  doDownloadFile(row: BaseIndexRowModel) {
    this.store.dispatch(new SetPageLoading(true));
    this.store.dispatch(
      new DownloadMultiTemplateTransactionalEmailFile(this.typeId!, row.activityId, row.file?.id!, row.file?.name!)
    );
  }

  doOpenFilterModal(type: MultiTemplateTransactionalEmailTypeModel) {
    this.filterForm.reset();
    this.filterForm.get('field')!.setValue('idxf2');
    this.updateFilterType(type, 'idxf2');
    this.isFilterModalVisible = true;
    this.cd.markForCheck();
  }

  updateFilterType(type: MultiTemplateTransactionalEmailTypeModel, fieldName: string) {
    const indexField = this.getIndexFieldByFieldName(fieldName, type);
    const value = indexField.dataType;
    this.filterForm.get('type')!.setValue(value);
    this.cd.markForCheck();
  }

  getIndexFieldByFieldName(fieldName: string, type: MultiTemplateTransactionalEmailTypeModel) {
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

  doFilterIndexFieldChange(type: MultiTemplateTransactionalEmailTypeModel, value: string) {
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
      new QueryMultiTemplateTransactionalEmailTypeFiles(
        this.typeId!,
        {
          ...this.store.selectSnapshot(MultiTemplateTransactionalEmailState.transactionalEmailFilePageable),
          page: 0,
        },
        this.store.selectSnapshot(MultiTemplateTransactionalEmailState.fileName),
        this.fileFilters
      )
    );
  }

  getApplicableFields(type: MultiTemplateTransactionalEmailTypeModel) {
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
      new QueryMultiTemplateTransactionalEmailTypeFiles(
        this.typeId!,
        this.store.selectSnapshot(MultiTemplateTransactionalEmailState.transactionalEmailFilePageable),
        this.store.selectSnapshot(MultiTemplateTransactionalEmailState.fileName),
        this.fileFilters
      )
    );
  }

  togglePasswordVisible() {
    this.showPassword = !this.showPassword;
    this.cd.markForCheck();
  }
}
