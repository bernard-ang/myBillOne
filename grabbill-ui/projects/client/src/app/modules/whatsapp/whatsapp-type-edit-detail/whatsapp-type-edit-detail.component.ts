import { WhatsAppApi } from "../../../../api/whatsapp.api";
import {
  AbstractControl,
  AsyncValidatorFn,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  Validators
} from "@angular/forms";
import { map, Observable, of, switchMap, tap } from "rxjs";
import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { WhatsAppState } from "../../../../states/whatsapp/whatsapp.state";
import {
  BaseIndexFieldModel,
  DataType,
  getErrorMessage,
  resolveErrorMessage,
  updateAndMarkControlAsDirty,
  WhatsappTemplateModel,
  WhatsAppTemplateParamModel,
  WhatsappTemplateStatus,
  WhatsAppTypeModel
} from "@grabbill/lib";
import { ActivatedRoute, Params } from "@angular/router";
import { NzModalService } from "ng-zorro-antd/modal";
import { NzMessageService } from "ng-zorro-antd/message";
import {
  GetWhatsAppTemplates,
  GetWhatsAppType,
  NewWhatsAppType,
  ResetWhatsAppType,
  UpdateWhatsAppType
} from "../../../../states/whatsapp/whatsapp.state-actions";
import { noWhitespaceValidator } from "../../../../utils/no-whitespace-validator";
import { ShowMessage } from "../../../../states/common/common.state-actions";
import { doDeleteAllIndex, doDeleteIndex } from "../../../../utils/manage-form-array";
import { createWhatsApp, handleNewWhatsApp } from "../../../../utils/whatsapp";
import { csvSeparators } from "../../../../utils/csv-separator";
import { camelCase } from "lodash";
import {
  getWhatsAppTemplateBody,
  getWhatsAppTemplateBodyParams,
  getWhatsAppTemplateButton, getWhatsAppTemplateButtonUrl,
  getWhatsAppTemplateFooter,
  getWhatsAppTemplateHeader
} from "../../../../utils/whatsapp-template";

export const makeNameValidator = (
  whatsAppApi: WhatsAppApi,
  originalName?: string
): AsyncValidatorFn => {
  return (control: AbstractControl): Observable<ValidationErrors> => {
    const value = control.value;
    if (value === originalName) {
      return of({});
    }

    if (!/^[a-z0-9 ]+$/i.test(value)) {
      return of({ alphanumericWithSpaceOnly: value });
    }

    return whatsAppApi
      .validateTypeName(value)
      .pipe(map((result) => (result.data.exist ? { nameExist: true } : {})));
  };
};

@Component({
  selector: 'grabbill-client-whatsApp-type-edit-detail',
  templateUrl: './whatsapp-type-edit-detail.component.html',
  styleUrls: ['./whatsapp-type-edit-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WhatsappTypeEditDetailComponent extends NgxsBaseComponent {
  form: UntypedFormGroup;
  isNew = true;
  isLoading = false;
  isInitialize = true;
  typeId?: number;

  @Select(WhatsAppState.whatsAppType)
  whatsAppType$!: Observable<WhatsAppTypeModel>;

  @Select(WhatsAppState.templates)
  whatsAppTemplates$!: Observable<WhatsappTemplateModel[]>;

  whatsAppTemplates: WhatsappTemplateModel[] = [];
  whatsAppTemplateOptions: WhatsappTemplateModel[] = [];
  whatsAppTemplate?: WhatsappTemplateModel;
  whatsAppTemplateParamTableData: string[] = [];

  whatsAppType?: WhatsAppTypeModel;

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    private whatsAppApi: WhatsAppApi,
    private modal: NzModalService,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      code: [null, [Validators.maxLength(255)]],

      hasAttachment: [false, [Validators.required]],
      passwordProtected: [false, [Validators.required]],

      csvSeparator: [',', [Validators.required]],
      indexFields: this.fb.array([]),

      whatsAppTemplate: [null, []],
      whatsAppTemplateParams: this.fb.array([]),
    });
  }

  get indexFields() {
    return this.form.controls['indexFields'] as UntypedFormArray;
  }

  get whatsAppTemplateParams() {
    return this.form.controls['whatsAppTemplateParams'] as UntypedFormArray;
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.store.dispatch(new GetWhatsAppTemplates());
    this.store.dispatch(new ResetWhatsAppType());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          if (this.typeId) {
            this.isInitialize = false;
            this.cd.markForCheck();
            this.store.dispatch(new GetWhatsAppType(this.typeId));
            this.isNew = false;
            this.cd.markForCheck();
          } else {
            this.isNew = true;
            this.form.get('name')!.addAsyncValidators(makeNameValidator(this.whatsAppApi));
            this.doAddIndexField(
              {
                id: 1,
                seqOrder: 1,
                label: 'WhatsApp No',
                header: 'whatsAppNo',
                required: true,
                dataType: DataType.TEXT,
                referenced: false,
                applicable: true,
              },
              true,
              true
            );
            this.doAddIndexField(
              {
                id: 2,
                seqOrder: 2,
                label: 'Attachment Filename',
                header: 'attachmentFilename',
                required: true,
                dataType: DataType.TEXT,
                referenced: false,
                applicable: false,
              },
              true,
              false
            );
            this.doAddIndexField(
              {
                id: 3,
                seqOrder: 3,
                label: 'Attachment Password',
                header: 'attachmentPassword',
                required: true,
                dataType: DataType.TEXT,
                referenced: false,
                applicable: false,
              },
              true,
              false
            );
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetWhatsAppTemplates),
        switchMap((data: ActionCompletion) => {
          this.cd.markForCheck();
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.whatsAppType$.pipe(
        tap((whatsAppType) => {
          if (!this.isNew && whatsAppType) {
            this.whatsAppType = whatsAppType;
            this.cd.markForCheck();
            this.form
              .get('name')!
              .addAsyncValidators(makeNameValidator(this.whatsAppApi, whatsAppType.name));
            this.form.setValue({
              name: whatsAppType.name,
              code: whatsAppType.code || null,
              hasAttachment: whatsAppType.hasAttachment,
              passwordProtected: whatsAppType.passwordProtected,
              csvSeparator: whatsAppType.csvSeparator,
              indexFields: [],
              whatsAppTemplate: whatsAppType.whatsAppTemplateName || null,
              whatsAppTemplateParams: [],
            });

            whatsAppType.indexFields.map((indexField) => {
              this.doAddIndexField(indexField, indexField.seqOrder <= 2);
            });

            this.processWhatsappTemplate(whatsAppType);

            this.toggleAttachment(whatsAppType.hasAttachment);
            this.togglePasswordProtect(whatsAppType.passwordProtected);
          }
        })
      ),
      this.whatsAppTemplates$.pipe(
        tap((whatsAppTemplates) => {
          this.whatsAppTemplates = whatsAppTemplates;
          this.whatsAppTemplateOptions =
            whatsAppTemplates.filter(item =>
              item.status === WhatsappTemplateStatus.APPROVED &&
              getWhatsAppTemplateButtonUrl(item).includes('/ack/whatsapp?recordId={{1}}')
            )

          if (this.whatsAppType && this.whatsAppTemplateParams.controls.length === 0) {
            this.processWhatsappTemplate(this.whatsAppType);
          }
        })
      ),
      this.form.get('whatsAppTemplate')!.valueChanges.pipe(
        tap((whatsAppTemplate) => {

          if (this.isInitialize) {
            if (whatsAppTemplate) {
              this.whatsAppTemplate = this.whatsAppTemplates.find((template) => template.name === whatsAppTemplate);
              if (this.whatsAppTemplate) {
                doDeleteAllIndex(this.whatsAppTemplateParams);
                const matches = getWhatsAppTemplateBodyParams(this.whatsAppTemplate!);
                if (matches) {
                  for (const match of matches) {
                    this.doAddWhatsappTemplateBodyParam(match);
                  }
                  this.whatsAppTemplateParamTableData = [''];
                } else {
                  this.whatsAppTemplateParamTableData = [];
                }
                this.cd.markForCheck();
                return;
              }
            }

            this.whatsAppTemplate = undefined;
            doDeleteAllIndex(this.whatsAppTemplateParams);
            this.whatsAppTemplateParamTableData = [];

            this.cd.markForCheck();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetWhatsAppType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'whatsapp', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewWhatsAppType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `WhatsApp created`));
            const whatsApp = this.store.selectSnapshot(WhatsAppState.whatsAppType)!;
            createWhatsApp(this.store, whatsApp, whatsApp.name);
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateWhatsAppType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `WhatsApp updated`));
            const whatsApp = this.store.selectSnapshot(WhatsAppState.whatsAppType);
            this.navigate(['/', 'whatsapp', 'detail', whatsApp!.id], { tab: 'settings' });
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      handleNewWhatsApp(this.actions$, this.store, this.navigate.bind(this))
    );
  }

  private processWhatsappTemplate(whatsAppType: WhatsAppTypeModel) {
    if (whatsAppType.whatsAppTemplateName) {
      if (this.whatsAppTemplates) {
        this.whatsAppTemplate = this.whatsAppTemplates.find(
          (template) => template.name === whatsAppType.whatsAppTemplateName
        );
        if (this.whatsAppTemplate) {
          whatsAppType.whatsAppTemplateParams.map((param) => {
            this.doAddWhatsappTemplateBodyParam(param.index, param);
          });

          if (whatsAppType.whatsAppTemplateParams.length > 0) {
            this.whatsAppTemplateParamTableData = [''];
          } else {
            this.whatsAppTemplateParamTableData = [];
          }
        }
        this.isInitialize = true;
      }
    } else {
      this.isInitialize = true;
    }
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    this.form.reset();
  }

  submitForm(): void {
    const value = this.form.getRawValue();

    this.isLoading = true;
    this.cd.markForCheck();

    if (this.form.valid) {

      if (this.isNew) {
        this.store.dispatch(
          new NewWhatsAppType({
            name: value.name,
            code: value.code,
            csvSeparator: value.csvSeparator,
            indexFields: value.indexFields,

            hasAttachment: value.hasAttachment,
            passwordProtected: value.passwordProtected,

            whatsAppTemplateName: value.whatsAppTemplate,
            whatsAppTemplateParams: value.whatsAppTemplateParams,
          })
        );
      } else {
        this.store.dispatch(
          new UpdateWhatsAppType(this.typeId!, {
            name: value.name,
            code: value.code,
            csvSeparator: value.csvSeparator,
            indexFields: value.indexFields,
            hasAttachment: value.hasAttachment,
            passwordProtected: value.passwordProtected,
            whatsAppTemplateName: value.whatsAppTemplate,
            whatsAppTemplateParams: value.whatsAppTemplateParams,
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

  getCsvSeparators() {
    return csvSeparators;
  }

  toggleAttachment(value: boolean): void {
    const passwordProtected = this.form.get('passwordProtected');
    const attachmentFieldShow = this.indexFields.at(1)?.get('show');
    const attachmentFieldApplicable = this.indexFields.at(1)?.get('applicable');

    if (value) {
      passwordProtected?.enable();
      attachmentFieldShow?.setValue(true);
      attachmentFieldApplicable?.setValue(true);

      this.cd.markForCheck();
    } else {
      passwordProtected?.disable();
      passwordProtected?.setValue(false);
      this.togglePasswordProtect(false);

      attachmentFieldShow?.setValue(false);
      attachmentFieldApplicable?.setValue(false);

      this.cd.markForCheck();
    }
  }

  togglePasswordProtect(value: boolean): void {
    const passwordProtectedShow = this.indexFields.at(2)?.get('show');
    const passwordProtectedApplicable = this.indexFields.at(2)?.get('applicable');

    if (value) {
      passwordProtectedShow?.setValue(true);
      passwordProtectedApplicable?.setValue(true);
      this.cd.markForCheck();
    } else {
      passwordProtectedShow?.setValue(false);
      passwordProtectedApplicable?.setValue(false);
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

  doLabelChange(control: AbstractControl) {
    const header = control.get('header')?.value;
    if (!header || header === '') {
      const label = control.get('label')?.value;
      control.get('header')?.setValue(camelCase(label));
    }
  }

  getWhatsappTemplateHeader() {
    return this.whatsAppTemplate ? getWhatsAppTemplateHeader(this.whatsAppTemplate) : '-';
  }

  getWhatsappTemplateBody() {
    return this.whatsAppTemplate ? getWhatsAppTemplateBody(this.whatsAppTemplate) : '-';
  }

  getWhatsappTemplateFooter() {
    return this.whatsAppTemplate ? getWhatsAppTemplateFooter(this.whatsAppTemplate) : '-';
  }

  getWhatsappTemplateButton() {
    return this.whatsAppTemplate ? getWhatsAppTemplateButton(this.whatsAppTemplate) : '-';
  }

  doAddWhatsappTemplateBodyParam(index: string, param?: WhatsAppTemplateParamModel): void {
    const paramForm = this.fb.group({
      id: [param?.id || this.whatsAppTemplateParams.length + 1, [Validators.required]],
      index: [param?.index || index, [Validators.required]],
      field: [param?.field, [Validators.required]],
    });
    this.whatsAppTemplateParams.push(paramForm);
    this.cd.markForCheck();
  }

  getIndexFieldHeader() {
    return this.indexFields
      .getRawValue()
      .filter((item) => item.applicable && item.show)
      .map((item) => item.header);
  }
}
