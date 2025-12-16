import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import {
  AffiliateCodeBasicModel,
  getErrorMessage,
  PageableModel,
  resolveErrorMessage,
  SearchResultPayloadModel,
  updateAndMarkControlAsDirty,
} from '@grabbill/lib';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ShowMessage } from '../../../states/admin-common/admin-common.state-actions';
import { filter } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { AffiliateCodeState } from '../../../states/affiliate-code/affiliate-code.state';
import {
  DeleteAffiliateCode,
  GenerateMasterCode,
  NewAffiliateCode,
  QueryAffiliateCodes,
  ResetAffiliateCodes,
  UpdateAffiliateCode,
} from '../../../states/affiliate-code/affiliate-code.state-actions';

@Component({
  selector: 'grabbill-admin-admin-affiliate-code-list',
  templateUrl: './admin-affiliate-code-list.component.html',
  styleUrls: ['./admin-affiliate-code-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminAffiliateCodeListComponent extends NgxsBaseComponent {
  @Select(AffiliateCodeState.affiliateCodeSearchResult)
  affiliateCodeSearchResult$!: Observable<SearchResultPayloadModel<AffiliateCodeBasicModel>>;

  @Select(AffiliateCodeState.affiliateCodePageable)
  affiliateCodePageable$!: Observable<PageableModel>;

  isInitialize = false;
  isTableLoading = false;
  searchChange$ = new BehaviorSubject('');

  isCreateFormLoading = false;
  isCreateFormVisible = false;
  isEditFormLoading = false;
  isEditFormVisible = false;

  createForm: UntypedFormGroup;
  editForm: UntypedFormGroup;

  constructor(
    private fb: UntypedFormBuilder,
    protected override store: Store,
    protected override messageService: NzMessageService,
    private modal: NzModalService,
    private actions$: Actions,
    private cd: ChangeDetectorRef
  ) {
    super(store, messageService);

    this.createForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      code: ['', [Validators.required, Validators.maxLength(5)]],
    });

    this.editForm = this.fb.group({
      id: ['', [Validators.required]],
      name: ['', [Validators.required, Validators.maxLength(255)]],
      code: ['', [Validators.required, Validators.maxLength(5)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetAffiliateCodes());

    this.autoUnsubscribe(
      this.affiliateCodeSearchResult$.pipe(
        tap(() => {
          this.isTableLoading = false;
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteAffiliateCode),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Affiliate code deleted`));
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewAffiliateCode),
        switchMap((data: ActionCompletion) => {
          this.isCreateFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isCreateFormVisible = false;

            this.store.dispatch(new ShowMessage('info', `New affiliate code added`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateAffiliateCode),
        switchMap((data: ActionCompletion) => {
          this.isEditFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isEditFormVisible = false;
            this.store.dispatch(new ShowMessage('info', `Affiliate code updated`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.searchChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((code: string) => {
            this.isTableLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new QueryAffiliateCodes(
                {
                  ...this.store.selectSnapshot(AffiliateCodeState.affiliateCodePageable),
                  page: 1,
                },
                code
              )
            );
          })
        )
    );
  }

  dateTimeFormat() {
    return environment.config.dateTimeFormat;
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(AffiliateCodeState.affiliateCodePageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(new QueryAffiliateCodes(pageable));
    this.isInitialize = true;
  }

  doSearch(event: any) {
    this.isTableLoading = true;
    this.searchChange$.next(event.target.value);
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doDeleteAffiliateCode(affiliateCode: AffiliateCodeBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete affiliate code ${affiliateCode.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeleteAffiliateCode(affiliateCode.id));
      },
      nzCancelText: 'No',
    });
  }

  doOpenCreateForm() {
    this.autoUnsubscribeOnChanges(
      this.store.dispatch(new GenerateMasterCode()).pipe(
        tap(() => {
          const newMasterCode = this.store.selectSnapshot(AffiliateCodeState.newMasterCode);
          this.createForm.reset({
            code: newMasterCode,
          });
          this.isCreateFormVisible = true;
          this.cd.markForCheck();
        })
      )
    );
  }

  doCreateAffiliateCode() {
    this.isCreateFormLoading = true;
    this.cd.markForCheck();

    if (this.createForm.valid) {
      const value = this.createForm.getRawValue();
      this.store.dispatch(new NewAffiliateCode({ code: value.code, name: value.name }));
    } else {
      updateAndMarkControlAsDirty(this.createForm);
      this.isCreateFormLoading = false;
      this.cd.markForCheck();
    }
  }

  closeCreateForm(): void {
    this.isCreateFormVisible = false;
    this.cd.markForCheck();
  }

  doOpenEditForm(affiliateCode: AffiliateCodeBasicModel) {
    this.editForm.reset({ id: affiliateCode.id, code: affiliateCode.code, name: affiliateCode.name });
    this.isEditFormVisible = true;
    this.cd.markForCheck();
  }

  doUpdateAffiliateCode() {
    this.isEditFormLoading = true;
    this.cd.markForCheck();

    if (this.editForm.valid) {
      const value = this.editForm.getRawValue();
      this.store.dispatch(new UpdateAffiliateCode(value.id, { code: value.code, name: value.name }));
    } else {
      updateAndMarkControlAsDirty(this.editForm);
      this.isEditFormLoading = false;
      this.cd.markForCheck();
    }
  }

  closeEditForm(): void {
    this.isEditFormVisible = false;
    this.cd.markForCheck();
  }
}
