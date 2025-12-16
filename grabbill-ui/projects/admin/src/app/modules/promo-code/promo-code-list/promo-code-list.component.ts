import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import {
  DiscountOccurrence,
  DiscountType,
  getErrorMessage,
  PageableModel,
  PromoCodeBasicModel,
  resolveErrorMessage,
  SearchResultPayloadModel,
  updateAndMarkControlAsDirty,
} from '@grabbill/lib';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import {
  DeletePromoCode,
  NewPromoCode,
  QueryPromoCodes,
  ResetPromoCodes,
  UpdatePromoCode,
  UpdatePromoCodeStatus,
} from '../../../../states/promo-code/promo-code.state-actions';
import { ShowMessage } from '../../../../states/admin-common/admin-common.state-actions';
import { filter } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { PromoCodeState } from '../../../../states/promo-code/promo-code-state';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { endOfDay, isAfter, startOfDay } from "date-fns";

export const startDateValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  if (control && control.value && control.parent) {
    const endDate = control.parent.get('end')?.value;
    if (endDate && isAfter(control.value, endOfDay(endDate))) {
      return { 'Start date is after end date': true }
    }
  }

  return null;
}

export const endDateValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  if (control && control.value && control.parent) {
    const startDate = control.parent.get('start')?.value;
    if (startDate && isAfter(startDate, endOfDay(control.value))) {
      return { 'End date is before start date': true }
    }
  }

  return null;
}

export const positive: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  if (control.value <= 0) {
    return { 'Value must be greater than positive': true }
  }

  return null;
}

@Component({
  selector: 'grabbill-admin-promo-code-list',
  templateUrl: './promo-code-list.component.html',
  styleUrls: ['./promo-code-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PromoCodeListComponent extends NgxsBaseComponent {
  @Select(PromoCodeState.promoCodeSearchResult)
  promoCodeSearchResult$!: Observable<SearchResultPayloadModel<PromoCodeBasicModel>>;

  @Select(PromoCodeState.promoCodePageable)
  promoCodePageable$!: Observable<PageableModel>;

  isInitialize = false;
  isTableLoading = false;
  searchChange$ = new BehaviorSubject('');

  isCreateFormLoading = false;
  isCreateFormVisible = false;
  createForm: UntypedFormGroup;

  isEditFormLoading = false;
  isEditFormVisible = false;
  editForm: UntypedFormGroup;

  discountType = DiscountType;
  discountOccurrence = DiscountOccurrence;

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
      name: ['', [Validators.required, Validators.maxLength(100)]],
      code: ['', [Validators.required, Validators.maxLength(10)]],
      start: [null, [startDateValidator]],
      end: [null, [endDateValidator]],
      discountType: [DiscountType.PERCENTAGE, [Validators.required]],
      discount: [0, [Validators.required, positive]],
      discountOccurrence: [DiscountOccurrence.FOREVER, [Validators.required]],
      discountOccurrenceCount: [0, [Validators.required, positive]],
      active: [true, [Validators.required]],
      remarks: ['', [Validators.maxLength(250)]],
    });

    this.editForm = this.fb.group({
      id: ['', [Validators.required]],
      name: ['', [Validators.required, Validators.maxLength(100)]],
      start: [null, [startDateValidator]],
      end: [null, [endDateValidator]],
      discountOccurrence: [DiscountOccurrence.FOREVER, [Validators.required]],
      discountOccurrenceCount: [0, [Validators.required, positive]],
      remarks: ['', [Validators.maxLength(250)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetPromoCodes());

    this.autoUnsubscribe(
      this.promoCodeSearchResult$.pipe(
        tap(() => {
          this.isTableLoading = false;
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeletePromoCode),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Promo code deleted`));
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewPromoCode),
        switchMap((data: ActionCompletion) => {
          this.isCreateFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isCreateFormVisible = false;

            this.store.dispatch(new ShowMessage('info', `New promo code added`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdatePromoCode),
        switchMap((data: ActionCompletion) => {
          this.isEditFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isEditFormVisible = false;

            this.store.dispatch(new ShowMessage('info', `Promo code edited`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdatePromoCodeStatus),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Promo code status updated`));
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
              new QueryPromoCodes(
                {
                  ...this.store.selectSnapshot(PromoCodeState.promoCodePageable),
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

  dateFormat() {
    return environment.config.dateFormat;
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(PromoCodeState.promoCodePageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(new QueryPromoCodes(pageable));
    this.isInitialize = true;
  }

  doSearch(event: any) {
    this.isTableLoading = true;
    this.searchChange$.next(event.target.value);
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doDeletePromoCode(promoCode: PromoCodeBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete promo code ${promoCode.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeletePromoCode(promoCode.id));
      },
      nzCancelText: 'No',
    });
  }

  doUpdatePromoCodeStatus(promoCode: PromoCodeBasicModel) {
    this.modal.confirm({
      nzTitle: `${promoCode.active ? 'Disable' : 'Enable'} ${promoCode.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new UpdatePromoCodeStatus(promoCode.id, { active: !promoCode.active }));
      },
      nzCancelText: 'No',
    });
  }

  doOpenCreateForm() {
    this.createForm.reset({
      name: '',
      code: '',
      discountType: DiscountType.PERCENTAGE,
      discount: 0,
      discountOccurrence: DiscountOccurrence.FOREVER,
      discountOccurrenceCount: 0,
      active: true,
      remarks: '',
    });
    this.isCreateFormVisible = true;
    this.handleDiscountOccurrenceChange(this.createForm, DiscountOccurrence.FOREVER);
    this.cd.markForCheck();
  }

  doCreatePromoCode() {
    this.isCreateFormLoading = true;
    this.cd.markForCheck();

    if (this.createForm.valid) {
      const value = this.createForm.getRawValue();
      this.store.dispatch(
        new NewPromoCode({
          code: value.code,
          name: value.name,
          start: value.start ? startOfDay(value.start): undefined,
          end: value.end ? endOfDay(value.end): undefined,
          discountType: value.discountType,
          discount: value.discount,
          discountOccurrence: value.discountOccurrence,
          discountOccurrenceCount: value.discountOccurrenceCount,
          active: value.active,
          remarks: value.remarks,
        })
      );
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

  doOpenEditForm(promoCode: PromoCodeBasicModel) {
    this.editForm.reset({
      id: promoCode.id,
      name: promoCode.name,
      start: promoCode.start ? new Date(promoCode.start): undefined,
      end: promoCode.end ? new Date(promoCode.end): undefined,
      discountOccurrence: promoCode.discountOccurrence,
      discountOccurrenceCount: promoCode.discountOccurrenceCount,
      remarks: promoCode.remarks,
    });
    this.isEditFormVisible = true;
    this.handleDiscountOccurrenceChange(this.editForm, promoCode.discountOccurrence);
    this.cd.markForCheck();
  }

  doEditPromoCode() {
    this.isEditFormLoading = true;
    this.cd.markForCheck();

    if (this.editForm.valid) {
      const value = this.editForm.getRawValue();
      this.store.dispatch(
        new UpdatePromoCode(value.id, {
          name: value.name,
          start: value.start ? startOfDay(value.start): undefined,
          end: value.end ? endOfDay(value.end): undefined,
          discountOccurrence: value.discountOccurrence,
          discountOccurrenceCount: value.discountOccurrenceCount,
          remarks: value.remarks,
        })
      );
    } else {
      updateAndMarkControlAsDirty(this.createForm);
      this.isEditFormLoading = false;
      this.cd.markForCheck();
    }
  }

  closeEditForm(): void {
    this.isEditFormVisible = false;
    this.cd.markForCheck();
  }

  handleDiscountOccurrenceChange(form: UntypedFormGroup, discountOccurrence: DiscountOccurrence) {
    if (discountOccurrence === DiscountOccurrence.FOREVER) {
      form.get('discountOccurrenceCount')?.disable();
      form.get('discountOccurrenceCount')?.setValue(0);
    } else {
      form.get('discountOccurrenceCount')?.enable();
    }
  }

  handleStartDateChange(form: UntypedFormGroup) {
    form.get('end')?.updateValueAndValidity();
  }

  handleEndDateChange(form: UntypedFormGroup) {
    form.get('start')?.updateValueAndValidity();
  }
}
