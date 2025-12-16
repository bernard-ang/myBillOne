import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { UntypedFormBuilder, UntypedFormGroup } from "@angular/forms";
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from "rxjs";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { NzModalService } from "ng-zorro-antd/modal";
import { NzTableQueryParams } from "ng-zorro-antd/table";
import { NzMessageService } from "ng-zorro-antd/message";
import produce from "immer";
import { BouncedEmailModel, DomainType, getErrorMessage, PageableModel, SearchResultPayloadModel } from "@grabbill/lib";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { BouncedEmailState } from "../../../../states/bounced-email/bounced-email.state";
import {
  DeleteBouncedEmail,
  QueryBouncedEmails,
  ResetBouncedEmails
} from "../../../../states/bounced-email/bounced-email.state-actions";
import { ShowMessage } from "../../../../states/common/common.state-actions";
import { filter } from "rxjs/operators";

@Component({
  selector: 'grabbill-client-bounced-email-list',
  templateUrl: './bounced-email-list.component.html',
  styleUrls: ['./bounced-email-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BouncedEmailListComponent extends NgxsBaseComponent {
  @Select(BouncedEmailState.bouncedEmailSearchResult)
  bouncedEmailSearchResult$!: Observable<SearchResultPayloadModel<BouncedEmailModel>>;

  @Select(BouncedEmailState.bouncedEmailPageable)
  bouncedEmailPageable$!: Observable<PageableModel>;

  expandSet = new Set<number>();

  isInitialize = false;
  isTableLoading = false;
  searchChange$ = new BehaviorSubject('');
  dateRangeChange$: BehaviorSubject<(Date | null)[]> = new BehaviorSubject<(Date | null)[]>([]);
  form: UntypedFormGroup;

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
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetBouncedEmails());

    this.autoUnsubscribe(
      this.bouncedEmailSearchResult$.pipe(
        tap(() => {
          this.isTableLoading = false;
        })
      ),
      this.searchChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((email: string) => {
            this.isTableLoading = true;
            this.cd.markForCheck();
            const startDate = this.store.selectSnapshot(BouncedEmailState.startDate);
            const endDate = this.store.selectSnapshot(BouncedEmailState.endDate);
            return this.store.dispatch(
              new QueryBouncedEmails(
                {
                  ...this.store.selectSnapshot(BouncedEmailState.bouncedEmailPageable),
                  page: 1,
                },
                endDate,
                startDate,
                email
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
              new QueryBouncedEmails(
                {
                  ...this.store.selectSnapshot(BouncedEmailState.bouncedEmailPageable),
                  page: 1,
                },
                range[1] ? range[1] : undefined,
                range[0] ? range[0] : undefined,
                this.store.selectSnapshot(BouncedEmailState.email)
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(QueryBouncedEmails),
        switchMap((data: ActionCompletion) => {
          this.isTableLoading = false
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteBouncedEmail),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Bounced email deleted`));
          }

          return of(false);
        })
      )
    );
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(BouncedEmailState.bouncedEmailPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(new QueryBouncedEmails(pageable));
    this.isInitialize = true;
  }

  doSearch(event: any) {
    this.searchChange$.next(event.target.value);
  }

  doDateRange(event: (Date | null)[]) {
    this.dateRangeChange$.next(event);
  }

  doDelete(bouncedEmail: BouncedEmailModel) {
    this.modal.confirm({
      nzTitle: `Delete bounced email ${bouncedEmail.email}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeleteBouncedEmail(bouncedEmail.id));
      },
      nzCancelText: 'No',
    });
  }

  doNavigate(bouncedEmail: BouncedEmailModel) {
    if (bouncedEmail.domainType === DomainType.TRANSACTIONAL_EMAIL) {
      this.navigate([ 'transactional-email', 'detail', bouncedEmail.typeId, 'activity', bouncedEmail.activityId ], {
        tab: 'records',
      });
    } else if (bouncedEmail.domainType === DomainType.MT_TRANSACTIONAL_EMAIL) {
      this.navigate([ "mt-transactional-email", "detail", bouncedEmail.typeId, "activity", bouncedEmail.activityId ], {
        tab: "records"
      });
    } else {
      this.navigate(['email-campaign', 'detail', bouncedEmail.typeId, 'activity', bouncedEmail.activityId], {
        tab: 'records',
      });
    }
  }

  onExpandChange(id: number, checked: boolean): void {
    if (checked) {
      this.expandSet.add(id);
    } else {
      this.expandSet.delete(id);
    }
  }
}
