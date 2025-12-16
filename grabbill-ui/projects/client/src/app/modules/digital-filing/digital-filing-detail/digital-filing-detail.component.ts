import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { saveAs } from 'file-saver';
import { format } from 'date-fns';
import produce from 'immer';
import {
  BaseIndexRowModel,
  DataType,
  DigitalFilingActivityBasicModel,
  DigitalFilingTypeModel,
  getErrorMessage,
  PageableModel,
  Privilege,
  ProcessStatus,
  SearchResultPayloadModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { environment } from '../../../../environments/environment';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { AuthState } from '../../../../states/auth/auth.state';
import {
  DeleteDigitalFilingActivity,
  DeleteDigitalFilingFile,
  DownloadDigitalFilingFile,
  DownloadDigitalFilingFiles,
  ExportDigitalFilingActivity,
  GetDigitalFilingType,
  LoadMoreDigitalFilingActivities,
  PurgeDigitalFilingActivity,
  QueryDigitalFilingActivities,
  QueryDigitalFilingTypeFiles,
  ResetDigitalFilingType,
} from '../../../../states/digital-filing/digital-filing.state-actions';
import { DigitalFilingState } from '../../../../states/digital-filing/digital-filing.state';
import { getCsvSeparatorLabel } from '../../../../utils/csv-separator';
import { getCode } from '../../../../utils/get-code';
import { getIndexRowValues } from '../../../../utils/get-index-row-values';
import { getIndexFieldLabels } from '../../../../utils/get-index-field-labels';
import { getStatusTag } from '../../../../utils/get-status-tag';
import { hasPrivilege } from '../../../../utils/has-privilege';
import { filter } from 'rxjs/operators';
import { createDigitalFiling, handleNewDigitalFiling } from '../../../../utils/digital-filing';
import { isArrayDates } from '../../../../utils/array';

@Component({
  selector: 'grabbill-client-digital-filing-detail',
  templateUrl: './digital-filing-detail.component.html',
  styleUrls: ['./digital-filing-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DigitalFilingDetailComponent extends NgxsBaseComponent {
  typeId?: number;
  user?: UserAuthorityModel;

  isInitialize = false;

  isActivityListLoading = false;
  queryAction = QueryDigitalFilingActivities;
  loadMoreAction = LoadMoreDigitalFilingActivities;
  state = DigitalFilingState;

  isFileTableLoading = false;

  isFilterModalVisible = false;
  filterForm: UntypedFormGroup;
  fileFilters: { [index: string]: any } = {};

  filenameSearchChange$ = new BehaviorSubject('');

  @Select(DigitalFilingState.digitalFilingActivityPageable)
  digitalFilingActivityPageable$!: Observable<PageableModel>;

  @Select(DigitalFilingState.digitalFilingType)
  digitalFilingType$!: Observable<DigitalFilingTypeModel>;

  @Select(DigitalFilingState.digitalFilingActivitySearchResult)
  digitalFilingActivitySearchResult$!: Observable<SearchResultPayloadModel<DigitalFilingActivityBasicModel>>;

  @Select(DigitalFilingState.digitalFilingFilePageable)
  digitalFilingFilePageable$!: Observable<PageableModel>;

  @Select(DigitalFilingState.digitalFilingFileSearchResult)
  digitalFilingFileSearchResult$!: Observable<SearchResultPayloadModel<BaseIndexRowModel>>;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private modal: NzModalService,
    public actions$: Actions,
    public override store: Store,
    override messageService: NzMessageService
  ) {
    super(store, messageService);
    this.filterForm = this.fb.group({
      field: ['', [Validators.required]],
      type: [''],
      textValue: [undefined, [Validators.required, Validators.maxLength(255)]],
      numberValue: [undefined, [Validators.required, Validators.max(99999999999)]],
      dateValue: [undefined, [Validators.required]],
      endDateValue: [undefined],
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
    this.store.dispatch(new ResetDigitalFilingType());

    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.store.dispatch(new GetDigitalFilingType(this.typeId!));
          this.store.dispatch(new QueryDigitalFilingActivities(this.typeId!));
        })
      ),
      this.digitalFilingType$.pipe(
        tap((type) => {
          if (type) {
            this.isInitialize = true;
          }
        })
      ),
      this.digitalFilingFileSearchResult$.pipe(
        tap(() => {
          this.isFileTableLoading = false;
        })
      ),
      this.digitalFilingActivitySearchResult$.pipe(
        tap(() => {
          this.isActivityListLoading = false;
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
              new QueryDigitalFilingTypeFiles(
                this.typeId!,
                this.store.selectSnapshot(DigitalFilingState.digitalFilingFilePageable),
                name,
                this.store.selectSnapshot(DigitalFilingState.fileFilters)
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(GetDigitalFilingType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'digital-filing', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(QueryDigitalFilingActivities, LoadMoreDigitalFilingActivities),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.isActivityListLoading = false;
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(QueryDigitalFilingTypeFiles),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.isFileTableLoading = false;
          }
          return of(false);
        })
      ),
      handleNewDigitalFiling(this.actions$, this.store, this.navigate.bind(this)),
      this.actions$.pipe(
        ofActionCompleted(ExportDigitalFilingActivity, DownloadDigitalFilingFile, DownloadDigitalFilingFiles),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(DigitalFilingState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteDigitalFilingActivity),
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
        ofActionCompleted(PurgeDigitalFilingActivity, DeleteDigitalFilingFile),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity file(s) deleted`));
            this.store.dispatch(new QueryDigitalFilingActivities(this.typeId!));
            this.store.dispatch(new QueryDigitalFilingTypeFiles(this.typeId!));
          }
          return of(false);
        })
      )
    );
  }

  getCode(digitalFilingType: DigitalFilingTypeModel): string {
    return getCode(digitalFilingType.code);
  }

  getStatusTag(activity: DigitalFilingActivityBasicModel): string {
    return getStatusTag(activity.status);
  }

  getSeparatorLabel(csvSeparator: string): string {
    return getCsvSeparatorLabel(csvSeparator);
  }

  doEdit(typeId: number) {
    this.navigate(['/', 'digital-filing', 'detail', typeId, 'edit']);
  }

  doViewActivity(typeId: number, activityId: number, status: ProcessStatus) {
    if (status === ProcessStatus.DRAFT) {
      this.doEditActivity(typeId, activityId);
    } else {
      this.navigate(['/', 'digital-filing', 'detail', typeId, 'activity', activityId], { tab: 'records' });
    }
  }

  doEditActivity(typeId: number, activityId: number) {
    this.navigate(['/', 'digital-filing', 'detail', typeId, 'activity', activityId, 'edit']);
  }

  doQueryFile(event: NzTableQueryParams) {
    this.isFileTableLoading = true;
    this.cd.markForCheck();
    const pageable = produce(this.store.selectSnapshot(DigitalFilingState.digitalFilingFilePageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(
      new QueryDigitalFilingTypeFiles(
        this.typeId!,
        pageable,
        this.store.selectSnapshot(DigitalFilingState.fileName),
        this.store.selectSnapshot(DigitalFilingState.fileFilters)
      )
    );
  }

  doSearchFilename(event: any) {
    this.filenameSearchChange$.next(event.target.value);
  }

  doDeleteFile(indexRow: BaseIndexRowModel, digitalFilingType: DigitalFilingTypeModel) {
    this.modal.confirm({
      nzTitle: `Delete ${indexRow.text1}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isFileTableLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteDigitalFilingFile(digitalFilingType.id, indexRow.activityId, indexRow.file?.id!));
      },
      nzCancelText: 'No',
    });
  }

  doCreateDigitalFilingTemplate(typeId: number, digitalFilingName: string) {
    createDigitalFiling(this.store, typeId, digitalFilingName);
  }

  doDeleteActivity(typeId: number, activity: DigitalFilingActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${activity.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isActivityListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteDigitalFilingActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  doExportActivity(typeId: number, activity: DigitalFilingActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Export ${activity.name} Files`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new SetPageLoading(true));
        this.store.dispatch(new ExportDigitalFilingActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  doPurgeActivity(typeId: number, activity: DigitalFilingActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Purge ${activity.name} Files`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isActivityListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new PurgeDigitalFilingActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  getIndexFieldLabels(digitalFileType: DigitalFilingTypeModel) {
    return getIndexFieldLabels(digitalFileType);
  }

  getIndexRowValues(row: BaseIndexRowModel, digitalFileType: DigitalFilingTypeModel) {
    return getIndexRowValues(row, digitalFileType);
  }

  doDownloadFile(row: BaseIndexRowModel) {
    this.store.dispatch(new SetPageLoading(true));
    this.store.dispatch(new DownloadDigitalFilingFile(this.typeId!, row.activityId, row.file?.id!, row.file?.name!));
  }

  getActivityStatusText(digitalFilingActivity: DigitalFilingActivityBasicModel): string {
    if (digitalFilingActivity.lastModifiedDate != null) {
      const lastModifiedDate = new Date(digitalFilingActivity.lastModifiedDate);
      return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
        digitalFilingActivity.lastModifiedBy === this.user?.email ? 'me' : digitalFilingActivity.lastModifiedBy
      }`;
    }
    if (digitalFilingActivity.createdDate != null) {
      const createdDate = new Date(digitalFilingActivity.createdDate);
      return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
        digitalFilingActivity.createdBy === this.user?.email ? 'me' : digitalFilingActivity.createdBy
      }`;
    }

    return '';
  }

  getMessage() {
    return (digitalFilingActivity: DigitalFilingActivityBasicModel): string => {
      return this.getActivityStatusText(digitalFilingActivity);
    };
  }

  doSelect(digitalFilingActivity: DigitalFilingActivityBasicModel) {
    return this.doViewActivity(this.typeId!, digitalFilingActivity.id, digitalFilingActivity.status);
  }

  doOpenFilterModal(type: DigitalFilingTypeModel) {
    this.filterForm.reset();
    this.filterForm.get('field')!.setValue('idxf2');
    this.updateFilterType(type, 'idxf2');
    this.isFilterModalVisible = true;
    this.cd.markForCheck();
  }

  doDownloadFiles(type: DigitalFilingTypeModel) {
    this.store.dispatch(new SetPageLoading(true));
    this.store.dispatch(new DownloadDigitalFilingFiles(type.id));
  }

  updateFilterType(type: DigitalFilingTypeModel, fieldName: string) {
    const indexField = this.getIndexFieldByFieldName(fieldName, type);
    const value = indexField.dataType;
    this.filterForm.get('type')!.setValue(value);
    this.cd.markForCheck();
  }

  getIndexFieldByFieldName(fieldName: string, type: DigitalFilingTypeModel) {
    const idx = parseInt(fieldName.replace('idxf', ''));
    return type.indexFields[idx - 1];
  }

  getIndexFieldValue(value: any) {
    if (isArrayDates(value)) {
      return `${format(value[0], 'dd/MM/yyyy')} - ${format(value[1], 'dd/MM/yyyy')}`;
    } else if (value instanceof Date) {
      return format(value, 'dd/MM/yyyy');
    }
    return value;
  }

  doCloseFilterModal() {
    this.isFilterModalVisible = false;
    this.cd.markForCheck();
  }

  doFilterIndexFieldChange(type: DigitalFilingTypeModel, value: string) {
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

      const endDateValue = this.filterForm.get('endDateValue')!.value;
      const targetEndDate = endDateValue ? new Date(endDateValue) : new Date();
      targetEndDate.setHours(23, 59, 59, 0);
      updateFilters[field] = [targetDate, targetEndDate];
    }

    this.fileFilters = updateFilters;

    this.isFilterModalVisible = false;
    this.cd.markForCheck();

    this.store.dispatch(
      new QueryDigitalFilingTypeFiles(
        this.typeId!,
        {
          ...this.store.selectSnapshot(DigitalFilingState.digitalFilingFilePageable),
          page: 0,
        },
        this.store.selectSnapshot(DigitalFilingState.fileName),
        this.fileFilters
      )
    );
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
      new QueryDigitalFilingTypeFiles(
        this.typeId!,
        this.store.selectSnapshot(DigitalFilingState.digitalFilingFilePageable),
        this.store.selectSnapshot(DigitalFilingState.fileName),
        this.fileFilters
      )
    );
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }
}
