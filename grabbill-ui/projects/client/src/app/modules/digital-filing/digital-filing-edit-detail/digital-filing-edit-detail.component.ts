import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import {
  AbstractControl,
  AsyncValidatorFn,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { map, Observable, of, switchMap, tap } from 'rxjs';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import {
  BaseIndexFieldModel,
  DataType,
  DigitalFilingTypeModel,
  getErrorMessage,
  resolveErrorMessage,
  updateAndMarkControlAsDirty,
} from '@grabbill/lib';
import { DigitalFilingApi } from '../../../../api/digital-filing.api';
import {
  GetDigitalFilingType,
  NewDigitalFilingType,
  ResetDigitalFilingType,
  UpdateDigitalFilingType,
} from '../../../../states/digital-filing/digital-filing.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { DigitalFilingState } from '../../../../states/digital-filing/digital-filing.state';
import { csvSeparators } from '../../../../utils/csv-separator';
import { doDeleteIndex } from '../../../../utils/manage-form-array';
import { noWhitespaceValidator } from '../../../../utils/no-whitespace-validator';
import { createDigitalFiling, handleNewDigitalFiling } from '../../../../utils/digital-filing';
import { camelCase } from 'lodash';

export const makeNameValidator = (digitalFilingApi: DigitalFilingApi, originalName?: string): AsyncValidatorFn => {
  return (control: AbstractControl): Observable<ValidationErrors> => {
    const value = control.value;
    if (value === originalName) {
      return of({});
    }

    if (!(/^[a-z0-9 ]+$/i.test(value))) {
      return of({ alphanumericWithSpaceOnly: value });
    }

    return digitalFilingApi
      .validateTypeName(value)
      .pipe(map((result) => (result.data.exist ? { nameExist: true } : {})));
  };
};

@Component({
  selector: 'grabbill-client-digital-filing-edit-detail',
  templateUrl: './digital-filing-edit-detail.component.html',
  styleUrls: ['./digital-filing-edit-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DigitalFilingEditDetailComponent extends NgxsBaseComponent {
  form: UntypedFormGroup;
  isNew = true;
  isLoading = true;
  isInitialize = true;
  typeId?: number;

  @Select(DigitalFilingState.digitalFilingType)
  digitalFilingType$!: Observable<DigitalFilingTypeModel>;

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    private digitalFilingApi: DigitalFilingApi,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      code: [null, [Validators.required, Validators.maxLength(255)]],
      autoPurge: [false, [Validators.required]],
      autoPurgeByDays: [1, [Validators.max(99999999999)]],
      csvSeparator: [',', [Validators.required]],
      indexFields: this.fb.array([]),
    });
  }

  get indexFields() {
    return this.form.controls['indexFields'] as UntypedFormArray;
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.store.dispatch(new ResetDigitalFilingType());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          if (this.typeId) {
            this.isInitialize = false;
            this.cd.markForCheck();
            this.store.dispatch(new GetDigitalFilingType(this.typeId));
            this.isNew = false;
            this.cd.markForCheck();
          } else {
            this.isNew = true;
            this.form.get('name')!.addAsyncValidators(makeNameValidator(this.digitalFilingApi));
            this.toggleAutoPurge(false);
            this.doAddIndexField(
              {
                id: 1,
                seqOrder: 1,
                label: 'Attachment Filename',
                header: 'attachmentFilename',
                required: true,
                dataType: DataType.TEXT,
                referenced: false,
                applicable: true,
              },
              true
            );
          }
        })
      ),
      this.digitalFilingType$.pipe(
        tap((digitalFilingType) => {
          if (!this.isNew && digitalFilingType) {
            this.isInitialize = true;
            this.cd.markForCheck();
            this.form.get('name')!.addAsyncValidators(makeNameValidator(this.digitalFilingApi, digitalFilingType.name));
            this.form.setValue({
              name: digitalFilingType.name,
              code: digitalFilingType.code,
              autoPurge: digitalFilingType.autoPurge,
              autoPurgeByDays: digitalFilingType.autoPurgeByDays,
              csvSeparator: digitalFilingType.csvSeparator,
              indexFields: [],
            });

            this.toggleAutoPurge(digitalFilingType.autoPurge);

            digitalFilingType.indexFields.map((indexField) => {
              this.doAddIndexField(indexField, indexField.seqOrder === 1);
            });
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetDigitalFilingType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'digital-filing', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewDigitalFilingType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Digital filing created`));
            const digitalFiling = this.store.selectSnapshot(DigitalFilingState.digitalFilingType)!;
            createDigitalFiling(this.store, digitalFiling.id, digitalFiling.name);
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateDigitalFilingType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Digital filing updated`));
            const digitalFiling = this.store.selectSnapshot(DigitalFilingState.digitalFilingType);
            this.navigate(['/', 'digital-filing', 'detail', digitalFiling!.id], { tab: 'settings' });
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      handleNewDigitalFiling(this.actions$, this.store, this.navigate.bind(this))
    );
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    this.form.reset();
  }

  submitForm(): void {
    this.isLoading = true;
    this.cd.markForCheck();

    if (this.form.valid) {
      const value = this.form.getRawValue();
      if (this.isNew) {
        this.store.dispatch(
          new NewDigitalFilingType({
            name: value.name,
            code: value.code,
            autoPurge: value.autoPurge,
            autoPurgeByDays: value.autoPurgeByDays,
            csvSeparator: value.csvSeparator,
            indexFields: value.indexFields,
          })
        );
      } else {
        this.store.dispatch(
          new UpdateDigitalFilingType(this.typeId!, {
            name: value.name,
            code: value.code,
            autoPurge: value.autoPurge,
            autoPurgeByDays: value.autoPurgeByDays,
            csvSeparator: value.csvSeparator,
            indexFields: value.indexFields,
          })
        );
      }
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  doAddIndexField(indexField: BaseIndexFieldModel, systemDefined: boolean): void {
    const indexFieldForm = this.fb.group({
      id: [indexField.id, [Validators.required]],
      seqOrder: [indexField.seqOrder, [Validators.required]],
      label: [indexField.label, [Validators.required, Validators.maxLength(255)]],
      header: [indexField.header, [Validators.required, noWhitespaceValidator, Validators.maxLength(255)]],
      required: [indexField.required, [Validators.required]],
      dataType: [indexField.dataType],
      systemDefined: [systemDefined, [Validators.required]],
      referenced: [indexField.referenced, [Validators.required]],
      applicable: [indexField.applicable, [Validators.required]],
    });
    this.indexFields.push(indexFieldForm);
    this.cd.markForCheck();
  }

  addUserDefinedIndexField(): void {
    this.doAddIndexField(
      {
        id: this.indexFields.controls.length + 1,
        seqOrder: this.indexFields.controls.length + 1,
        label: '',
        header: '',
        required: false,
        dataType: DataType.TEXT,
        referenced: false,
        applicable: true,
      },
      false
    );
  }

  getDataTypeOptions() {
    return [DataType.TEXT, DataType.NUMBER, DataType.DATE];
  }

  toggleAutoPurge(isAutoPurge: boolean): void {
    const autoPurgeByDays = this.form.get('autoPurgeByDays');

    if (isAutoPurge) {
      autoPurgeByDays?.enable();
      this.cd.markForCheck();
    } else {
      autoPurgeByDays?.disable();
      this.cd.markForCheck();
    }
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doDeleteIndex(formArray: UntypedFormArray, index: number): void {
    doDeleteIndex(formArray, index, this.cd);
    this.refreshIndex();
  }

  refreshIndex() {
    let i = 1;
    this.indexFields.controls.map((control) => {
      control.get('seqOrder')?.setValue(i);
      i++;
    });
  }

  getCsvSeparators() {
    return csvSeparators;
  }

  doLabelChange(control: AbstractControl) {
    const header = control.get('header')?.value;
    if (!header || header === '') {
      const label = control.get('label')?.value;
      control.get('header')?.setValue(camelCase(label));
    }
  }
}
