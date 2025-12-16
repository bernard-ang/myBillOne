import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import {
  AccountStatementModel,
  getErrorMessage,
  PageableModel,
  SearchResultPayloadModel,
  SmsUsageSummaryPayloadModel
} from "@grabbill/lib";
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { filter } from 'rxjs/operators';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import { UsageState } from '../../../../states/usage/usage.state';
import {
  GetSmsUsage,
  QueryAccountStatements,
  ResetAccountStatements,
  ResetSmsUsage,
} from '../../../../states/usage/usage.state-actions';
import produce from 'immer';
import { environment } from '../../../../environments/environment';
import { subDays } from 'date-fns';

@Component({
  selector: 'grabbill-client-usage-list',
  templateUrl: './usage-list.component.html',
  styleUrls: ['./usage-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsageListComponent extends NgxsBaseComponent {
  @Select(UsageState.accountStatementSearchResult)
  accountStatementSearchResult$!: Observable<SearchResultPayloadModel<AccountStatementModel>>;

  @Select(UsageState.accountStatementPageable)
  accountStatementPageable$!: Observable<PageableModel>;

  @Select(UsageState.smsUsageSummary)
  smsUsageSummary$!: Observable<SmsUsageSummaryPayloadModel>;

  isInitialize = false;
  isTableLoading = false;
  dateRangeChange$: BehaviorSubject<(Date | null)[]> = new BehaviorSubject<(Date | null)[]>([]);
  form: UntypedFormGroup;
  smsForm: UntypedFormGroup;

  canTopUpSms = environment.config.canTopUpSms;

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private cd: ChangeDetectorRef,
    private actions$: Actions,
    private fb: UntypedFormBuilder
  ) {
    super(store, messageService);
    this.form = this.fb.group({
      range: [],
    });

    this.smsForm = this.fb.group({
      range: [[subDays(new Date(), 1), new Date()]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetAccountStatements());
    if (this.canTopUpSms) {
      this.store.dispatch(new ResetSmsUsage());
      this.store.dispatch(new GetSmsUsage(subDays(new Date(), 1), new Date()));
    }

    this.autoUnsubscribe(
      this.accountStatementSearchResult$.pipe(
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
              new QueryAccountStatements(
                {
                  ...this.store.selectSnapshot(UsageState.accountStatementPageable),
                  page: 1,
                },
                range[0] ? range[0] : undefined,
                range[1] ? range[1] : undefined
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(QueryAccountStatements),
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
    const pageable = produce(this.store.selectSnapshot(UsageState.accountStatementPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(new QueryAccountStatements(pageable));
    this.isInitialize = true;
  }

  doDateRange(event: (Date | null)[]) {
    this.dateRangeChange$.next(event);
  }

  doSmsDateRange(event: Date[]) {
    if (event[0] && event[1]) {
      this.store.dispatch(new GetSmsUsage(event[0], event[1]));
    }
  }
}
