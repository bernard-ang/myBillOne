import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppCommonModule } from '../../app-common/app-common.module';
import {
  AbstractControl,
  AsyncValidatorFn,
  ReactiveFormsModule,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { map, Observable, of, switchMap, tap } from 'rxjs';
import { MultiTemplateWhatsappApi } from '../../../../api/multi-template-whatsapp.api';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { WhatsAppState } from '../../../../states/whatsapp/whatsapp.state';
import {
  BaseIndexFieldModel,
  DataType,
  getErrorMessage,
  MultiTemplateWhatsappTemplateModel,
  MultiTemplateWhatsappTypeModel,
  resolveErrorMessage,
  updateAndMarkControlAsDirty,
  WhatsappTemplateModel,
  WhatsAppTemplateParamModel,
  WhatsappTemplateStatus,
} from '@grabbill/lib';
import { ActivatedRoute, Params } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageModule, NzMessageService } from 'ng-zorro-antd/message';
import {
  GetMultiTemplateWhatsAppType,
  GetWhatsAppTemplates,
  NewMultiTemplateWhatsAppType,
  ResetMultiTemplateWhatsAppType,
  UpdateMultiTemplateWhatsAppType,
} from '../../../../states/whatsapp/whatsapp.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import {
  getWhatsAppTemplateBody,
  getWhatsAppTemplateBodyParams,
  getWhatsAppTemplateButton,
  getWhatsAppTemplateButtonUrl,
  getWhatsAppTemplateFooter,
  getWhatsAppTemplateHeader,
} from '../../../../utils/whatsapp-template';
import { doDeleteAllIndex, doDeleteIndex } from '../../../../utils/manage-form-array';
import { createMultiTemplateWhatsApp, handleMultiTemplateNewWhatsApp } from '../../../../utils/whatsapp';
import { noWhitespaceValidator } from '../../../../utils/no-whitespace-validator';
import { csvSeparators } from '../../../../utils/csv-separator';
import { camelCase } from 'lodash';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzStepsModule } from 'ng-zorro-antd/steps';
import { NzListModule } from "ng-zorro-antd/list";
import { NzSkeletonModule } from "ng-zorro-antd/skeleton";
import { NzFormModule } from "ng-zorro-antd/form";
import { NzTypographyModule } from "ng-zorro-antd/typography";
import { NzSwitchModule } from "ng-zorro-antd/switch";
import { NzSelectModule } from "ng-zorro-antd/select";
import { NzTableModule } from "ng-zorro-antd/table";
import { NzCheckboxModule } from "ng-zorro-antd/checkbox";
import { NzInputModule } from "ng-zorro-antd/input";
import { IconsProviderModule } from "../../../icons-provider.module";

export const makeNameValidator = (
  multiTemplateWhatsAppApi: MultiTemplateWhatsappApi,
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

    return multiTemplateWhatsAppApi
      .validateTypeName(value)
      .pipe(map((result) => (result.data.exist ? { nameExist: true } : {})));
  };
};

@Component({
  selector: 'grabbill-client-multi-template-whatsapp-type-edit-detail',
  standalone: true,
  imports: [
    CommonModule,
    AppCommonModule,
    NzMessageModule,
    NzButtonModule,
    NzListModule,
    NzSkeletonModule,
    NzFormModule,
    NzTypographyModule,
    NzSwitchModule,
    NzSelectModule,
    NzTableModule,
    NzCheckboxModule,
    NzInputModule,
    NzToolTipModule,
    NzStepsModule,
    ReactiveFormsModule,
    IconsProviderModule
  ],
  templateUrl: './multi-template-whatsapp-type-edit-detail.component.html',
  styleUrl: './multi-template-whatsapp-type-edit-detail.component.less',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MultiTemplateWhatsappTypeEditDetailComponent extends NgxsBaseComponent {
  form: UntypedFormGroup;
  isNew = true;
  isLoading = false;
  isInitialize = true;
  isTemplateProcess = false;
  typeId?: number;

  @Select(WhatsAppState.multiTemplateWhatsAppType)
  multiTemplateWhatsAppType$!: Observable<MultiTemplateWhatsappTypeModel>;

  @Select(WhatsAppState.templates)
  whatsAppTemplates$!: Observable<WhatsappTemplateModel[]>;

  whatsAppTemplates: WhatsappTemplateModel[] = [];
  whatsAppTemplateOptions: WhatsappTemplateModel[] = [];

  whatsAppSelectedTemplates: (WhatsappTemplateModel|undefined)[] = [];
  whatsAppTemplatesParamTableData: string[][] = [];

  multiTemplateWhatsAppType?: MultiTemplateWhatsappTypeModel;

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    private multiTemplateWhatsAppApi: MultiTemplateWhatsappApi,
    private modal: NzModalService,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      code: [null, [Validators.required, Validators.maxLength(255)]],

      hasAttachment: [false, [Validators.required]],
      passwordProtected: [false, [Validators.required]],

      csvSeparator: [',', [Validators.required]],
      indexFields: this.fb.array([]),

      templates: this.fb.array([]),
    });
  }

  get indexFields() {
    return this.form.controls['indexFields'] as UntypedFormArray;
  }

  get templates() {
    return this.form.controls['templates'] as UntypedFormArray;
  }

  getWhatsAppTemplateParams(control: AbstractControl) {
    return control.get('whatsAppTemplateParams') as UntypedFormArray;
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.store.dispatch(new GetWhatsAppTemplates());
    this.store.dispatch(new ResetMultiTemplateWhatsAppType());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          if (this.typeId) {
            this.isInitialize = false;
            this.cd.markForCheck();
            this.store.dispatch(new GetMultiTemplateWhatsAppType(this.typeId));
            this.isNew = false;
            this.cd.markForCheck();
          } else {
            this.isNew = true;
            this.form.get('name')!.addAsyncValidators(makeNameValidator(this.multiTemplateWhatsAppApi));
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
            this.doAddIndexField(
              {
                id: 4,
                seqOrder: 4,
                label: 'Template',
                header: 'template',
                required: true,
                dataType: DataType.TEXT,
                referenced: false,
                applicable: true,
              },
              true,
              true
            );
            this.addTemplate(0);
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
      this.multiTemplateWhatsAppType$.pipe(
        tap((whatsAppType) => {
          if (!this.isNew && whatsAppType) {
            this.multiTemplateWhatsAppType = whatsAppType;
            this.cd.markForCheck();
            this.form
              .get('name')!
              .addAsyncValidators(makeNameValidator(this.multiTemplateWhatsAppApi, whatsAppType.name));
            this.form.setValue({
              name: whatsAppType.name,
              code: whatsAppType.code || null,
              hasAttachment: whatsAppType.hasAttachment,
              passwordProtected: whatsAppType.passwordProtected,
              csvSeparator: whatsAppType.csvSeparator,
              indexFields: [],
              templates: [],
            });

            whatsAppType.indexFields.map((indexField) => {
              this.doAddIndexField(indexField, indexField.seqOrder <= 3);
            });

            for (let j = 0; j < whatsAppType.whatsAppTemplates.length; j++) {
              const currentTemplate = whatsAppType.whatsAppTemplates[j];
              this.addTemplate(j, currentTemplate)
            }

            this.toggleAttachment(whatsAppType.hasAttachment);
            this.togglePasswordProtect(whatsAppType.passwordProtected);

            this.processWhatsappTemplate();

            this.isInitialize = true;
          }
        })
      ),
      this.whatsAppTemplates$.pipe(
        tap((whatsAppTemplates) => {
          if(whatsAppTemplates) {
            this.whatsAppTemplates = whatsAppTemplates;
            this.whatsAppTemplateOptions = whatsAppTemplates.filter(
              (item) =>
                item.status === WhatsappTemplateStatus.APPROVED &&
                getWhatsAppTemplateButtonUrl(item).includes('/ack/whatsapp?recordId={{1}}')
            );

            this.processWhatsappTemplate();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetMultiTemplateWhatsAppType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'mt-whatsapp', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewMultiTemplateWhatsAppType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `WhatsApp created`));
            const whatsApp = this.store.selectSnapshot(WhatsAppState.multiTemplateWhatsAppType)!;
            createMultiTemplateWhatsApp(this.store, whatsApp, whatsApp.name);
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateMultiTemplateWhatsAppType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `WhatsApp updated`));
            const whatsApp = this.store.selectSnapshot(WhatsAppState.multiTemplateWhatsAppType);
            this.navigate(['/', 'mt-whatsapp', 'detail', whatsApp!.id], { tab: 'settings' });
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      handleMultiTemplateNewWhatsApp(this.actions$, this.store, this.navigate.bind(this))
    );
  }

  addTemplate(index: number, multiTemplateWhatsappTemplate?: MultiTemplateWhatsappTemplateModel) {
    const group = this.fb.group({
      whatsAppTemplate: [multiTemplateWhatsappTemplate ? multiTemplateWhatsappTemplate.whatsAppTemplateName: null, []],
      whatsAppTemplateParams: this.fb.array([]),
    });
    this.templates.push(group)
    this.whatsAppSelectedTemplates.push(undefined);
    this.whatsAppTemplatesParamTableData.push([]);

    this.autoUnsubscribeOnChanges(
      group.get('whatsAppTemplate')!.valueChanges.pipe(
        tap((whatsAppTemplate) => {
          if (this.isInitialize) {
            if (whatsAppTemplate) {
              this.whatsAppSelectedTemplates[index] = this.whatsAppTemplates.find((template) => template.name === whatsAppTemplate);
              const currentTemplate = this.whatsAppSelectedTemplates[index];

              if (currentTemplate) {
                doDeleteAllIndex(this.getWhatsAppTemplateParams(group));
                const params = getWhatsAppTemplateBodyParams(currentTemplate);
                const matches = [...new Set(params)]
                if (matches) {
                  for (const match of matches) {
                    this.doAddWhatsappTemplateBodyParam(group, match);
                  }
                  this.whatsAppTemplatesParamTableData[index] = [''];
                } else {
                  this.whatsAppTemplatesParamTableData[index] = [];
                }
                this.cd.markForCheck();
                return;
              }
            }

            this.whatsAppSelectedTemplates[index] = undefined;
            doDeleteAllIndex(this.getWhatsAppTemplateParams(group));
            this.whatsAppTemplatesParamTableData[index] = [];

            this.cd.markForCheck();
          }
        })
      ),
    )
  }

  deleteTemplate(index: number) {
    doDeleteIndex(this.templates, index, this.cd)
  }

  private processWhatsappTemplate() {
    if (this.whatsAppTemplates && this.multiTemplateWhatsAppType && !this.isTemplateProcess) {
      this.isTemplateProcess = true;
      const currentWhatsAppTemplates = this.multiTemplateWhatsAppType.whatsAppTemplates;
      for (let i = 0; i < currentWhatsAppTemplates.length; i++) {
        const currentTemplate = currentWhatsAppTemplates[i];
        if (currentTemplate.whatsAppTemplateName) {
          const control = this.templates.at(i);
          if (this.whatsAppTemplates) {
            this.whatsAppSelectedTemplates[i] = this.whatsAppTemplates.find(
              (template) => template.name === currentTemplate.whatsAppTemplateName
            );
            if (this.whatsAppSelectedTemplates[i]) {
              currentTemplate.whatsAppTemplateParams.map((param) => {
                this.doAddWhatsappTemplateBodyParam(control, param.index, param);
              });

              if (currentTemplate.whatsAppTemplateParams.length > 0) {
                this.whatsAppTemplatesParamTableData[i] = [''];
              } else {
                this.whatsAppTemplatesParamTableData[i] = [];
              }
            }
          }
        }
      }
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
      for (const whatsAppSelectedTemplate of this.whatsAppSelectedTemplates) {
        if (whatsAppSelectedTemplate) {
          const header = getWhatsAppTemplateHeader(whatsAppSelectedTemplate);
          if (header !== '-' && !value.hasAttachment) {
            this.modal.error({
              nzTitle: 'Attachment Required',
              nzContent: `Template (${whatsAppSelectedTemplate.name}) required attachment`,
            });
            return;
          }
        }
      }

      if (this.isNew) {
        this.store.dispatch(
          new NewMultiTemplateWhatsAppType({
            name: value.name,
            code: value.code,
            csvSeparator: value.csvSeparator,
            indexFields: value.indexFields,

            hasAttachment: value.hasAttachment,
            passwordProtected: value.passwordProtected,
            whatsAppTemplates: value.templates.map((current: {
              whatsAppTemplate: string;
              whatsAppTemplateParams: WhatsAppTemplateParamModel[];
            }) => ({
              whatsAppTemplateName: current.whatsAppTemplate,
              whatsAppTemplateParams: current.whatsAppTemplateParams
            })),
          })
        );
      } else {
        this.store.dispatch(
          new UpdateMultiTemplateWhatsAppType(this.typeId!, {
            name: value.name,
            code: value.code,
            csvSeparator: value.csvSeparator,
            indexFields: value.indexFields,
            hasAttachment: value.hasAttachment,
            passwordProtected: value.passwordProtected,
            whatsAppTemplates: value.templates.map((current: {
              whatsAppTemplate: string;
              whatsAppTemplateParams: WhatsAppTemplateParamModel[];
            }) => ({
              whatsAppTemplateName: current.whatsAppTemplate,
              whatsAppTemplateParams: current.whatsAppTemplateParams
            })),
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

  getWhatsappTemplateHeader(index: number) {
    const template = this.whatsAppSelectedTemplates[index];
    return template ? getWhatsAppTemplateHeader(template) : '-';
  }

  getWhatsappTemplateBody(index: number) {
    const template = this.whatsAppSelectedTemplates[index];
    return template ? getWhatsAppTemplateBody(template) : '-';
  }

  getWhatsappTemplateFooter(index: number) {
    const template = this.whatsAppSelectedTemplates[index];
    return template ? getWhatsAppTemplateFooter(template) : '-';
  }

  getWhatsappTemplateButton(index: number) {
    const template = this.whatsAppSelectedTemplates[index];
    return template ? getWhatsAppTemplateButton(template) : '-';
  }

  doAddWhatsappTemplateBodyParam(control: AbstractControl, index: string, param?: WhatsAppTemplateParamModel): void {
    const paramForm = this.fb.group({
      id: [param?.id || this.getWhatsAppTemplateParams(control).length + 1, [Validators.required]],
      index: [param?.index || index, [Validators.required]],
      field: [param?.field, [Validators.required]],
    });
    this.getWhatsAppTemplateParams(control).push(paramForm);
    this.cd.markForCheck();
  }

  getIndexFieldHeader() {
    return this.indexFields
      .getRawValue()
      .filter((item) => item.applicable && item.show)
      .map((item) => item.header);
  }
}

