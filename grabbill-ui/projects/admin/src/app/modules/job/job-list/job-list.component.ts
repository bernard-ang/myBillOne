import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import { NzModalService } from 'ng-zorro-antd/modal';
import { filter } from 'rxjs/operators';
import produce from 'immer';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import {
  DomainType,
  getErrorMessage,
  getJobStatusTag,
  JobBasicModel,
  JobStatus,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { environment } from '../../../../environments/environment';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { JobManagementState } from '../../../../states/job-management/job-management.state';
import { QueryJobs, ResetJobs, RetryJob } from '../../../../states/job-management/job-management.state-actions';
import { ShowMessage } from '../../../../states/admin-common/admin-common.state-actions';

@Component({
  selector: 'grabbill-admin-job-list',
  templateUrl: './job-list.component.html',
  styleUrls: ['./job-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JobListComponent extends NgxsBaseComponent {
  @Select(JobManagementState.jobSearchResult)
  jobSearchResult$!: Observable<SearchResultPayloadModel<JobBasicModel>>;

  @Select(JobManagementState.jobPageable)
  jobPageable$!: Observable<PageableModel>;

  isInitialize = false;
  isTableLoading = false;
  searchChange$ = new BehaviorSubject('');
  dateRangeChange$: BehaviorSubject<(Date | null)[]> = new BehaviorSubject<(Date | null)[]>([]);
  form: UntypedFormGroup;

  isFilterModalVisible = false;
  filterForm: UntypedFormGroup;
  filters: { [index: string]: any } = {};
  filterOptions = [
    {
      label: 'Domain Type',
      key: 'domainType',
      type: 'OPTION',
      options: Object.values(DomainType),
    },
    {
      label: 'Activity',
      key: 'activity',
      type: 'TEXT',
      options: [],
    },
    {
      label: 'Status',
      key: 'status',
      type: 'OPTION',
      options: Object.values(JobStatus),
    },
  ];
  options: string[] = [];

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private actions$: Actions,
    private modal: NzModalService,
    private cd: ChangeDetectorRef,
    private fb: UntypedFormBuilder
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      range: [],
    });

    this.filterForm = this.fb.group({
      field: ['', [Validators.required]],
      type: [''],
      value: [undefined, [Validators.required]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetJobs());

    this.autoUnsubscribe(
      this.jobSearchResult$.pipe(
        tap(() => {
          this.isTableLoading = false;
        })
      ),
      this.searchChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((accountName: string) => {
            this.isTableLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new QueryJobs(
                {
                  ...this.store.selectSnapshot(JobManagementState.jobPageable),
                  page: 1,
                },
                this.store.selectSnapshot(JobManagementState.filters),
                accountName,
                this.store.selectSnapshot(JobManagementState.startDate),
                this.store.selectSnapshot(JobManagementState.endDate)
              )
            );
          })
        ),
      this.dateRangeChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((range: (Date | null)[]) => {
            this.isTableLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new QueryJobs(
                {
                  ...this.store.selectSnapshot(JobManagementState.jobPageable),
                  page: 1,
                },
                this.store.selectSnapshot(JobManagementState.filters),
                this.store.selectSnapshot(JobManagementState.accountName),
                range[0] ? range[0] : undefined,
                range[1] ? range[1] : undefined
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(RetryJob),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Job retried`));
            this.isTableLoading = true;
            this.store.dispatch(
              new QueryJobs(
                this.store.selectSnapshot(JobManagementState.jobPageable),
                this.store.selectSnapshot(JobManagementState.filters),
                this.store.selectSnapshot(JobManagementState.accountName),
                this.store.selectSnapshot(JobManagementState.startDate),
                this.store.selectSnapshot(JobManagementState.endDate)
              )
            );
            this.cd.markForCheck();
          }

          return of(false);
        })
      )
    );
  }

  doSearch(event: any) {
    this.searchChange$.next(event.target.value);
  }

  doDateRange(event: (Date | null)[]) {
    this.dateRangeChange$.next(event);
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(JobManagementState.jobPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(
      new QueryJobs(
        pageable,
        this.store.selectSnapshot(JobManagementState.filters),
        this.store.selectSnapshot(JobManagementState.accountName),
        this.store.selectSnapshot(JobManagementState.startDate),
        this.store.selectSnapshot(JobManagementState.endDate)
      )
    );
    this.isInitialize = true;
  }

  dateTimeFormat() {
    return environment.config.dateTimeFormat;
  }

  doOpenFilterModal() {
    this.filterForm.reset();
    this.filterForm.get('field')!.setValue('domainType');
    this.doFilterFieldChange('domainType');
    this.isFilterModalVisible = true;
    this.cd.markForCheck();
  }

  doCloseFilterModal() {
    this.isFilterModalVisible = false;
    this.cd.markForCheck();
  }

  doFilterFieldChange(value: string) {
    const option = this.filterOptions.filter((option) => option.key === value)[0];
    this.options = option.options;
    this.filterForm.get('type')!.setValue(option.type);
    this.cd.markForCheck();
  }

  doAddFilter() {
    let updateFilters = { ...this.filters };
    const field = this.filterForm.get('field')!.value;
    updateFilters[field] = this.filterForm.get('value')!.value;

    this.filters = updateFilters;

    this.isTableLoading = true;
    this.isFilterModalVisible = false;
    this.cd.markForCheck();

    this.store.dispatch(
      new QueryJobs(
        this.store.selectSnapshot(JobManagementState.jobPageable),
        this.filters,
        this.store.selectSnapshot(JobManagementState.accountName),
        this.store.selectSnapshot(JobManagementState.startDate),
        this.store.selectSnapshot(JobManagementState.endDate)
      )
    );
  }

  getFilters() {
    return Object.entries(this.filters);
  }

  doRemoveFilter(filter: string) {
    this.isTableLoading = true;
    this.cd.markForCheck();

    let updateFilters: { [index: string]: any } = {};
    for (const entry of Object.entries(this.filters)) {
      if (entry[0] !== filter) {
        updateFilters[entry[0]] = entry[1];
      }
    }
    this.filters = updateFilters;

    this.store.dispatch(
      new QueryJobs(
        this.store.selectSnapshot(JobManagementState.jobPageable),
        this.filters,
        this.store.selectSnapshot(JobManagementState.accountName),
        this.store.selectSnapshot(JobManagementState.startDate),
        this.store.selectSnapshot(JobManagementState.endDate)
      )
    );
  }

  getFilterLabel(filter: string) {
    return filter;
  }

  getStatusTag(job: JobBasicModel): string {
    return getJobStatusTag(job.status);
  }

  doRetry(job: JobBasicModel) {
    this.modal.confirm({
      nzTitle: `Retry ${job.activityName}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new RetryJob(job.id));
      },
      nzCancelText: 'No',
    });
  }
}
