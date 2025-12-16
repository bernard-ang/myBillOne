import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { Actions, Select, Store } from '@ngxs/store';
import { BehaviorSubject, debounceTime, Observable, switchMap, tap } from 'rxjs';
import { PageableModel, SearchResultPayloadModel, StripeEventBasicModel, StripeEventType } from '@grabbill/lib';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { filter } from 'rxjs/operators';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { environment } from '../../../../environments/environment';
import { StripeManagementState } from '../../../../states/stripe-management/stripe-management.state';
import {
  QueryStripeEvents,
  ResetStripEvents,
} from '../../../../states/stripe-management/stripe-management.state-actions';

@Component({
  selector: 'grabbill-admin-stripe-event-list',
  templateUrl: './stripe-event-list.component.html',
  styleUrls: ['./stripe-event-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StripeEventListComponent extends NgxsBaseComponent {
  @Select(StripeManagementState.eventSearchResult)
  eventSearchResult$!: Observable<SearchResultPayloadModel<StripeEventBasicModel>>;

  @Select(StripeManagementState.eventPageable)
  eventPageable$!: Observable<PageableModel>;

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
      label: 'Type',
      key: 'type',
      type: 'OPTION',
      options: Object.values(StripeEventType),
    },
    {
      label: 'Ref Id',
      key: 'refId',
      type: 'TEXT',
      options: [],
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
    this.store.dispatch(new ResetStripEvents());

    this.autoUnsubscribe(
      this.eventSearchResult$.pipe(
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
              new QueryStripeEvents(
                {
                  ...this.store.selectSnapshot(StripeManagementState.eventPageable),
                  page: 1,
                },
                accountName,
                this.store.selectSnapshot(StripeManagementState.type),
                this.store.selectSnapshot(StripeManagementState.refId),
                this.store.selectSnapshot(StripeManagementState.startDate),
                this.store.selectSnapshot(StripeManagementState.endDate)
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
              new QueryStripeEvents(
                {
                  ...this.store.selectSnapshot(StripeManagementState.eventPageable),
                  page: 1,
                },
                this.store.selectSnapshot(StripeManagementState.accountName),
                this.store.selectSnapshot(StripeManagementState.type),
                this.store.selectSnapshot(StripeManagementState.refId),
                range[0] ? range[0] : undefined,
                range[1] ? range[1] : undefined
              )
            );
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
    const pageable = produce(this.store.selectSnapshot(StripeManagementState.eventPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(
      new QueryStripeEvents(
        pageable,
        this.store.selectSnapshot(StripeManagementState.accountName),
        this.store.selectSnapshot(StripeManagementState.type),
        this.store.selectSnapshot(StripeManagementState.refId),
        this.store.selectSnapshot(StripeManagementState.startDate),
        this.store.selectSnapshot(StripeManagementState.endDate)
      )
    );
    this.isInitialize = true;
  }

  dateTimeFormat() {
    return environment.config.dateTimeFormat;
  }

  doOpenFilterModal() {
    this.filterForm.reset();
    this.filterForm.get('field')!.setValue('type');
    this.doFilterFieldChange('type');
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
    const { type, refId } = this.filters;

    this.isTableLoading = true;
    this.isFilterModalVisible = false;
    this.cd.markForCheck();

    this.store.dispatch(
      new QueryStripeEvents(
        this.store.selectSnapshot(StripeManagementState.eventPageable),
        this.store.selectSnapshot(StripeManagementState.accountName),
        type,
        refId
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
    const { type, refId } = this.filters;

    this.store.dispatch(
      new QueryStripeEvents(
        this.store.selectSnapshot(StripeManagementState.eventPageable),
        this.store.selectSnapshot(StripeManagementState.accountName),
        type,
        refId
      )
    );
  }

  getFilterLabel(filter: string) {
    return filter;
  }
}
