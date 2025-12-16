import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, ViewChild } from '@angular/core';
import {
  AbstractControl,
  AsyncValidatorFn,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { finalize, map, Observable, of, switchMap, tap } from 'rxjs';
import { EmailCampaignApi } from '../../../../api/email-campaign.api';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { MailServerState } from '../../../../states/mail-server/mail-server.state';
import {
  BaseIndexFieldModel,
  ContactFieldModel,
  EmailCampaignTypeModel,
  getErrorMessage,
  MailServerModel,
  makePageable,
  resolveErrorMessage,
  updateAndMarkControlAsDirty,
} from '@grabbill/lib';
import { EmailEditorComponent } from '../../app-common/components/email-editor/email-editor.component';
import { defaultMjmlTemplate } from '../../../../utils/default-mjml-template';
import { environment } from '../../../../environments/environment';
import { ActivatedRoute, Params } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import {
  GetEmailCampaignType,
  NewEmailCampaignType,
  ResetEmailCampaignType,
  UpdateEmailCampaignType,
} from '../../../../states/email-campaign/email-campaign.state-actions';
import { EmailCampaignState } from '../../../../states/email-campaign/email-campaign.state';
import { getUpdatedMjmlHtml } from '../../../../utils/get-updated-mjml-html';
import { getUpdatedMjml } from '../../../../utils/get-updated-mjml';
import { Editor } from 'grapesjs';
import { GetMailServer } from '../../../../states/mail-server/mail-server.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { GetContactFields, ResetContactFields } from '../../../../states/contact-field/contact-field.state-actions';
import { ContactFieldState } from '../../../../states/contact-field/contact-field.state';
import { ContactGroupApi } from '../../../../api/contact-group.api';
import { observeTypeFormMailServerSetting } from '../../../../utils/observe-form-mail-server-setting';
import { observeContactFields } from '../../../../utils/observe-contact-fields';
import { createEmailCampaign, handleNewEmailCampaign } from '../../../../utils/email-campaign';
import {
  createCloseEditorConfirmationModal,
  editorOutOfFocus,
} from '../../../../utils/create-close-editor-confirmation-modal';

export const makeNameValidator = (emailCampaignApi: EmailCampaignApi, originalName?: string): AsyncValidatorFn => {
  return (control: AbstractControl): Observable<ValidationErrors> => {
    const value = control.value;
    if (value === originalName) {
      return of({});
    }

    if (!/^[a-z0-9 ]+$/i.test(value)) {
      return of({ alphanumericWithSpaceOnly: value });
    }

    return emailCampaignApi
      .validateTypeName(value)
      .pipe(map((result) => (result.data.exist ? { nameExist: true } : {})));
  };
};

@Component({
  selector: 'grabbill-client-email-campaign-edit-detail',
  templateUrl: './email-campaign-edit-detail.component.html',
  styleUrls: ['./email-campaign-edit-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmailCampaignEditDetailComponent extends NgxsBaseComponent {
  @Select(EmailCampaignState.emailCampaignType)
  emailCampaignType$!: Observable<EmailCampaignTypeModel>;

  @Select(MailServerState.mailServer)
  mailServer$!: Observable<MailServerModel>;

  @Select(ContactFieldState.contactFields)
  contactFields$!: Observable<ContactFieldModel[]>;

  isContactGroupLoading = false;

  form: UntypedFormGroup;
  isNew = true;
  isLoading = false;
  isInitialize = true;
  typeId?: number;

  nzFilterOption = (): boolean => true;
  listOfOption: Array<{ value: string; text: string }> = [];

  isEditorVisible = false;
  isEditorLoading = false;

  contactFields: BaseIndexFieldModel[] = [];

  mailServer?: MailServerModel;

  @ViewChild('editor') editor!: ElementRef<EmailEditorComponent>;

  mjml: string = defaultMjmlTemplate(environment.config.appUrl);
  html: string = '';

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    private emailCampaignApi: EmailCampaignApi,
    private contactGroupApi: ContactGroupApi,
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

      contactGroup: ['all', [Validators.required]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.store.dispatch(new GetMailServer());
    this.store.dispatch(new ResetEmailCampaignType());
    this.store.dispatch(new ResetContactFields());
    this.store.dispatch(new GetContactFields());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          if (this.typeId) {
            this.isInitialize = false;
            this.cd.markForCheck();
            this.store.dispatch(new GetEmailCampaignType(this.typeId));
            this.isNew = false;
            this.cd.markForCheck();
          } else {
            this.isNew = true;
            this.form.get('name')!.addAsyncValidators(makeNameValidator(this.emailCampaignApi));
            this.form.get('emailMjmlContent')?.setValue(this.mjml);
            this.search('');
          }
        })
      ),
      observeContactFields(this.contactFields$).pipe(tap((fields) => (this.contactFields = fields))),
      observeTypeFormMailServerSetting(this.mailServer$, this.form, this.isNew, this.mailServer),
      this.emailCampaignType$.pipe(
        tap((emailCampaignType) => {
          if (!this.isNew && emailCampaignType) {
            this.isInitialize = true;
            this.cd.markForCheck();
            this.form.get('name')!.addAsyncValidators(makeNameValidator(this.emailCampaignApi, emailCampaignType.name));
            this.form.setValue({
              name: emailCampaignType.name,
              code: emailCampaignType.code || null,
              emailFrom: this.mailServer?.customServer ? emailCampaignType.emailFrom : environment.config.smtpFromEmail,
              emailFromName: emailCampaignType.emailFromName,
              emailSubject: emailCampaignType.emailSubject,
              emailContent: emailCampaignType.emailContent,
              emailMjmlContent: emailCampaignType.emailMjmlContent,
              contactGroup: emailCampaignType.contactGroupId ? emailCampaignType.contactGroupId.toString() : 'all',
            });

            this.mjml = emailCampaignType.emailMjmlContent;
            this.form.get('emailMjmlContent')?.setValue(this.mjml);
            this.search(emailCampaignType.contactGroupName ? emailCampaignType.contactGroupName : '');
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetEmailCampaignType),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'email-campaign', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewEmailCampaignType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Email campaign created`));
            const emailCampaign = this.store.selectSnapshot(EmailCampaignState.emailCampaignType)!;
            createEmailCampaign(this.store, emailCampaign, emailCampaign.name);
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateEmailCampaignType),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Email campaign updated`));
            const emailCampaign = this.store.selectSnapshot(EmailCampaignState.emailCampaignType);
            this.navigate(['/', 'email-campaign', 'detail', emailCampaign!.id], { tab: 'settings' });
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      handleNewEmailCampaign(this.actions$, this.store, this.navigate.bind(this))
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
      const contactGroup = value.contactGroup === 'all' ? undefined : value.contactGroup * 1;

      const mismatchMergeFields = this.getMismatchMergeFields();
      if (mismatchMergeFields.length > 0) {
        this.displayMismatchFieldErrorModal(mismatchMergeFields);
        return;
      }

      if (this.isNew) {
        this.store.dispatch(
          new NewEmailCampaignType({
            name: value.name,
            code: value.code,

            emailFrom: value.emailFrom,
            emailFromName: value.emailFromName,
            emailSubject: value.emailSubject,
            emailContent: value.emailContent,
            emailMjmlContent: value.emailMjmlContent,
            contactGroupId: contactGroup,

            hasAttachment: true,
            autoPurge: false,
            autoPurgeByDays: 0,
          })
        );
      } else {
        this.store.dispatch(
          new UpdateEmailCampaignType(this.typeId!, {
            name: value.name,
            code: value.code,

            emailFrom: value.emailFrom,
            emailFromName: value.emailFromName,
            emailSubject: value.emailSubject,
            emailContent: value.emailContent,
            emailMjmlContent: value.emailMjmlContent,
            contactGroupId: contactGroup,

            hasAttachment: true,
            autoPurge: false,
            autoPurgeByDays: 0,
          })
        );
      }
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  getIndexFields() {
    return this.contactFields;
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

  private displayMismatchFieldErrorModal(mismatchMergeFields: string[]) {
    this.modal.error({
      nzTitle: 'Mismatch field found',
      nzContent: `Mismatch field [${mismatchMergeFields.join(', ')}] found in email template`,
    });
  }

  updateHtml(html: string) {
    this.html = getUpdatedMjmlHtml(html);
    this.cd.markForCheck();
    this.form.get('emailContent')?.setValue(this.html);
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

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
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
}
