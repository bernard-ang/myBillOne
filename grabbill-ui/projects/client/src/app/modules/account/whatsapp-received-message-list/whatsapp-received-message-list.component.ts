import { Component, ChangeDetectionStrategy, ChangeDetectorRef } from "@angular/core";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from "rxjs";
import { getErrorMessage, PageableModel, SearchResultPayloadModel } from "@grabbill/lib";
import { UntypedFormBuilder, UntypedFormGroup } from "@angular/forms";
import { NzMessageService } from "ng-zorro-antd/message";
import { filter } from "rxjs/operators";
import { ShowMessage } from "../../../../states/common/common.state-actions";
import { NzTableQueryParams } from "ng-zorro-antd/table";
import produce from "immer";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { WhatsAppState } from "../../../../states/whatsapp/whatsapp.state";
import { WhatsappEventBasicModel } from "../../../../../../lib/src/models/data/whatsapp/whatsapp-event-basic.model";
import { QueryWhatsAppEvents, ResetWhatsAppEvents } from "../../../../states/whatsapp/whatsapp.state-actions";

@Component({
  selector: 'grabbill-client-whatsapp-received-message-list',
  templateUrl: './whatsapp-received-message-list.component.html',
  styleUrls: ['./whatsapp-received-message-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WhatsappReceivedMessageListComponent extends NgxsBaseComponent {

  @Select(WhatsAppState.eventSearchResult)
  eventSearchResult$!: Observable<SearchResultPayloadModel<WhatsappEventBasicModel>>;

  @Select(WhatsAppState.eventPageable)
  eventPageable$!: Observable<PageableModel>;

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
    this.store.dispatch(new ResetWhatsAppEvents());

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
          switchMap((mobileNo: string) => {
            this.isTableLoading = true;
            this.cd.markForCheck();
            const startDate = this.store.selectSnapshot(WhatsAppState.startDate);
            const endDate = this.store.selectSnapshot(WhatsAppState.endDate);
            return this.store.dispatch(
              new QueryWhatsAppEvents(
                {
                  ...this.store.selectSnapshot(WhatsAppState.eventPageable),
                  page: 1,
                },
                endDate,
                startDate,
                mobileNo
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
              new QueryWhatsAppEvents(
                {
                  ...this.store.selectSnapshot(WhatsAppState.eventPageable),
                  page: 1,
                },
                range[1] ? range[1] : undefined,
                range[0] ? range[0] : undefined,
                this.store.selectSnapshot(WhatsAppState.mobileNo)
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(QueryWhatsAppEvents),
        switchMap((data: ActionCompletion) => {
          this.isTableLoading = false
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
    );
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(WhatsAppState.eventPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(new QueryWhatsAppEvents(pageable));
    this.isInitialize = true;
  }

  doSearch(event: any) {
    this.searchChange$.next(event.target.value);
  }

  doDateRange(event: (Date | null)[]) {
    this.dateRangeChange$.next(event);
  }
}
