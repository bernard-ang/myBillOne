import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { UntypedFormControl } from '@angular/forms';
import {
  BaseTypeBasicModel,
  BaseTypeModel,
  ContactFieldModel,
  DomainType,
  getErrorMessage,
  makePageable,
  Privilege,
  SearchResultPayloadModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import { GetType, QueryTypes, ResetIndexRecords, ResetSearch } from '../../../../states/search/search.state-actions';
import { SearchState } from '../../../../states/search/search.state';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { ContactFieldState } from '../../../../states/contact-field/contact-field.state';
import { GetContactFields, ResetContactFields } from '../../../../states/contact-field/contact-field.state-actions';
import { defaultContactFields } from '../../../../utils/default-contact-fields';
import { AuthState } from '../../../../states/auth/auth.state';
import { hasPrivilege } from '../../../../utils/has-privilege';
import { isSaasMode } from "../../../../utils/deployment-mode";

@Component({
  selector: 'grabbill-client-search-list',
  templateUrl: './search-list.component.html',
  styleUrls: ['./search-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SearchListComponent extends NgxsBaseComponent {
  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  @Select(SearchState.typeSearchResult)
  typeSearchResult$!: Observable<SearchResultPayloadModel<BaseTypeBasicModel>>;

  @Select(SearchState.type)
  type$!: Observable<BaseTypeModel>;

  @Select(ContactFieldState.contactFields)
  contactFields$!: Observable<ContactFieldModel[]>;

  domain = new UntypedFormControl();
  type = new UntypedFormControl();
  typeOptions: BaseTypeBasicModel[] = [];
  contactFields: ContactFieldModel[] = [];
  isLoading = false;
  isTableLoading = false;
  searchChange$ = new BehaviorSubject('');

  currentDomain?: DomainType;
  previousValue = '';

  currentType?: BaseTypeModel;

  isSaas = isSaasMode()

  getDomainTypeOptions(user: UserAuthorityModel): DomainType[] {
    const options = [];

    if (hasPrivilege(user, Privilege.DGTL_FILING_ACTIVITY_VIEW)) {
      options.push(DomainType.DIGITAL_FILING);
    }

    if (hasPrivilege(user, Privilege.EMAIL_CAMPAIGN_ACTIVITY_VIEW)) {
      options.push(DomainType.EMAIL_CAMPAIGN);
    }

    if (hasPrivilege(user, Privilege.TRX_EMAIL_ACTIVITY_VIEW)) {
      if (this.isSaas) {
        options.push(DomainType.TRANSACTIONAL_EMAIL);
      } else {
        options.push(DomainType.MT_TRANSACTIONAL_EMAIL);
      }
    }

    if (hasPrivilege(user, Privilege.WA_ACTIVITY_VIEW)) {
      if (this.isSaas) {
        options.push(DomainType.WHATSAPP);
      } else {
        options.push(DomainType.MT_WHATSAPP);
      }
    }

    if (hasPrivilege(user, Privilege.SMS_ACTIVITY_VIEW)) {
      options.push(DomainType.SMS);
    }
    return options;
  }

  getDomainLabel(domain: DomainType) {
    return (domain as any).replaceAll('MT_', '').replaceAll('_', ' ');
  }

  constructor(
    private cd: ChangeDetectorRef,
    private actions$: Actions,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetContactFields());
    this.store.dispatch(new ResetSearch());
    this.store.dispatch(new GetContactFields());

    this.autoUnsubscribe(
      this.typeSearchResult$.pipe(
        tap((searchResult) => {
          this.typeOptions = searchResult.items;
          this.isLoading = false;
        })
      ),
      this.contactFields$.pipe(
        tap((contactFields) => {
          if (contactFields) {
            this.contactFields = [
              ...defaultContactFields.map((field, idx) => ({ ...field, seqOrder: idx + 1 })),
              ...contactFields.map((field) => ({ ...field, seqOrder: field.seqOrder + 2 })),
            ];
          }
        })
      ),
      this.type$.pipe(
        tap((type) => {
          this.currentType = type;
          this.cd.markForCheck();
        })
      ),
      this.typeSearchResult$.pipe(
        tap(() => {
          this.isTableLoading = false;
        })
      ),
      this.searchChange$.asObservable().pipe(
        debounceTime(200),
        tap((name) => {
          if (this.currentDomain) {
            this.isLoading = true;
            this.store.dispatch(new QueryTypes(this.currentDomain, makePageable(5, 1, 'name', 'asc'), name));
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(QueryTypes, GetType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          this.isTableLoading = false;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      )
    );
  }

  doSearchType(value: string) {
    if (this.previousValue !== value) {
      this.previousValue = value;
      this.searchChange$.next(value);
    }
  }

  doDomainChange(value: DomainType) {
    this.currentDomain = value;
    this.store.dispatch(new ResetIndexRecords());
    this.currentType = undefined;
    this.type.setValue(undefined);
    this.isLoading = true;
    this.cd.markForCheck();
    this.store.dispatch(new QueryTypes(this.currentDomain, makePageable(5, 1, 'name', 'asc')));
  }

  doTypeChange(typeId: number) {
    if (typeId) {
      this.isTableLoading = true;
      this.cd.markForCheck();
      this.store.dispatch(new ResetIndexRecords());
      this.store.dispatch(new GetType(this.currentDomain!, typeId));
    }
  }

  handleLoadingChange(loading: boolean) {
    this.isTableLoading = loading;
    this.cd.markForCheck();
  }
}
