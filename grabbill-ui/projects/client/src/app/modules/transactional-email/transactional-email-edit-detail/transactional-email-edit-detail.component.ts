import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, ViewChild } from "@angular/core";
import { ActivatedRoute, Params } from "@angular/router";
import {
  AbstractControl,
  AsyncValidatorFn,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  Validators
} from "@angular/forms";
import grapesjs, { Editor } from "grapesjs";
import { map, Observable, of, switchMap, tap } from "rxjs";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { NzMessageService } from "ng-zorro-antd/message";
import { NzModalService } from "ng-zorro-antd/modal";
import {
  BaseIndexFieldModel,
  DataType,
  getErrorMessage,
  MailServerModel,
  resolveErrorMessage,
  TransactionalEmailTypeModel,
  updateAndMarkControlAsDirty,
  WhatsappTemplateModel,
  WhatsAppTemplateParamModel,
  WhatsappTemplateStatus
} from "@grabbill/lib";
import { environment } from "../../../../environments/environment";
import { TransactionalEmailApi } from "../../../../api/transactional-email.api";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { ShowMessage } from "../../../../states/common/common.state-actions";
import {
  GetTransactionalEmailType,
  NewTransactionalEmailType,
  ResetTransactionalEmailType,
  UpdateTransactionalEmailType
} from "../../../../states/transactional-email/transactional-email.state-actions";
import { TransactionalEmailState } from "../../../../states/transactional-email/transactional-email.state";
import { MailServerState } from "../../../../states/mail-server/mail-server.state";
import { GetMailServer } from "../../../../states/mail-server/mail-server.state-actions";
import { EmailEditorComponent } from "../../app-common/components/email-editor/email-editor.component";
import { csvSeparators } from "../../../../utils/csv-separator";
import { defaultMjmlTemplate } from "../../../../utils/default-mjml-template";
import { getUpdatedMjmlHtml } from "../../../../utils/get-updated-mjml-html";
import { doDeleteAllIndex, doDeleteIndex } from "../../../../utils/manage-form-array";
import { getUpdatedMjml } from "../../../../utils/get-updated-mjml";
import { noWhitespaceValidator } from "../../../../utils/no-whitespace-validator";
import { createTransactionalEmail, handleNewTransactionEmail } from "../../../../utils/transactional-email";
import { camelCase } from "lodash";
import {
  createCloseEditorConfirmationModal,
  editorOutOfFocus
} from "../../../../utils/create-close-editor-confirmation-modal";
import { WhatsAppState } from "../../../../states/whatsapp/whatsapp.state";
import { GetWhatsAppTemplates } from "../../../../states/whatsapp/whatsapp.state-actions";
import {
  getWhatsAppTemplateBody,
  getWhatsAppTemplateBodyParams,
  getWhatsAppTemplateButton,
  getWhatsAppTemplateFooter,
  getWhatsAppTemplateHeader
} from "../../../../utils/whatsapp-template";

export const makeNameValidator = (
  transactionalEmailApi: TransactionalEmailApi,
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

    return transactionalEmailApi
      .validateTypeName(value)
      .pipe(map((result) => (result.data.exist ? { nameExist: true } : {})));
  };
};

@Component({
  selector: 'grabbill-client-transactional-email-edit-detail',
  templateUrl: './transactional-email-edit-detail.component.html',
  styleUrls: ['./transactional-email-edit-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransactionalEmailEditDetailComponent extends NgxsBaseComponent {
  form: UntypedFormGroup;
  isNew = true;
  isLoading = false;
  isInitialize = true;
  typeId?: number;

  @Select(TransactionalEmailState.transactionalEmailType)
  transactionalEmailType$!: Observable<TransactionalEmailTypeModel>;

  @Select(MailServerState.mailServer)
  mailServer$!: Observable<MailServerModel>;

  @Select(WhatsAppState.templates)
  whatsAppTemplates$!: Observable<WhatsappTemplateModel[]>;

  isEditorVisible = false;
  isEditorLoading = false;

  whatsAppTemplates: WhatsappTemplateModel[] = [];
  whatsAppTemplateOptions: WhatsappTemplateModel[] = [];
  whatsAppTemplateName?: WhatsappTemplateModel;
  whatsAppTemplateParamTableData: string[] = [];

  transactionalEmailType?: TransactionalEmailTypeModel;

  mailServer?: MailServerModel;

  @ViewChild('editor') editor!: ElementRef<EmailEditorComponent>;

  mjml: string = defaultMjmlTemplate(environment.config.appUrl);
  html: string = '';

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    private transactionalEmailApi: TransactionalEmailApi,
    private modal: NzModalService,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      code: [null, [Validators.maxLength(255)]],

      emailFrom: ['', [Validators.email, Validators.required, Validators.maxLength(255)]],
      emailFromName: ['', [Validators.required, Validators.maxLength(255)]],
      emailSubject: ['', [Validators.required, Validators.maxLength(255)]],
      emailContent: ['', [Validators.required]],
      emailMjmlContent: ['', [Validators.required]],

      hasAttachment: [false, [Validators.required]],
      passwordProtected: [false, [Validators.required]],

      archive: [false, [Validators.required]],
      autoPurge: [false, [Validators.required]],
      autoPurgeByDays: [1],

      csvSeparator: [',', [Validators.required]],
      indexFields: this.fb.array([]),

      whatsAppTemplateName: [null, []],
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

    this.store.dispatch(new GetMailServer());
    this.store.dispatch(new GetWhatsAppTemplates());
    this.store.dispatch(new ResetTransactionalEmailType());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          if (this.typeId) {
            this.isInitialize = false;
            this.cd.markForCheck();
            this.store.dispatch(new GetTransactionalEmailType(this.typeId));
            this.isNew = false;
            this.cd.markForCheck();
          } else {
            this.isNew = true;
            this.form.get('name')!.addAsyncValidators(makeNameValidator(this.transactionalEmailApi));
            this.form.get('emailMjmlContent')?.setValue(this.mjml);
            this.toggleAutoPurge(false);
            this.doAddIndexField(
              {
                id: 1,
                seqOrder: 1,
                label: 'Email',
                header: 'email',
                required: true,
                dataType: DataType.EMAIL,
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
                label: 'Mobile No',
                header: 'mobileNo',
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
      this.mailServer$.pipe(
        tap((mailServer) => {
          if (mailServer) {
            this.mailServer = mailServer;
            if (!mailServer.customServer) {
              this.form.get('emailFrom')!.disable();
            }
            if (this.isNew) {
              this.form
                .get('emailFrom')!
                .setValue(mailServer.customServer ? mailServer.smtpFrom : environment.config.smtpFromEmail);
              this.form.get('emailFromName')!.setValue(mailServer.smtpFromName);
            }
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
      this.transactionalEmailType$.pipe(
        tap((transactionalEmailType) => {
          if (!this.isNew && transactionalEmailType) {
            this.transactionalEmailType = transactionalEmailType;
            this.cd.markForCheck();
            this.form
              .get('name')!
              .addAsyncValidators(makeNameValidator(this.transactionalEmailApi, transactionalEmailType.name));
            this.form.setValue({
              name: transactionalEmailType.name,
              code: transactionalEmailType.code || null,
              emailFrom: this.mailServer?.customServer
                ? transactionalEmailType.emailFrom
                : environment.config.smtpFromEmail,
              emailFromName: transactionalEmailType.emailFromName,
              emailSubject: transactionalEmailType.emailSubject,
              emailContent: transactionalEmailType.emailContent,
              emailMjmlContent: transactionalEmailType.emailMjmlContent,
              hasAttachment: transactionalEmailType.hasAttachment,
              passwordProtected: transactionalEmailType.passwordProtected,
              archive: transactionalEmailType.archive,
              autoPurge: transactionalEmailType.autoPurge,
              autoPurgeByDays: transactionalEmailType.autoPurgeByDays,
              csvSeparator: transactionalEmailType.csvSeparator,
              indexFields: [],
              whatsAppTemplateName: transactionalEmailType.whatsAppTemplateName || null,
              whatsAppTemplateParams: [],
            });

            this.mjml = transactionalEmailType.emailMjmlContent;
            this.form.get('emailMjmlContent')?.setValue(this.mjml);

            this.toggleAutoPurge(transactionalEmailType.autoPurge);

            transactionalEmailType.indexFields.map((indexField) => {
              this.doAddIndexField(indexField, indexField.seqOrder <= 3);
            });

            this.processWhatsappTemplate(transactionalEmailType);

            this.toggleAttachment(transactionalEmailType.hasAttachment);
            this.togglePasswordProtect(transactionalEmailType.passwordProtected);
          }
        })
      ),
      this.whatsAppTemplates$.pipe(
        tap((whatsAppTemplates) => {
          this.whatsAppTemplates = whatsAppTemplates;
          this.whatsAppTemplateOptions = whatsAppTemplates.filter(item => item.status === WhatsappTemplateStatus.APPROVED)

          if (this.transactionalEmailType && this.whatsAppTemplateParams.controls.length === 0) {
            this.processWhatsappTemplate(this.transactionalEmailType);
          }
        })
      ),
      this.form.get('whatsAppTemplateName')!.valueChanges.pipe(
        tap((whatsAppTemplateName) => {
          const mobileNumberIndexField = this.indexFields.at(3);

          if (this.isInitialize) {
            if (whatsAppTemplateName) {
              this.whatsAppTemplateName = this.whatsAppTemplates.find((template) => template.name === whatsAppTemplateName);
              if (this.whatsAppTemplateName) {
                if (mobileNumberIndexField) {
                  mobileNumberIndexField.get('applicable')?.setValue(true);
                  mobileNumberIndexField.get('required')?.setValue(false);
                  mobileNumberIndexField.get('systemDefined')?.setValue(true);
                  mobileNumberIndexField.get('show')?.setValue(true);
                }

                doDeleteAllIndex(this.whatsAppTemplateParams);
                const matches = getWhatsAppTemplateBodyParams(this.whatsAppTemplateName!);
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

            this.whatsAppTemplateName = undefined;
            doDeleteAllIndex(this.whatsAppTemplateParams);
            this.whatsAppTemplateParamTableData = [];
            if (mobileNumberIndexField) {
              mobileNumberIndexField.get('applicable')?.setValue(false);
              mobileNumberIndexField.get('required')?.setValue(false);
              mobileNumberIndexField.get('systemDefined')?.setValue(false);
              mobileNumberIndexField.get('show')?.setValue(false);
            }

            this.cd.markForCheck();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetTransactionalEmailType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'transactional-email', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewTransactionalEmailType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Transactional email created`));
            const transactionalEmail = this.store.selectSnapshot(TransactionalEmailState.transactionalEmailType)!;
            createTransactionalEmail(this.store, transactionalEmail, transactionalEmail.name);
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateTransactionalEmailType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Transactional email updated`));
            const transactionalEmail = this.store.selectSnapshot(TransactionalEmailState.transactionalEmailType);
            this.navigate(['/', 'transactional-email', 'detail', transactionalEmail!.id], { tab: 'settings' });
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      handleNewTransactionEmail(this.actions$, this.store, this.navigate.bind(this))
    );
  }

  private processWhatsappTemplate(transactionalEmailType: TransactionalEmailTypeModel) {
    if (transactionalEmailType.whatsAppTemplateName) {
      if (this.whatsAppTemplates) {
        const mobileNumberIndexField = this.indexFields.at(3);
        this.whatsAppTemplateName = this.whatsAppTemplates.find(
          (template) => template.name === transactionalEmailType.whatsAppTemplateName
        );
        if (this.whatsAppTemplateName) {
          if (mobileNumberIndexField) {
            mobileNumberIndexField.get('applicable')?.setValue(true);
            mobileNumberIndexField.get('required')?.setValue(false);
            mobileNumberIndexField.get('systemDefined')?.setValue(true);
          }

          transactionalEmailType.whatsAppTemplateParams.map((param) => {
            this.doAddWhatsappTemplateBodyParam(param.index, param);
          });

          if (transactionalEmailType.whatsAppTemplateParams.length > 0) {
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
    if (value.whatsAppTemplate) {
      if(!value.hasAttachment || !value.archive || value.autoPurge) {
        this.modal.error({
          nzTitle: 'Invalid Attachment Settings',
          nzContent: 'Attachment must be archive and not auto purge.'
        });

        return;
      }
    }

    this.isLoading = true;
    this.cd.markForCheck();

    if (this.form.valid) {

      const mismatchMergeFields = this.getMismatchMergeFields();
      if (mismatchMergeFields.length > 0) {
        this.displayMismatchFieldErrorModal(mismatchMergeFields);
        this.isLoading = false;
        return;
      }

      if (this.isNew) {
        this.store.dispatch(
          new NewTransactionalEmailType({
            name: value.name,
            code: value.code,
            autoPurge: value.autoPurge,
            autoPurgeByDays: value.autoPurgeByDays,
            csvSeparator: value.csvSeparator,
            indexFields: value.indexFields,

            sendSms: false,

            emailFrom: value.emailFrom,
            emailFromName: value.emailFromName,
            emailSubject: value.emailSubject,
            emailContent: value.emailContent,
            emailMjmlContent: value.emailMjmlContent,
            hasAttachment: value.hasAttachment,
            passwordProtected: value.passwordProtected,
            archive: value.archive,

            whatsAppTemplateName: value.whatsAppTemplateName,
            whatsAppTemplateParams: value.whatsAppTemplateParams,
          })
        );
      } else {
        this.store.dispatch(
          new UpdateTransactionalEmailType(this.typeId!, {
            name: value.name,
            code: value.code,
            autoPurge: value.autoPurge,
            autoPurgeByDays: value.autoPurgeByDays,
            csvSeparator: value.csvSeparator,
            indexFields: value.indexFields,

            sendSms: false,

            emailFrom: value.emailFrom,
            emailFromName: value.emailFromName,
            emailSubject: value.emailSubject,
            emailContent: value.emailContent,
            emailMjmlContent: value.emailMjmlContent,
            hasAttachment: value.hasAttachment,
            passwordProtected: value.passwordProtected,
            archive: value.archive,

            whatsAppTemplateName: value.whatsAppTemplateName,
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
    const archive = this.form.get('archive');
    const attachmentFieldShow = this.indexFields.at(1)?.get('show');
    const attachmentFieldApplicable = this.indexFields.at(1)?.get('applicable');

    if (value) {
      passwordProtected?.enable();
      archive?.enable();
      attachmentFieldShow?.setValue(true);
      attachmentFieldApplicable?.setValue(true);

      this.cd.markForCheck();
    } else {
      passwordProtected?.disable();
      passwordProtected?.setValue(false);
      this.togglePasswordProtect(false);

      archive?.disable();
      archive?.setValue(false);
      this.toggleArchive(false);
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

  toggleArchive(value: boolean): void {
    const isAutoPurge = this.form.get('autoPurge');

    if (value) {
      isAutoPurge?.enable();
      this.cd.markForCheck();
    } else {
      isAutoPurge?.disable();
      isAutoPurge?.setValue(false);
      this.toggleAutoPurge(false);

      this.cd.markForCheck();
    }
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

  getEditor() {
    return (this.editor as any)?.grapesjsEditor as Editor;
  }

  doEditEmailTemplate(): void {
    const grapejsEditor = this.getEditor();
    if (grapejsEditor) {
      grapejsEditor.setComponents(this.mjml, {});
      grapejsEditor.runCommand('core:open-blocks');
    }
    this.isEditorVisible = true;
  }

  closeEditor() {
    createCloseEditorConfirmationModal(
      this.modal,
      () => {
        this.doUpdateTemplate();
      },
      () => {
        this.isEditorVisible = false;
        this.cd.markForCheck();
      }
    );
  }

  doUpdateTemplate() {
    this.mjml = getUpdatedMjml(this.getEditor().getHtml());
    const mismatchMergeFields = this.getMismatchMergeFields();
    if (mismatchMergeFields.length > 0) {
      this.displayMismatchFieldErrorModal(mismatchMergeFields);
      return;
    }

    this.form.get('emailMjmlContent')?.setValue(this.mjml);

    this.html = getUpdatedMjmlHtml(this.getEditor().runCommand('mjml-code-to-html', {}).html);
    this.form.get('emailContent')?.setValue(this.html);

    this.isEditorVisible = false;
    editorOutOfFocus();
    this.cd.markForCheck();
  }

  getIndexFields() {
    return this.form.get('indexFields')!.value;
  }

  updateHtml(html: string) {
    this.html = getUpdatedMjmlHtml(html);
    this.cd.markForCheck();
    this.form.get('emailContent')?.setValue(this.html);
  }

  getMismatchMergeFields(): string[] {
    const fields = this.getIndexFields();
    const availableFields = fields.map((field: any) => field.header);

    const mismatchMergeFields: string[] = [];

    const mergeFieldMatch = this.mjml.matchAll(/{{[\w _-]+}}/g);
    for (const match of mergeFieldMatch) {
      const field = match[0].replace('{{', '').replace('}}', '');
      if (field !== 'unsubscribe_link' && !availableFields.includes(field)) {
        mismatchMergeFields.push(field);
      }
    }

    return mismatchMergeFields;
  }

  doLabelChange(control: AbstractControl) {
    const header = control.get('header')?.value;
    if (!header || header === '') {
      const label = control.get('label')?.value;
      control.get('header')?.setValue(camelCase(label));
    }
  }

  private displayMismatchFieldErrorModal(mismatchMergeFields: string[]) {
    this.modal.error({
      nzTitle: 'Mismatch field found',
      nzContent: `Mismatch field [${mismatchMergeFields.join(', ')}] found in email template`,
    });
  }

  getWhatsappTemplateHeader() {
    return this.whatsAppTemplateName ? getWhatsAppTemplateHeader(this.whatsAppTemplateName) : '-';
  }

  getWhatsappTemplateBody() {
    return this.whatsAppTemplateName ? getWhatsAppTemplateBody(this.whatsAppTemplateName) : '-';
  }

  getWhatsappTemplateFooter() {
    return this.whatsAppTemplateName ? getWhatsAppTemplateFooter(this.whatsAppTemplateName) : '-';
  }

  getWhatsappTemplateButton() {
    return this.whatsAppTemplateName ? getWhatsAppTemplateButton(this.whatsAppTemplateName) : '-';
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
