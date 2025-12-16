import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import {
  getErrorMessage,
  getInvoiceStatusTag,
  InvoiceBasicModel,
  InvoiceStatus,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { filter } from 'rxjs/operators';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { environment } from '../../../../environments/environment';
import { InvoiceManagementState } from '../../../../states/invoice-management/invoice-management.state';
import {
  DownloadInvoiceReport,
  QueryInvoices,
  ResetInvoices,
} from '../../../../states/invoice-management/invoice-management.state-actions';
import { ShowMessage } from '../../../../../../client/src/states/common/common.state-actions';
import { saveAs } from 'file-saver';

@Component({
  selector: 'grabbill-admin-invoice-list',
  templateUrl: './invoice-list.component.html',
  styleUrls: ['./invoice-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InvoiceListComponent extends NgxsBaseComponent {
  @Select(InvoiceManagementState.invoiceSearchResult)
  invoiceSearchResult$!: Observable<SearchResultPayloadModel<InvoiceBasicModel>>;

  @Select(InvoiceManagementState.invoicePageable)
  invoicePageable$!: Observable<PageableModel>;

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
      label: 'Invoice No',
      key: 'invoiceNo',
      type: 'TEXT',
      options: [],
    },
    {
      label: 'Plan Name',
      key: 'planName',
      type: 'TEXT',
      options: [],
    },
    {
      label: 'Status',
      key: 'status',
      type: 'OPTION',
      options: Object.values(InvoiceStatus),
    },
  ];
  options: string[] = [];

  isReportModalVisible = false;
  reportForm: UntypedFormGroup;

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

    this.reportForm = this.fb.group({
      dates: ['', [Validators.required]],
      affiliateCode: [null],
      accountName: [null],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetInvoices());

    this.autoUnsubscribe(
      this.invoiceSearchResult$.pipe(
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
              new QueryInvoices(
                {
                  ...this.store.selectSnapshot(InvoiceManagementState.invoicePageable),
                  page: 1,
                },
                accountName,
                this.store.selectSnapshot(InvoiceManagementState.invoiceNo),
                this.store.selectSnapshot(InvoiceManagementState.planName),
                this.store.selectSnapshot(InvoiceManagementState.status),
                this.store.selectSnapshot(InvoiceManagementState.startDate),
                this.store.selectSnapshot(InvoiceManagementState.endDate)
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
              new QueryInvoices(
                {
                  ...this.store.selectSnapshot(InvoiceManagementState.invoicePageable),
                  page: 1,
                },
                this.store.selectSnapshot(InvoiceManagementState.accountName),
                this.store.selectSnapshot(InvoiceManagementState.invoiceNo),
                this.store.selectSnapshot(InvoiceManagementState.planName),
                this.store.selectSnapshot(InvoiceManagementState.status),
                range[0] ? range[0] : undefined,
                range[1] ? range[1] : undefined
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(DownloadInvoiceReport),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(InvoiceManagementState.file)!;
            saveAs(file.blob, file.name);
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
    const pageable = produce(this.store.selectSnapshot(InvoiceManagementState.invoicePageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(
      new QueryInvoices(
        pageable,
        this.store.selectSnapshot(InvoiceManagementState.accountName),
        this.store.selectSnapshot(InvoiceManagementState.invoiceNo),
        this.store.selectSnapshot(InvoiceManagementState.planName),
        this.store.selectSnapshot(InvoiceManagementState.status),
        this.store.selectSnapshot(InvoiceManagementState.startDate),
        this.store.selectSnapshot(InvoiceManagementState.endDate)
      )
    );
    this.isInitialize = true;
  }

  dateTimeFormat() {
    return environment.config.dateTimeFormat;
  }

  dateFormat() {
    return environment.config.dateFormat;
  }

  doOpenFilterModal() {
    this.filterForm.reset();
    this.filterForm.get('field')!.setValue('invoiceNo');
    this.doFilterFieldChange('invoiceNo');
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
    const { invoiceNo, planName, status } = this.filters;

    this.isTableLoading = true;
    this.isFilterModalVisible = false;
    this.cd.markForCheck();

    this.store.dispatch(
      new QueryInvoices(
        this.store.selectSnapshot(InvoiceManagementState.invoicePageable),
        this.store.selectSnapshot(InvoiceManagementState.accountName),
        invoiceNo,
        planName,
        status
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
    const { invoiceNo, planName, status } = this.filters;

    this.store.dispatch(
      new QueryInvoices(
        this.store.selectSnapshot(InvoiceManagementState.invoicePageable),
        this.store.selectSnapshot(InvoiceManagementState.accountName),
        invoiceNo,
        planName,
        status
      )
    );
  }

  getFilterLabel(filter: string) {
    return filter;
  }

  getStatusTag(invoice: InvoiceBasicModel): string {
    return getInvoiceStatusTag(invoice.status);
  }

  doOpenDownloadReportModal() {
    this.reportForm.reset();
    this.isReportModalVisible = true;
    this.cd.markForCheck();
  }

  doCloseReportModal() {
    this.isReportModalVisible = false;
    this.cd.markForCheck();
  }

  doDownloadReport() {
    const { dates, affiliateCode, accountName } = this.reportForm.getRawValue();
    this.isReportModalVisible = false;
    this.cd.markForCheck();
    this.store.dispatch(new DownloadInvoiceReport(dates[0], dates[1], affiliateCode, accountName));
  }
}
