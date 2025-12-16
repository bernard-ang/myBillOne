import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { SmsApi } from '../../../../api/sms.api';
import {
  AbstractControl,
  AsyncValidatorFn,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { finalize, map, Observable, of, switchMap, tap } from 'rxjs';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { SmsState } from '../../../../states/sms/sms.state';
import {
  BaseIndexFieldModel,
  ContactFieldModel,
  DataType,
  getErrorMessage,
  makePageable,
  resolveErrorMessage,
  SmsFieldType,
  SmsTypeModel,
  updateAndMarkControlAsDirty,
} from '@grabbill/lib';
import { ActivatedRoute, Params } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { GetSmsType, NewSmsType, ResetSmsType, UpdateSmsType } from '../../../../states/sms/sms.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { noWhitespaceValidator } from '../../../../utils/no-whitespace-validator';
import { doDeleteIndex } from 'projects/client/src/utils/manage-form-array';
import { createSms, handleNewSms, smsContentHelp } from '../../../../utils/sms';
import { observeContactFields } from '../../../../utils/observe-contact-fields';
import { ContactFieldState } from '../../../../states/contact-field/contact-field.state';
import { GetContactFields, ResetContactFields } from '../../../../states/contact-field/contact-field.state-actions';
import { ContactGroupApi } from '../../../../api/contact-group.api';
import { AuthState } from '../../../../states/auth/auth.state';
import { countBytes, removePlaceholders } from '../../../../utils/count-bytes';
import { camelCase } from 'lodash';

export const makeNameValidator = (smsApi: SmsApi, originalName?: string): AsyncValidatorFn => {
  return (control: AbstractControl): Observable<ValidationErrors> => {
    const value = control.value;
    if (value === originalName) {
      return of({});
    }

    if (!(/^[a-z0-9 ]+$/i.test(value))) {
      return of({ alphanumericWithSpaceOnly: value });
    }

    return smsApi.validateTypeName(value).pipe(map((result) => (result.data.exist ? { nameExist: true } : {})));
  };
};

@Component({
  selector: 'grabbill-client-sms-type-edit-detail',
  templateUrl: './sms-type-edit-detail.component.html',
  styleUrls: ['./sms-type-edit-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SmsTypeEditDetailComponent extends NgxsBaseComponent {
  form: UntypedFormGroup;
  isNew = true;
  isLoading = false;
  isInitialize = true;
  typeId?: number;
  smsFieldType = SmsFieldType;

  @Select(SmsState.smsType)
  smsType$!: Observable<SmsTypeModel>;

  @Select(ContactFieldState.contactFields)
  contactFields$!: Observable<ContactFieldModel[]>;

  contactFields: BaseIndexFieldModel[] = [];

  listOfOption: Array<{ value: string; text: string }> = [];
  nzFilterOption = (): boolean => true;
  isContactGroupLoading = false;

  smsContentHelp = smsContentHelp;

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    private smsApi: SmsApi,
    private modal: NzModalService,
    private contactGroupApi: ContactGroupApi,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      code: [null, [Validators.maxLength(255)]],

      smsFieldType: [SmsFieldType.INDEX_FIELD, [Validators.required]],
      smsContent: ['', [Validators.required, Validators.maxLength(255)]],

      contactGroup: [''],

      indexFields: this.fb.array([]),
    });
  }

  get indexFields() {
    return this.form.controls['indexFields'] as UntypedFormArray;
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.store.dispatch(new ResetSmsType());
    this.store.dispatch(new ResetContactFields());
    this.store.dispatch(new GetContactFields());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          if (this.typeId) {
            this.isInitialize = false;
            this.cd.markForCheck();
            this.store.dispatch(new GetSmsType(this.typeId));
            this.isNew = false;
            this.cd.markForCheck();
          } else {
            this.isNew = true;
            this.form.get('name')!.addAsyncValidators(makeNameValidator(this.smsApi));
            this.doAddIndexField(
              {
                id: 1,
                seqOrder: 1,
                label: 'Mobile No',
                header: 'mobileNo',
                required: true,
                dataType: DataType.TEXT,
                referenced: false,
                applicable: true,
              },
              true,
              true
            );

            this.form.get('smsFieldType')?.enable();
          }
        })
      ),
      this.smsType$.pipe(
        tap((smsType) => {
          if (!this.isNew && smsType) {
            this.isInitialize = true;
            this.cd.markForCheck();
            this.form.get('name')!.addAsyncValidators(makeNameValidator(this.smsApi, smsType.name));
            this.form.setValue({
              name: smsType.name,
              code: smsType.code || null,
              smsFieldType: smsType.smsFieldType,
              smsContent: smsType.smsContent,
              contactGroup: smsType.contactGroupId ? smsType.contactGroupId.toString() : 'all',
              indexFields: [],
            });

            smsType.indexFields.map((indexField) => {
              this.doAddIndexField(indexField, indexField.seqOrder <= 1);
            });

            this.form.get('smsFieldType')?.disable();

            this.search(smsType.contactGroupName ? smsType.contactGroupName : '');
          }
        })
      ),
      observeContactFields(this.contactFields$).pipe(tap((fields) => (this.contactFields = fields))),
      this.actions$.pipe(
        ofActionCompleted(GetSmsType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'sms', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewSmsType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `SMS template created`));
            const sms = this.store.selectSnapshot(SmsState.smsType)!;
            createSms(this.store, sms, sms.name);
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateSmsType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `SMS template updated`));
            const sms = this.store.selectSnapshot(SmsState.smsType);
            this.navigate(['/', 'sms', 'detail', sms!.id], { tab: 'settings' });
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      handleNewSms(this.actions$, this.store, this.navigate.bind(this))
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

      const mismatchMergeFields = this.getMismatchMergeFields();
      if (mismatchMergeFields.length > 0) {
        this.displayMismatchFieldErrorModal(mismatchMergeFields);
        this.isLoading = false;
        return;
      }

      const user = this.store.selectSnapshot(AuthState.user);
      if (this.isNew) {
        this.store.dispatch(
          new NewSmsType({
            name: value.name,
            code: value.code,
            indexFields: value.indexFields,
            smsFieldType: value.smsFieldType,
            smsFrom: user?.account.companyContactNo || '',
            smsContent: value.smsContent,
            contactGroupId: value.contactGroup === 'all' ? undefined : Number(value.contactGroup),
          })
        );
      } else {
        this.store.dispatch(
          new UpdateSmsType(this.typeId!, {
            name: value.name,
            code: value.code,
            indexFields: value.indexFields,
            smsFieldType: value.smsFieldType,
            smsFrom: user?.account.companyContactNo || '',
            smsContent: value.smsContent,
            contactGroupId: value.contactGroup === 'all' ? undefined : Number(value.contactGroup),
          })
        );
      }
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  doAddIndexField(indexField: BaseIndexFieldModel, systemDefined: boolean, show = true): void {
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
      show: [show],
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

  getIndexFields() {
    return this.form.get('indexFields')!.value;
  }

  getMismatchMergeFields(): string[] {
    let fields: BaseIndexFieldModel[];

    if (this.form.get('smsFieldType')?.value === SmsFieldType.INDEX_FIELD) {
      fields = this.getIndexFields();
    } else {
      fields = this.contactFields;
    }

    const availableFields = fields.map((field: any) => field.header);

    const mismatchMergeFields: string[] = [];

    const mergeFieldMatch = this.form.get('smsContent')!.value.matchAll(/{{[\w _-]+}}/g);
    for (const match of mergeFieldMatch) {
      const field = match[0].replace('{{', '').replace('}}', '');
      if (!availableFields.includes(field)) {
        mismatchMergeFields.push(field);
      }
    }

    return mismatchMergeFields;
  }

  private displayMismatchFieldErrorModal(mismatchMergeFields: string[]) {
    this.modal.error({
      nzTitle: 'Mismatch field found',
      nzContent: `Mismatch field [${mismatchMergeFields.join(', ')}] found in sms content`,
    });
  }

  search(value: string): void {
    this.isContactGroupLoading = true;
    this.cd.markForCheck();

    this.autoUnsubscribeOnChanges(
      this.contactGroupApi.getContactGroups(makePageable(), value).pipe(
        tap((response) => {
          const listOfOption: Array<{ value: string; text: string }> = [{ value: 'all', text: '[All Contacts]' }];
          response.data.items.forEach((item) => {
            listOfOption.push({
              value: item.id.toString(),
              text: item.name,
            });
          });
          this.listOfOption = listOfOption;
        }),
        finalize(() => {
          this.isContactGroupLoading = false;
          this.cd.markForCheck();
        })
      )
    );
  }

  doLabelChange(control: AbstractControl) {
    const header = control.get('header')?.value;
    if (!header || header === '') {
      const label = control.get('label')?.value;
      control.get('header')?.setValue(camelCase(label));
    }
  }

  byteCount(value: string): number {
    const text = 'RM0 ' + removePlaceholders(value);
    return countBytes(text.trim());
  }
}
