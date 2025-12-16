import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { BehaviorSubject, debounceTime, Observable, switchMap, tap } from 'rxjs';
import { filter } from 'rxjs/operators';
import { Select, Store } from '@ngxs/store';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { AdminAuditLogModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';
import { AdminAuditState } from '../../../states/admin-audit/admin-audit.state';
import { QueryAudits, ResetAudits } from '../../../states/admin-audit/admin-audit.state-actions';

@Component({
  selector: 'grabbill-admin-audit-list',
  templateUrl: './admin-audit-list.component.html',
  styleUrls: ['./admin-audit-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminAuditListComponent extends NgxsBaseComponent {
  @Select(AdminAuditState.auditSearchResult)
  auditResult$!: Observable<SearchResultPayloadModel<AdminAuditLogModel>>;

  @Select(AdminAuditState.auditPageable)
  auditPageable$!: Observable<PageableModel>;

  isInitialize = false;
  isTableLoading = false;

  searchChange$ = new BehaviorSubject('');
  dateRangeChange$: BehaviorSubject<(Date | null)[]> = new BehaviorSubject<(Date | null)[]>([]);
  dates: Date[] = [];

  form: UntypedFormGroup;

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
    this.form = fb.group({
      range: [[]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetAudits());

    this.autoUnsubscribe(
      this.auditResult$.pipe(
        tap(() => {
          this.isTableLoading = false;
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
              new QueryAudits(
                {
                  ...this.store.selectSnapshot(AdminAuditState.auditPageable),
                  page: 1,
                },
                this.store.selectSnapshot(AdminAuditState.query),
                range[0] ? range[0] : undefined,
                range[1] ? range[1] : undefined
              )
            );
          })
        ),
      this.searchChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((query: string) => {
            this.isTableLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new QueryAudits(
                {
                  ...this.store.selectSnapshot(AdminAuditState.auditPageable),
                  page: 1,
                },
                query,
                this.dates[0],
                this.dates[1]
              )
            );
          })
        )
    );
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(AdminAuditState.auditPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(new QueryAudits(pageable));
    this.isInitialize = true;
  }

  doSearch(event: any) {
    this.searchChange$.next(event.target.value);
  }

  doDateRange(event: (Date | null)[]) {
    this.dateRangeChange$.next(event);
  }
}
