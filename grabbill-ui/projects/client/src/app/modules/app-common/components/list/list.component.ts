import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input, OnInit,
  Output,
  TemplateRef
} from "@angular/core";
import { UntypedFormControl } from '@angular/forms';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import { filter } from 'rxjs/operators';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { getErrorMessage, makePageable, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';
import { BaseComponent } from '../../../../components/base.component';
import { ShowMessage } from '../../../../../states/common/common.state-actions';

interface MyContext {
  $implicit: any;
}

@Component({
  selector: 'grabbill-client-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListComponent extends BaseComponent implements OnInit {
  @Input()
  store!: Store;

  @Input()
  actions$!: Actions;

  @Input()
  result$!: Observable<SearchResultPayloadModel<any>>;

  @Input()
  pageable$!: Observable<PageableModel>;

  @Input()
  hasSearch = true;

  @Input()
  searchText = 'Find by name';

  @Input()
  noRecordText = 'No record found';

  @Input()
  icon!: string;

  @Input()
  queryAction!: any;

  @Input()
  queryActionParams: any[] = [];

  @Input()
  resetAction: any;

  @Input()
  loadMoreAction!: any;

  @Input()
  pageableState!: any;

  @Input()
  typeNameState!: any;

  @Input()
  isListLoading!: boolean;

  @Input()
  hasSort = true;

  @Input()
  getSubtitle?: (item: any) => string;

  @Input()
  subtitleTemplate?: TemplateRef<MyContext>;

  @Input()
  contentTemplate?: TemplateRef<MyContext>;

  @Input()
  actionsTemplate?: TemplateRef<MyContext>;

  @Input()
  statusTemplate?: TemplateRef<MyContext>;

  @Input()
  additionalButtonTemplate?: TemplateRef<MyContext>;

  @Output()
  selectItem = new EventEmitter<any>();

  isInitialize = false;
  searchChange$ = new BehaviorSubject('');
  sortBy = new UntypedFormControl('lastModifiedDate');

  constructor(private cd: ChangeDetectorRef) {
    super();
  }

  ngOnInit(): void {
    if (this.resetAction) {
      this.store.dispatch(new this.resetAction());
    }
    this.autoUnsubscribe(
      this.result$.pipe(
        tap((result) => {
          this.isListLoading = false;
        })
      ),
      this.searchChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((name: string) => {
            this.isListLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new this.queryAction(
                ...this.queryActionParams,
                {
                  ...this.store.selectSnapshot(this.pageableState),
                  page: 1,
                },
                name
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(this.queryAction, this.loadMoreAction),
        switchMap((data: ActionCompletion) => {
          this.isListLoading = false;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          this.cd.markForCheck();
          return of(false);
        })
      )
    );

    this.isListLoading = true;
    this.store.dispatch(
      new this.queryAction(...this.queryActionParams, makePageable(50, 1, 'lastModifiedDate', 'DESC'))
    );
    this.isInitialize = true;
  }

  doSelect(item: any) {
    this.selectItem.emit(item);
  }

  doSearch(event: any) {
    this.searchChange$.next(event.target.value);
  }

  doSortChange(value: string) {
    if (this.isInitialize) {
      this.isListLoading = true;
      this.store.dispatch(
        new this.queryAction(
          ...this.queryActionParams,
          { ...this.store.selectSnapshot(this.pageableState), sort: value },
          this.store.selectSnapshot(this.typeNameState)
        )
      );
    }
  }

  doLoadMore() {
    this.isListLoading = true;
    this.cd.markForCheck();
    this.store.dispatch(new this.loadMoreAction());
  }
}
