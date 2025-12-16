import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { UntypedFormBuilder, UntypedFormGroup } from "@angular/forms";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from "rxjs";
import produce from "immer";
import { NzTableQueryParams } from "ng-zorro-antd/table";
import { NzMessageService } from "ng-zorro-antd/message";
import { NzModalService } from "ng-zorro-antd/modal";
import {
  DomainType,
  getErrorMessage,
  PageableModel,
  SearchResultPayloadModel,
  UnsubscribedEmailModel
} from "@grabbill/lib";
import { UnsubscribedEmailState } from "../../../../states/unsubscribed-email/unsubscribed-email.state";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { ShowMessage } from "../../../../states/common/common.state-actions";
import {
  DeleteUnsubscribedEmail,
  QueryUnsubscribedEmails,
  ResetUnsubscribedEmails
} from "../../../../states/unsubscribed-email/unsubscribed-email.state-actions";
import { filter } from "rxjs/operators";

@Component({
  selector: 'grabbill-client-unsubscribe-email-list',
  templateUrl: './unsubscribe-email-list.component.html',
  styleUrls: ['./unsubscribe-email-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnsubscribeEmailListComponent extends NgxsBaseComponent {
  @Select(UnsubscribedEmailState.unsubscribedEmailSearchResult)
  unsubscribedEmailSearchResult$!: Observable<SearchResultPayloadModel<UnsubscribedEmailModel>>;

  @Select(UnsubscribedEmailState.unsubscribedEmailPageable)
  unsubscribedEmailPageable$!: Observable<PageableModel>;

  isInitialize = false;
  isTableLoading = false;
  searchChange$ = new BehaviorSubject('');
  dateRangeChange$: BehaviorSubject<(Date | null)[]> = new BehaviorSubject<(Date | null)[]>([]);
  form: UntypedFormGroup;

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private cd: ChangeDetectorRef,
    private actions$: Actions,
    private modal: NzModalService,
    private fb: UntypedFormBuilder
  ) {
    super(store, messageService);
    this.form = this.fb.group({
      range: [],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetUnsubscribedEmails());

    this.autoUnsubscribe(
      this.unsubscribedEmailSearchResult$.pipe(
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
            const startDate = this.store.selectSnapshot(UnsubscribedEmailState.startDate);
            const endDate = this.store.selectSnapshot(UnsubscribedEmailState.endDate);
            return this.store.dispatch(
              new QueryUnsubscribedEmails(
                {
                  ...this.store.selectSnapshot(UnsubscribedEmailState.unsubscribedEmailPageable),
                  page: 1,
                },
                startDate,
                endDate,
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
              new QueryUnsubscribedEmails(
                {
                  ...this.store.selectSnapshot(UnsubscribedEmailState.unsubscribedEmailPageable),
                  page: 1,
                },
                range[0] ? range[0] : undefined,
                range[1] ? range[1] : undefined,
                this.store.selectSnapshot(UnsubscribedEmailState.email)
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(QueryUnsubscribedEmails),
        switchMap((data: ActionCompletion) => {
          this.isTableLoading = false
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteUnsubscribedEmail),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Unsubscribed email updated`));
          }

          return of(false);
        })
      )
    );
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(UnsubscribedEmailState.unsubscribedEmailPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(new QueryUnsubscribedEmails(pageable));
    this.isInitialize = true;
  }

  doSearch(event: any) {
    this.searchChange$.next(event.target.value);
  }

  doDateRange(event: (Date | null)[]) {
    this.dateRangeChange$.next(event);
  }

  doDelete(unsubscribedEmail: UnsubscribedEmailModel) {
    this.modal.confirm({
      nzTitle: `Delete unsubscribed email ${unsubscribedEmail.email}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeleteUnsubscribedEmail(unsubscribedEmail.id));
      },
      nzCancelText: 'No',
    });
  }

  doNavigate(unsubscribedEmail: UnsubscribedEmailModel) {
    if (unsubscribedEmail.domainType === DomainType.TRANSACTIONAL_EMAIL) {
      this.navigate(
        [ 'transactional-email', 'detail', unsubscribedEmail.typeId, 'activity', unsubscribedEmail.activityId ],
        {
          tab: 'records',
        }
      );
    } else if (unsubscribedEmail.domainType === DomainType.MT_TRANSACTIONAL_EMAIL) {
      this.navigate(
        ['mt-transactional-email', 'detail', unsubscribedEmail.typeId, 'activity', unsubscribedEmail.activityId],
        {
          tab: 'records',
        }
      );
    } else {
      this.navigate(['email-campaign', 'detail', unsubscribedEmail.typeId, 'activity', unsubscribedEmail.activityId], {
        tab: 'records',
      });
    }
  }
}
