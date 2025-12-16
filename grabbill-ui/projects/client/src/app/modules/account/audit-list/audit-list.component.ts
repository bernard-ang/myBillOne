import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { AuditState } from '../../../../states/audit/audit.state';
import { QueryAudits, ResetAudits } from '../../../../states/audit/audit.state-actions';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import { AuditLogModel, getErrorMessage, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';
import { filter } from 'rxjs/operators';
import { ShowMessage } from '../../../../states/common/common.state-actions';

@Component({
  selector: 'grabbill-client-audit-list',
  templateUrl: './audit-list.component.html',
  styleUrls: ['./audit-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuditListComponent extends NgxsBaseComponent {
  @Select(AuditState.auditSearchResult)
  auditResult$!: Observable<SearchResultPayloadModel<AuditLogModel>>;

  @Select(AuditState.auditPageable)
  auditPageable$!: Observable<PageableModel>;

  isInitialize = false;
  isTableLoading = false;
  searchChange$ = new BehaviorSubject('');
  dateRangeChange$: BehaviorSubject<(Date | null)[]> = new BehaviorSubject<(Date | null)[]>([]);

  form: UntypedFormGroup;

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private actions$: Actions,
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
                  ...this.store.selectSnapshot(AuditState.auditPageable),
                  page: 1,
                },
                this.store.selectSnapshot(AuditState.query),
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
                  ...this.store.selectSnapshot(AuditState.auditPageable),
                  page: 1,
                },
                query,
                this.store.selectSnapshot(AuditState.startDate),
                this.store.selectSnapshot(AuditState.endDate)
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(QueryAudits),
        switchMap((data: ActionCompletion) => {
          this.isTableLoading = false;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      )
    );
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(AuditState.auditPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(
      new QueryAudits(
        pageable,
        this.store.selectSnapshot(AuditState.query),
        this.store.selectSnapshot(AuditState.startDate),
        this.store.selectSnapshot(AuditState.endDate)
      )
    );
    this.isInitialize = true;
  }

  doSearch(event: any) {
    this.searchChange$.next(event.target.value);
  }

  doDateRange(event: (Date | null)[]) {
    this.dateRangeChange$.next(event);
  }
}
