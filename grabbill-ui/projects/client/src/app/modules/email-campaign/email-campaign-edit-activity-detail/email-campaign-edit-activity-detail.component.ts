import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  ViewChild,
} from '@angular/core';
import {
  AbstractControl,
  AsyncValidatorFn,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { finalize, map, Observable, of, Subject, switchMap, tap } from 'rxjs';
import { EmailCampaignApi } from '../../../../api/email-campaign.api';
import {
  BaseFileModel,
  BaseIndexFieldModel,
  ContactFieldModel,
  DashboardStatisticsModel,
  EmailCampaignActivityModel,
  EmailCampaignTypeModel,
  getErrorMessage,
  MailServerModel,
  makePageable,
  ProcessStatus,
  resolveErrorMessage,
  updateAndMarkControlAsDirty,
  UserAuthorityModel,
} from '@grabbill/lib';
import { EmailEditorComponent } from '../../app-common/components/email-editor/email-editor.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { AuthState } from '../../../../states/auth/auth.state';
import { DashboardState } from '../../../../states/dashboard/dashboard.state';
import { MailServerState } from '../../../../states/mail-server/mail-server.state';
import { EmailCampaignState } from '../../../../states/email-campaign/email-campaign.state';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { GetCurrentStatistics } from '../../../../states/dashboard/dashboard.state-actions';
import { GetMailServer } from '../../../../states/mail-server/mail-server.state-actions';
import {
  DeleteEmailCampaignFile,
  DeleteEmailCampaignFiles,
  GetEmailCampaignActivity,
  GetEmailCampaignType,
  ResetEmailCampaignActivity,
  UpdateEmailCampaignActivity,
  UploadEmailCampaignFile,
} from '../../../../states/email-campaign/email-campaign.state-actions';
import { environment } from '../../../../environments/environment';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { isElementBottomVisible, isElementTopVisible } from '../../../../utils/is-in-viewport';
import { scrollToTargetAdjusted } from '../../../../utils/scroll-to-target-adjusted';
import { NzUploadFile } from 'ng-zorro-antd/upload';
import prettyBytes from 'pretty-bytes';
import { Editor } from "grapesjs";
import { getUpdatedMjml } from '../../../../utils/get-updated-mjml';
import { getUpdatedMjmlHtml } from '../../../../utils/get-updated-mjml-html';
import { GetContactFields, ResetContactFields } from '../../../../states/contact-field/contact-field.state-actions';
import { ContactGroupApi } from '../../../../api/contact-group.api';
import { ContactFieldState } from '../../../../states/contact-field/contact-field.state';
import { observeActivityFormMailServerSetting } from '../../../../utils/observe-form-mail-server-setting';
import { observeContactFields } from '../../../../utils/observe-contact-fields';
import { concatMap } from 'rxjs/operators';
import {
  createCloseEditorConfirmationModal,
  editorOutOfFocus
} from "../../../../utils/create-close-editor-confirmation-modal";

export const makeNameValidator = (
  emailCampaignApi: EmailCampaignApi,
  typeId: number,
  originalName: string
): AsyncValidatorFn => {
  return (control: AbstractControl): Observable<ValidationErrors> => {
    const value = control.value;
    if (value === originalName) {
      return of({});
    }

    if (!(/^[a-z0-9-: ]+$/i.test(value))) {
      return of({ alphanumericWithSpaceDashColonOnly: value });
    }

    return emailCampaignApi
      .validateActivityName(typeId, value)
      .pipe(map((result) => (result.data.exist ? { nameExist: true } : {})));
  };
};

@Component({
  selector: 'grabbill-client-email-campaign-edit-activity-detail',
  templateUrl: './email-campaign-edit-activity-detail.component.html',
  styleUrls: ['./email-campaign-edit-activity-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmailCampaignEditActivityDetailComponent extends NgxsBaseComponent {
  form: UntypedFormGroup;
  typeId?: number;
  activityId?: number;

  isLoading = true;
  canUploadAttachment = true;

  searchAttachmentFilename?: string;
  attachmentPageIndex = 1;
  attachmentPageSize = 5;

  isEditorVisible = false;
  isEditorLoading = false;
  mjml: string = '';
  html: string = '';

  mailServer?: MailServerModel;
  user?: UserAuthorityModel;
  currentStatistics?: DashboardStatisticsModel;

  isScheduleModalVisible = false;
  scheduleForm: UntypedFormGroup;

  index = 1;
  lastScrollTop = 0;
  isScrolling = false;

  contactFields: BaseIndexFieldModel[] = [];

  nzFilterOption = (): boolean => true;
  listOfOption: Array<{ value: string; text: string }> = [];

  isContactGroupLoading = false;

  @ViewChild('editor') editor!: ElementRef<EmailEditorComponent>;
  @ViewChild('step2') step2!: ElementRef<HTMLDivElement>;
  @ViewChild('step3') step3!: ElementRef<HTMLDivElement>;

  @Select(DashboardState.currentStatistics)
  currentStatistics$!: Observable<DashboardStatisticsModel>;

  @Select(MailServerState.mailServer)
  mailServer$!: Observable<MailServerModel>;

  @Select(EmailCampaignState.emailCampaignType)
  emailCampaignType$!: Observable<EmailCampaignTypeModel>;

  @Select(EmailCampaignState.emailCampaignActivity)
  emailCampaignActivity$!: Observable<EmailCampaignActivityModel>;

  @Select(EmailCampaignState.emailCampaignActivityFiles)
  emailCampaignActivityFiles$!: Observable<BaseFileModel[]>;

  @Select(ContactFieldState.contactFields)
  contactFields$!: Observable<ContactFieldModel[]>;

  uploadAttachmentQueue$ = new Subject<FormData>();

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router,
    private actions$: Actions,
    private modal: NzModalService,
    private emailCampaignApi: EmailCampaignApi,
    private contactGroupApi: ContactGroupApi,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      emailFrom: ['', [Validators.email, Validators.required, Validators.maxLength(255)]],
      emailFromName: ['', [Validators.required, Validators.maxLength(255)]],
      emailSubject: ['', [Validators.required, Validators.maxLength(255)]],
      emailContent: ['', [Validators.required]],
      emailMjmlContent: ['', [Validators.required]],

      contactGroup: ['all', [Validators.required]],
    });

    this.scheduleForm = this.fb.group({
      scheduledTimestamp: ['', [Validators.required]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetEmailCampaignActivity());
    this.store.dispatch(new ResetContactFields());
    this.store.dispatch(new GetCurrentStatistics());
    this.store.dispatch(new GetMailServer());
    this.store.dispatch(new GetContactFields());
    this.user = this.store.selectSnapshot(AuthState.user);

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.store.dispatch(new GetEmailCampaignType(this.typeId!));
          this.store.dispatch(new GetEmailCampaignActivity(this.typeId!, this.activityId!));
        })
      ),
      this.currentStatistics$.pipe(
        tap((currentStatistics) => {
          if (currentStatistics) {
            this.currentStatistics = currentStatistics;
          }
        })
      ),
      observeContactFields(this.contactFields$).pipe(tap((fields) => (this.contactFields = fields))),
      observeActivityFormMailServerSetting(this.mailServer$, this.form, this.mailServer),
      this.emailCampaignActivity$.pipe(
        tap((activity) => {
          if (activity) {
            this.form
              .get('name')!
              .addAsyncValidators(makeNameValidator(this.emailCampaignApi, Number(this.typeId), activity.name));
            this.form.setValue({
              name: activity.name,
              emailFrom: this.mailServer?.customServer ? activity.emailFrom : environment.config.smtpFromEmail,
              emailFromName: activity.emailFromName,
              emailSubject: activity.emailSubject,
              emailContent: activity.emailContent,
              emailMjmlContent: activity.emailMjmlContent,
              contactGroup: activity.contactGroupId ? activity.contactGroupId.toString() : 'all',
            });
            this.mjml = activity.emailMjmlContent;
            this.form.get('emailMjmlContent')?.setValue(this.mjml);
            this.search(activity.contactGroupName ? activity.contactGroupName : '');
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
        ofActionCompleted(GetEmailCampaignActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'email-campaign', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateEmailCampaignActivity),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          const status = data.action.request.status;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity updated`));
            if (status === ProcessStatus.DRAFT) {
              this.navigate(['/', 'email-campaign', 'detail', this.typeId!], { tab: 'activities' });
            } else {
              this.navigate(['/', 'email-campaign', 'detail', this.typeId!, 'activity', this.activityId!], {
                tab: 'info',
              });
            }
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UploadEmailCampaignFile),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          this.canUploadAttachment = true;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Attachment updated`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteEmailCampaignFile),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Attachment deleted`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteEmailCampaignFiles),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Attachments deleted`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.uploadAttachmentQueue$.pipe(
        concatMap((formData) => {
          return this.store.dispatch(new UploadEmailCampaignFile(this.typeId!, this.activityId!, formData));
        })
      )
    );
  }

  @HostListener('window:scroll', ['$event'])
  onScroll() {
    if (!this.isScrolling) {
      const scrollTop = document.documentElement.scrollTop;
      if (scrollTop > this.lastScrollTop) {
        // down scroll code
        if (isElementTopVisible(this.step3.nativeElement)) {
          this.index = 2;
        } else if (isElementTopVisible(this.step2.nativeElement)) {
          this.index = 1;
        }
      } else if (scrollTop < this.lastScrollTop) {
        // up scroll code
        if (isElementBottomVisible(this.step2.nativeElement)) {
          this.index = 1;
        } else if (isElementBottomVisible(this.step3.nativeElement)) {
          this.index = 2;
        }
      }
      this.lastScrollTop = scrollTop <= 0 ? 0 : scrollTop;
    }
  }

  doStepChange(index: number): void {
    this.isScrolling = true;
    this.index = index;

    if (index === 0) {
      this.navigate(['/', 'email-campaign', 'detail', this.typeId, 'edit']);
    } else if (index === 1) {
      scrollToTargetAdjusted(this.step2.nativeElement);
    } else if (index === 2) {
      scrollToTargetAdjusted(this.step3.nativeElement);
    }
    this.isScrolling = false;
  }

  doSubmitForm(
    isDraft: boolean,
    type: EmailCampaignTypeModel,
    files: BaseFileModel[],
    scheduledTimestamp?: Date
  ): void {
    if (this.form.valid) {
      if (!isDraft) {
        const mismatchMergeFields = this.getMismatchMergeFields();
        if (mismatchMergeFields.length > 0) {
          this.displayMismatchFieldErrorModal(mismatchMergeFields);
          return;
        }
      }

      const value = this.form.getRawValue();
      value.scheduledTimestamp = scheduledTimestamp;

      if (!isDraft) {
        this.modal.confirm({
          nzTitle: scheduledTimestamp ? 'Schedule email campaign' : 'Send email campaign',
          nzOkText: 'Yes',
          nzOkType: 'primary',
          nzOkDanger: true,
          nzOnOk: () => {
            this.dispatchUpdateEmailCampaignActivity(value, isDraft);
          },
          nzCancelText: 'No',
        });
      } else {
        this.dispatchUpdateEmailCampaignActivity(value, isDraft);
      }
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  private dispatchUpdateEmailCampaignActivity(value: any, isDraft: boolean) {
    const contactGroup = value.contactGroup === 'all' ? undefined : value.contactGroup * 1;

    this.store.dispatch(
      new UpdateEmailCampaignActivity(this.typeId!, this.activityId!, {
        name: value.name,
        emailFrom: value.emailFrom,
        emailFromName: value.emailFromName,
        emailSubject: value.emailSubject,
        emailContent: value.emailContent,
        emailMjmlContent: value.emailMjmlContent,
        scheduledTimestamp: value.scheduledTimestamp,
        contactGroupId: contactGroup,
        status: isDraft ? ProcessStatus.DRAFT : ProcessStatus.SUBMITTED,
      })
    );
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doSearchAttachment(event: any) {
    this.searchAttachmentFilename = event.target.value;
  }

  doPageAttachments(attachments: BaseFileModel[]): BaseFileModel[] {
    const startIndex = this.attachmentPageSize * (this.attachmentPageIndex - 1);
    const endIndex = this.attachmentPageSize * this.attachmentPageIndex;
    return this.getFilterAttachments(attachments).slice(startIndex, endIndex);
  }

  doAttachmentPageIndexChange(pageIndex: number) {
    this.attachmentPageIndex = pageIndex;
  }

  getFilterAttachments(attachments: BaseFileModel[]): BaseFileModel[] {
    return attachments.filter((file) =>
      this.searchAttachmentFilename ? file.name.includes(this.searchAttachmentFilename) : true
    );
  }

  beforeUploadAttachment = (file: NzUploadFile): boolean => {
    this.canUploadAttachment = false;
    const isPdf = file.type === 'application/pdf';
    if (!isPdf) {
      this.canUploadAttachment = true;
      this.store.dispatch(new ShowMessage('error', 'Only PDF is allowed'));
    }

    if (file.size! > this.user!.subscription!.maxAttachmentSize) {
      this.modal.error({
        nzTitle: 'Invalid file size',
        nzContent: `Maximum PDF size is ${prettyBytes(this.user!.subscription!.maxAttachmentSize)}`,
      });
      this.canUploadAttachment = true;
      return false;
    }

    this.cd.markForCheck();

    if (file instanceof File) {
      const formData = new FormData();
      formData.append('file', file);
      this.uploadAttachmentQueue$.next(formData);
    }

    this.cd.markForCheck();
    return false;
  };

  doDeleteAttachment(file: BaseFileModel) {
    this.modal.confirm({
      nzTitle: `Delete ${file.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeleteEmailCampaignFile(this.typeId!, this.activityId!, file.id));
      },
      nzCancelText: 'No',
    });
  }

  doOpenScheduleModal() {
    this.isScheduleModalVisible = true;
  }

  doCloseScheduleModal() {
    this.isScheduleModalVisible = false;
  }

  doSchedule(type: EmailCampaignTypeModel, files: BaseFileModel[]) {
    if (this.scheduleForm.valid) {
      this.isScheduleModalVisible = false;
      this.doSubmitForm(false, type, files, this.scheduleForm.getRawValue().scheduledTimestamp);
    } else {
      updateAndMarkControlAsDirty(this.scheduleForm);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  disabledDate = (value: Date): boolean => {
    const date = new Date();
    date.setHours(0, 0, 0, 0);
    return value.getTime() < date.getTime();
  };

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
    this.store.dispatch(new SetPageLoading(true));
    this.mjml = getUpdatedMjml(this.getEditor().getHtml());
    const mismatchMergeFields = this.getMismatchMergeFields();
    if (mismatchMergeFields.length > 0) {
      this.displayMismatchFieldErrorModal(mismatchMergeFields);
      this.store.dispatch(new SetPageLoading(false));
      return;
    }

    this.form.get('emailMjmlContent')?.setValue(this.mjml);

    this.html = getUpdatedMjmlHtml(this.getEditor().runCommand('mjml-code-to-html', {}).html);
    this.form.get('emailContent')?.setValue(this.html);

    this.isEditorVisible = false;
    editorOutOfFocus();
    this.cd.markForCheck();
    this.store.dispatch(new SetPageLoading(false));
  }

  private displayMismatchFieldErrorModal(mismatchMergeFields: string[]) {
    this.modal.error({
      nzTitle: 'Mismatch field found',
      nzContent: `Mismatch field [${mismatchMergeFields.join(', ')}] found in email template`,
    });
  }

  updateHtml(html: string) {
    this.html = getUpdatedMjmlHtml(html);
    this.form.get('emailContent')?.setValue(this.html);
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

  getIndexFields() {
    return this.contactFields;
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
