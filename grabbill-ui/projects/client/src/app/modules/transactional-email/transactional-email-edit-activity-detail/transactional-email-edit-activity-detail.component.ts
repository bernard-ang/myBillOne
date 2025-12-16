import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  TrackByFunction,
  ViewChild,
} from '@angular/core';
import {
  AbstractControl,
  AsyncValidatorFn,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormControl,
  UntypedFormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzUploadFile } from 'ng-zorro-antd/upload';
import { map, Observable, of, Subject, switchMap, tap } from 'rxjs';
import { catchError, concatMap } from 'rxjs/operators';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { Editor } from "grapesjs";
import {
  BaseFileModel,
  BaseIndexFieldModel,
  BaseIndexRowModel,
  DashboardStatisticsModel,
  DataType,
  DomainType,
  getErrorMessage,
  MailServerModel,
  ProcessStatus,
  resolveErrorMessage,
  TransactionalEmailActivityModel,
  TransactionalEmailTypeModel,
  updateAndMarkControlAsDirty,
  UserAuthorityModel,
  WhatsappTemplateModel,
  WhatsappTemplateStatus,
} from '@grabbill/lib';
import { EmailEditorComponent } from '../../app-common/components/email-editor/email-editor.component';
import { environment } from '../../../../environments/environment';
import { TransactionalEmailApi } from '../../../../api/transactional-email.api';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { MailServerState } from '../../../../states/mail-server/mail-server.state';
import { GetMailServer } from '../../../../states/mail-server/mail-server.state-actions';
import {
  DeleteTransactionalEmailFile,
  DeleteTransactionalEmailFiles,
  DownloadIndexData,
  GetTransactionalEmailActivity,
  GetTransactionalEmailType,
  ResetTransactionalEmailActivity,
  UpdateTransactionalEmailActivity,
  UploadTransactionalEmailFile,
} from '../../../../states/transactional-email/transactional-email.state-actions';
import { TransactionalEmailState } from '../../../../states/transactional-email/transactional-email.state';
import { getUploadCsvData } from '../../../../utils/get-upload-csv-data';
import { getUploadExcelData } from '../../../../utils/get-upload-excel-data';
import { getUpdatedMjmlHtml } from '../../../../utils/get-updated-mjml-html';
import { isElementBottomVisible, isElementTopVisible } from '../../../../utils/is-in-viewport';
import { doDeleteAllIndex } from '../../../../utils/manage-form-array';
import { scrollToTargetAdjusted } from '../../../../utils/scroll-to-target-adjusted';
import { IndexRowError, validateIndexRows } from '../../../../utils/validate-index-rows';
import { getUpdatedMjml } from '../../../../utils/get-updated-mjml';
import { AuthState } from '../../../../states/auth/auth.state';
import prettyBytes from 'pretty-bytes';
import { DashboardState } from '../../../../states/dashboard/dashboard.state';
import { GetCurrentStatistics } from '../../../../states/dashboard/dashboard.state-actions';
import { pdfFilenameValidator } from '../../../../utils/filename-validator';
import { uniqBy } from 'lodash';
import {
  createCloseEditorConfirmationModal,
  editorOutOfFocus,
} from '../../../../utils/create-close-editor-confirmation-modal';
import {
  createWorkbook,
  downloadWorkbook,
  ExcelColumn,
  generateIndexRowArray,
  getSampleIndexData,
} from '../../../../utils/download-index-row';
import { WhatsAppState } from '../../../../states/whatsapp/whatsapp.state';
import { GetWhatsAppTemplates } from '../../../../states/whatsapp/whatsapp.state-actions';
import {
  getWhatsAppTemplateBody,
  getWhatsAppTemplateButton,
  getWhatsAppTemplateFooter,
  getWhatsAppTemplateHeader,
} from 'projects/client/src/utils/whatsapp-template';
import { getIndexFieldName } from '../../../../utils/get-index-field-name';

export const makeNameValidator = (
  transactionalEmailApi: TransactionalEmailApi,
  typeId: number,
  originalName: string
): AsyncValidatorFn => {
  return (control: AbstractControl): Observable<ValidationErrors> => {
    const value = control.value;
    if (value === originalName) {
      return of({});
    }

    if (!/^[a-z0-9-: ]+$/i.test(value)) {
      return of({ alphanumericWithSpaceDashColonOnly: value });
    }

    return transactionalEmailApi
      .validateActivityName(typeId, value)
      .pipe(map((result) => (result.data.exist ? { nameExist: true } : {})));
  };
};

interface AddFormControl {
  label: string;
  name: string;
  type: DataType;
  required: boolean;
}

@Component({
  selector: 'grabbill-client-transactional-email-edit-activity-detail',
  templateUrl: './transactional-email-edit-activity-detail.component.html',
  styleUrls: ['./transactional-email-edit-activity-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransactionalEmailEditActivityDetailComponent extends NgxsBaseComponent {
  form: UntypedFormGroup;
  typeId?: number;
  availableFields: string[] = [];
  activityId?: number;
  isLoading = true;
  canUploadAttachment = true;
  canUploadIndexRow = true;

  searchIndexRowFilename?: string;
  searchIndexRowName?: string;
  isIndexRowFormVisible = false;
  isIndexRowFormLoading = false;
  indexRowForm: UntypedFormGroup;
  addFormControls: AddFormControl[] = [];
  indexRows: BaseIndexRowModel[] = [];
  indexRowErrors: IndexRowError[] = [];
  isIndexRowErrorModalVisible = false;

  searchAttachmentFilename?: string;
  attachmentPageIndex = 1;
  attachmentPageSize = 5;

  showPassword = false;

  isScheduleModalVisible = false;
  scheduleForm: UntypedFormGroup;

  isEditorVisible = false;
  isEditorLoading = false;
  mjml: string = '';
  html: string = '';

  index = 1;
  lastScrollTop = 0;
  isScrolling = false;

  mailServer?: MailServerModel;
  user?: UserAuthorityModel;
  currentStatistics?: DashboardStatisticsModel;

  transactionalEmailType?: TransactionalEmailTypeModel;

  whatsappTemplates: WhatsappTemplateModel[] = [];
  whatsappTemplate?: WhatsappTemplateModel;

  @ViewChild('editor') editor!: ElementRef<EmailEditorComponent>;
  @ViewChild('step2') step2!: ElementRef<HTMLDivElement>;
  @ViewChild('step3') step3!: ElementRef<HTMLDivElement>;
  @ViewChild('step4') step4!: ElementRef<HTMLDivElement>;
  @ViewChild('step5') step5!: ElementRef<HTMLDivElement>;

  trackByFn: TrackByFunction<{
    label: string;
    idx: number;
    value: any;
    hiddenValue?: any;
  }> = (_, result) => `${result.label}-${result.idx}`;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  @Select(DashboardState.currentStatistics)
  currentStatistics$!: Observable<DashboardStatisticsModel>;

  @Select(MailServerState.mailServer)
  mailServer$!: Observable<MailServerModel>;

  @Select(TransactionalEmailState.transactionalEmailType)
  transactionalEmailType$!: Observable<TransactionalEmailTypeModel>;

  @Select(TransactionalEmailState.transactionalEmailActivity)
  transactionalEmailActivity$!: Observable<TransactionalEmailActivityModel>;

  @Select(TransactionalEmailState.transactionalEmailActivityFiles)
  transactionalEmailActivityFiles$!: Observable<BaseFileModel[]>;

  @Select(WhatsAppState.templates)
  whatsappTemplates$!: Observable<WhatsappTemplateModel[]>;

  uploadAttachmentQueue$ = new Subject<FormData>();

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router,
    private actions$: Actions,
    private modal: NzModalService,
    private transactionalEmailApi: TransactionalEmailApi,
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
      sendWhatsAppMessage: [false],
      indexRows: this.fb.array([]),
    });

    this.indexRowForm = this.fb.group({});

    this.scheduleForm = this.fb.group({
      scheduledTimestamp: ['', [Validators.required]],
    });
  }

  get formIndexRows() {
    return this.form.controls['indexRows'] as UntypedFormArray;
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.store.dispatch(new GetCurrentStatistics());
    this.store.dispatch(new GetMailServer());
    this.store.dispatch(new GetWhatsAppTemplates());
    this.store.dispatch(new ResetTransactionalEmailActivity());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.store.dispatch(new GetTransactionalEmailType(this.typeId!));
          this.store.dispatch(new GetTransactionalEmailActivity(this.typeId!, this.activityId!));
        })
      ),
      this.currentStatistics$.pipe(
        tap((currentStatistics) => {
          if (currentStatistics) {
            this.currentStatistics = currentStatistics;
          }
        })
      ),
      this.user$.pipe(
        tap((user) => {
          if (user) {
            this.user = user;
          }
        })
      ),
      this.mailServer$.pipe(
        tap((mailServer) => {
          if (mailServer) {
            this.mailServer = mailServer;
            if (!mailServer.customServer) {
              this.form.get('emailFrom')!.disable();
              this.form.get('emailFrom')!.setValue(environment.config.smtpFromEmail);
            }
          }
        })
      ),
      this.whatsappTemplates$.pipe(
        tap((whatsappTemplates) => {
          this.whatsappTemplates = whatsappTemplates;
          if (this.transactionalEmailType) {
            this.whatsappTemplate = whatsappTemplates.find(
              (item) => item.name === this.transactionalEmailType?.whatsAppTemplateName
            );
          }
        })
      ),
      this.transactionalEmailType$.pipe(
        tap((transactionalEmailType) => {
          if (transactionalEmailType) {
            this.transactionalEmailType = transactionalEmailType;
            this.availableFields = transactionalEmailType.indexFields
              .filter((value) => value.applicable)
              .map((value) => value.header);
            this.populateIndexRowFormFields(transactionalEmailType);

            if (this.whatsappTemplates.length > 0) {
              this.whatsappTemplate = this.whatsappTemplates.find(
                (item) => item.name === this.transactionalEmailType?.whatsAppTemplateName
              );
            }
          }
        })
      ),
      this.transactionalEmailActivity$.pipe(
        tap((transactionalEmailActivity) => {
          if (transactionalEmailActivity) {
            this.form
              .get('name')!
              .addAsyncValidators(
                makeNameValidator(this.transactionalEmailApi, Number(this.typeId), transactionalEmailActivity.name)
              );
            this.form.setValue({
              name: transactionalEmailActivity.name,
              emailFrom: this.mailServer?.customServer
                ? transactionalEmailActivity.emailFrom
                : environment.config.smtpFromEmail,
              emailFromName: transactionalEmailActivity.emailFromName,
              emailSubject: transactionalEmailActivity.emailSubject,
              emailContent: transactionalEmailActivity.emailContent,
              emailMjmlContent: transactionalEmailActivity.emailMjmlContent,
              indexRows: [],
              sendWhatsAppMessage: transactionalEmailActivity.sendWhatsAppMessage || false,
            });
            this.mjml = transactionalEmailActivity.emailMjmlContent;

            for (const row of transactionalEmailActivity.indexRows) {
              this.addIndexRowControl(row);
            }
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
        ofActionCompleted(GetTransactionalEmailActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'transactional-email', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateTransactionalEmailActivity),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          const status = data.action.request.status;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity updated`));
            if (status === ProcessStatus.DRAFT) {
              this.navigate(['/', 'transactional-email', 'detail', this.typeId!], { tab: 'activities' });
            } else {
              this.navigate(['/', 'transactional-email', 'detail', this.typeId!, 'activity', this.activityId!], {
                tab: 'info',
              });
            }
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UploadTransactionalEmailFile),
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
        ofActionCompleted(DeleteTransactionalEmailFile),
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
        ofActionCompleted(DeleteTransactionalEmailFiles),
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
      this.actions$.pipe(
        ofActionCompleted(DownloadIndexData),
        switchMap((data: ActionCompletion) => {
          let characterIndex = 'A'.charCodeAt(0) - 1;
          const excelColumns: ExcelColumn[] = data.action.type.indexFields
            .filter((field: BaseIndexFieldModel) => field.applicable)
            .map((field: BaseIndexFieldModel) => {
              characterIndex++;
              return {
                letter: String.fromCharCode(characterIndex),
                field: field,
              };
            });

          return of(data.action.indexRows).pipe(
            map(generateIndexRowArray(excelColumns, data.action.generateSampleData)),
            concatMap(createWorkbook(excelColumns)),
            tap(downloadWorkbook()),
            tap(this.store.dispatch(new SetPageLoading(false)))
          );
        })
      ),
      this.uploadAttachmentQueue$.pipe(
        concatMap((formData) => {
          return this.store.dispatch(new UploadTransactionalEmailFile(this.typeId!, this.activityId!, formData));
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
        if (isElementTopVisible(this.step4.nativeElement)) {
          this.index = 3;
        } else if (isElementTopVisible(this.step3.nativeElement)) {
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
        } else if (isElementBottomVisible(this.step4.nativeElement)) {
          this.index = 3;
        }
      }
      this.lastScrollTop = scrollTop <= 0 ? 0 : scrollTop;
    }
  }

  doStepChange(index: number): void {
    this.isScrolling = true;
    this.index = index;

    if (index === 0) {
      this.navigate(['/', 'transactional-email', 'detail', this.typeId, 'edit']);
    } else if (index === 1) {
      scrollToTargetAdjusted(this.step2.nativeElement);
    } else if (index === 2) {
      scrollToTargetAdjusted(this.step3.nativeElement);
    } else if (index === 3) {
      scrollToTargetAdjusted(this.step4.nativeElement);
    }
    this.isScrolling = false;
  }

  doSubmitForm(
    isDraft: boolean,
    type: TransactionalEmailTypeModel,
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

        if (this.formIndexRows.value.length === 0) {
          this.modal.error({
            nzTitle: 'Invalid Index Data',
            nzContent: 'Minimum 1 index row is required',
          });
          return;
        }

        if (type.hasAttachment) {
          if (files.length === 0) {
            this.modal.error({
              nzTitle: 'Invalid Upload',
              nzContent: 'Minimum 1 attachment is required',
            });
            return;
          }

          const missingIndexRows = this.getMissingIndexRows(files);
          if (missingIndexRows.length > 0) {
            this.modal.error({
              nzTitle: 'Missing Index Data',
              nzContent: `Index data is required for following data [${missingIndexRows.join(', ')}]`,
            });
            return;
          }
          const missingAttachments = this.getMissingAttachments(files);
          if (missingAttachments.length > 0) {
            this.modal.error({
              nzTitle: 'Missing Attachment',
              nzContent: `Attachments [${missingAttachments.join(', ')}] not found`,
            });
            return;
          }
        }
      }

      const value = this.form.getRawValue();

      value.scheduledTimestamp = scheduledTimestamp;

      const updatedIndexRows = [];
      const currentIndexRows = value.indexRows;
      for (let i = 0; i < currentIndexRows.length; i++) {
        const row = currentIndexRows[i];
        row.seqNo = i + 1;
        updatedIndexRows.push(row);
      }

      if (
        this.currentStatistics!.transactionalEmailSent + updatedIndexRows.length >
        this.user!.subscription!.transactionalEmailSize
      ) {
        this.modal.error({
          nzTitle: 'Maximum Email Reached',
          nzContent: 'Update plan to send more email.',
        });
        return;
      }

      if (value.sendWhatsAppMessage) {
        if (this.whatsappTemplate && this.whatsappTemplate.status !== WhatsappTemplateStatus.APPROVED) {
          this.modal.error({
            nzTitle: 'Invalid WhatsApp Template Status',
            nzContent:
              'WhatsApp status is not approved, please choose another WhatsApp template in transactional email settings or toggle off sending WhatsApp Message',
          });
          return;
        }

        if (!isDraft) {
          const invalidPhoneNo = currentIndexRows.find(
            (row: BaseIndexRowModel) =>
              !this.isValidMalaysiaMobileNo(row.text4) || this.validateWhatsappValidContentByIndexRow(row) !== ''
          );

          let whatsAppWarningMessage = invalidPhoneNo ? 'Skip invalid phone number(s)' : '';

          const invalidContent = currentIndexRows.find(
            (row: BaseIndexRowModel) => this.validateWhatsappValidContentByIndexRow(row) !== ''
          );

          if (invalidContent) {
            if (invalidPhoneNo) {
              whatsAppWarningMessage += ' and body message that exceed limit';
            } else {
              whatsAppWarningMessage += 'Skip body message(s) that exceed limit';
            }
          }

          if (whatsAppWarningMessage !== '') {
            this.modal.confirm({
              nzTitle: 'WhatsApp',
              nzContent: whatsAppWarningMessage,
              nzOkText: 'Skip',
              nzOnOk: () => {
                this.updateTransactionEmailActivity(isDraft, scheduledTimestamp, value, currentIndexRows);
              },
            });
          } else {
            this.updateTransactionEmailActivity(isDraft, scheduledTimestamp, value, currentIndexRows);
          }
        } else {
          this.updateTransactionEmailActivity(isDraft, scheduledTimestamp, value, currentIndexRows);
        }
      } else {
        this.updateTransactionEmailActivity(isDraft, scheduledTimestamp, value, currentIndexRows);
      }
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  private updateTransactionEmailActivity(
    isDraft: boolean,
    scheduledTimestamp: Date | undefined,
    value: any,
    currentIndexRows: BaseIndexRowModel[]
  ) {
    if (!isDraft) {
      this.modal.confirm({
        nzTitle: scheduledTimestamp ? 'Schedule transactional email' : 'Send transactional email',
        nzOkText: 'Yes',
        nzOkType: 'primary',
        nzOkDanger: true,
        nzOnOk: () => {
          this.dispatchUpdateTransactionalEmailActivity(value, isDraft, currentIndexRows);
        },
        nzCancelText: 'No',
      });
    } else {
      this.dispatchUpdateTransactionalEmailActivity(value, isDraft, currentIndexRows);
    }
  }

  private dispatchUpdateTransactionalEmailActivity(value: any, isDraft: boolean, indexRows: BaseIndexRowModel[]) {
    this.store.dispatch(
      new UpdateTransactionalEmailActivity(this.typeId!, this.activityId!, {
        name: value.name,
        emailFrom: value.emailFrom,
        emailFromName: value.emailFromName,
        emailSubject: value.emailSubject,
        emailContent: value.emailContent,
        emailMjmlContent: value.emailMjmlContent,
        scheduledTimestamp: value.scheduledTimestamp,
        status: isDraft ? ProcessStatus.DRAFT : ProcessStatus.SUBMITTED,
        sendWhatsAppMessage: value.sendWhatsAppMessage || false,
        indexRows,
      })
    );
  }

  getIndexFields(type: TransactionalEmailTypeModel): BaseIndexFieldModel[] {
    return type.indexFields.filter((field) => field.applicable);
  }

  getIndexFieldLabels(type: TransactionalEmailTypeModel) {
    return this.getIndexFields(type).map((field) => field.label);
  }

  getIndexRowValues(control: AbstractControl, transactionalEmailType: TransactionalEmailTypeModel, idx: number) {
    return transactionalEmailType.indexFields
      .filter((field) => field.applicable)
      .map((field) => {
        if (field.label === 'Attachment Password') {
          return { label: field.label, idx, value: '******', hiddenValue: control.get(`text${field.seqOrder}`)!.value };
        }

        switch (field.dataType) {
          case DataType.NUMBER:
            return { label: field.label, idx, value: control.get(`number${field.seqOrder}`)!.value };
          case DataType.DATE:
            const dateValue = control.get(`date${field.seqOrder}`)!.value;
            return { label: field.label, idx, value: dateValue ? new Date(dateValue).toLocaleDateString() : '' };
          default:
            return { label: field.label, idx, value: control.get(`text${field.seqOrder}`)!.value };
        }
      });
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doDeleteIndexRow(attachmentFilename: string, formArray: UntypedFormArray, seqOrder: number): void {
    this.modal.confirm({
      nzTitle: `Delete ${attachmentFilename}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        for (let i = 0; i < formArray.length; i++) {
          const transactionalEmailIndexRow = formArray.at(i);
          const currentSeqOrder = transactionalEmailIndexRow.get('seqOrder')!;
          if (currentSeqOrder.value === seqOrder) {
            formArray.removeAt(i);
          }
        }
        for (let i = 0; i < formArray.length; i++) {
          const transactionalEmailIndexRow = formArray.at(i);
          const currentSeqOrder = transactionalEmailIndexRow.get('seqOrder')!;
          currentSeqOrder.setValue(i + 1);
        }

        this.cd.markForCheck();
      },
      nzCancelText: 'No',
    });
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
    const isPdfOrZip = file.type === 'application/pdf' || file.type === 'application/zip';
    if (!isPdfOrZip) {
      this.canUploadAttachment = true;
      this.store.dispatch(new ShowMessage('error', 'Only PDF and ZIP is allowed'));
    }

    if (file.type === 'application/pdf' && file.size! > this.user!.subscription!.maxAttachmentSize) {
      this.modal.error({
        nzTitle: 'Invalid file size',
        nzContent: `Maximum PDF size is ${prettyBytes(this.user!.subscription!.maxAttachmentSize)}`,
      });
      this.canUploadAttachment = true;
      return false;
    }
    if (file.type === 'application/zip' && file.size! > environment.config.maxZipFileSizeBytes) {
      this.modal.error({
        nzTitle: 'Invalid file size',
        nzContent: 'Maximum ZIP size is 500MB',
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
        this.store.dispatch(new DeleteTransactionalEmailFile(this.typeId!, this.activityId!, file.id));
      },
      nzCancelText: 'No',
    });
  }

  doDeleteAllAttachments() {
    this.modal.confirm({
      nzTitle: `Delete All Attachments`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeleteTransactionalEmailFiles(this.typeId!, this.activityId!));
      },
      nzCancelText: 'No',
    });
  }

  beforeUploadIndexRow =
    (type: TransactionalEmailTypeModel) =>
    (file: NzUploadFile): boolean => {
      if (file instanceof File) {
        this.canUploadIndexRow = false;
        if (file.type === 'text/csv') {
          this.autoUnsubscribe(
            getUploadCsvData(file, type.indexFields, type.csvSeparator).pipe(
              tap(this.handleUploadData(type)),
              catchError((err) => {
                this.canUploadIndexRow = true;
                this.cd.markForCheck();
                this.modal.error({
                  nzTitle: 'Invalid CSV',
                  nzContent: err.join('<br />'),
                  nzWidth: '60%',
                });
                return of(null);
              })
            )
          );
        } else if (
          file.type === 'application/vnd.ms-excel' ||
          file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        ) {
          this.autoUnsubscribe(
            getUploadExcelData(file, type.indexFields).pipe(
              tap(this.handleUploadData(type)),
              catchError((err) => {
                this.canUploadIndexRow = true;
                this.cd.markForCheck();
                this.modal.error({
                  nzTitle: 'Invalid Excel',
                  nzContent: err.join('<br />'),
                  nzWidth: '80%',
                });
                return of(null);
              })
            )
          );
        } else {
          this.canUploadIndexRow = true;
          this.store.dispatch(new ShowMessage('error', 'Only Excel or CSV is allowed'));
          return false;
        }
        this.cd.markForCheck();
      }

      this.cd.markForCheck();
      return false;
    };

  private handleUploadData(type: TransactionalEmailTypeModel) {
    return (rows: BaseIndexRowModel[]) => {
      this.indexRows = rows;
      const indexRowErrors = validateIndexRows(DomainType.TRANSACTIONAL_EMAIL, rows, type.indexFields);
      if (indexRowErrors.length > 0) {
        this.indexRowErrors = indexRowErrors;
        this.isIndexRowErrorModalVisible = true;
        this.cd.markForCheck();
      } else {
        if (rows.length > environment.config.maxIndexRow) {
          this.modal.error({
            nzTitle: 'Invalid data',
            nzContent: `Maximum index row is ${environment.config.maxIndexRow} (${rows.length})`,
          });
          return;
        }

        doDeleteAllIndex(this.formIndexRows);
        for (const data of rows) {
          this.addIndexRowControl(data);
        }
      }
      this.canUploadIndexRow = true;
      this.cd.markForCheck();
    };
  }

  populateIndexRowFormFields(transactionalEmailType: TransactionalEmailTypeModel) {
    let addFormConfig: { [key: string]: any } = {};
    for (const transactionalEmailIndexField of transactionalEmailType.indexFields) {
      if (transactionalEmailIndexField.applicable) {
        switch (transactionalEmailIndexField.dataType) {
          case DataType.NUMBER:
            this.addFormControls.push({
              label: transactionalEmailIndexField.label,
              name: `number${transactionalEmailIndexField.seqOrder}`,
              type: DataType.NUMBER,
              required: transactionalEmailIndexField.required,
            });

            addFormConfig[`number${transactionalEmailIndexField.seqOrder}`] = [
              undefined,
              transactionalEmailIndexField.required ? [Validators.required] : [],
            ];
            break;
          case DataType.DATE:
            this.addFormControls.push({
              label: transactionalEmailIndexField.label,
              name: `date${transactionalEmailIndexField.seqOrder}`,
              type: DataType.DATE,
              required: transactionalEmailIndexField.required,
            });

            addFormConfig[`date${transactionalEmailIndexField.seqOrder}`] = [
              undefined,
              transactionalEmailIndexField.required ? [Validators.required] : [],
            ];
            break;
          case DataType.EMAIL:
            this.addFormControls.push({
              label: transactionalEmailIndexField.label,
              name: `text${transactionalEmailIndexField.seqOrder}`,
              type: DataType.EMAIL,
              required: transactionalEmailIndexField.required,
            });

            addFormConfig[`text${transactionalEmailIndexField.seqOrder}`] = [
              undefined,
              transactionalEmailIndexField.required ? [Validators.required, Validators.email] : [Validators.email],
            ];
            break;
          default:
            this.addFormControls.push({
              label: transactionalEmailIndexField.label,
              name: `text${transactionalEmailIndexField.seqOrder}`,
              type: DataType.TEXT,
              required: transactionalEmailIndexField.required,
            });

            const validators = [];
            if (transactionalEmailIndexField.required) {
              validators.push(Validators.required);
            }
            if (transactionalEmailIndexField.label === 'Attachment Filename') {
              validators.push(pdfFilenameValidator);
            }

            addFormConfig[`text${transactionalEmailIndexField.seqOrder}`] = [undefined, validators];
            break;
        }
      }
    }
    this.indexRowForm = this.fb.group(addFormConfig);
    this.cd.markForCheck();
  }

  doSkipInvalidEmails(type: TransactionalEmailTypeModel) {
    this.indexRows = uniqBy(this.indexRows, 'text1').filter((indexRow) => {
      const email = indexRow['text1'];
      const control = new UntypedFormControl(email, Validators.email);
      return !(control.errors && control.errors['email']);
    });

    const indexRowErrors = validateIndexRows(DomainType.TRANSACTIONAL_EMAIL, this.indexRows, type.indexFields, true);
    if (indexRowErrors.length > 0) {
      this.indexRowErrors = indexRowErrors;
      this.isIndexRowErrorModalVisible = true;
      this.cd.markForCheck();
    } else {
      if (this.indexRows.length > environment.config.maxIndexRow) {
        this.modal.error({
          nzTitle: 'Invalid data',
          nzContent: `Maximum index row is ${environment.config.maxIndexRow} (${this.indexRows.length})`,
        });
        return;
      }

      doDeleteAllIndex(this.formIndexRows);
      for (const data of this.indexRows) {
        this.addIndexRowControl(data);
      }

      this.isIndexRowErrorModalVisible = false;
    }
    this.canUploadIndexRow = true;
    this.cd.markForCheck();
  }

  doDownloadIndexRow(type: TransactionalEmailTypeModel) {
    this.modal.confirm({
      nzTitle: 'Download Sample',
      nzContent: 'Download excel with sample data',
      nzOkText: 'Yes',
      nzCancelText: 'No',
      nzOnOk: () => {
        this.store.dispatch(new SetPageLoading(true));
        this.store.dispatch(new DownloadIndexData([], type, true));
      },
      nzOnCancel: () => {
        this.store.dispatch(new SetPageLoading(true));
        this.store.dispatch(new DownloadIndexData([], type));
      },
    });
    return of(null);
  }

  getExcelSampleIndexData(field: BaseIndexFieldModel) {
    return getSampleIndexData(field);
  }

  openIndexRowForm() {
    this.isIndexRowFormVisible = true;
  }

  closeIndexRowForm() {
    this.isIndexRowFormVisible = false;
  }

  doSubmitIndexRow() {
    this.isIndexRowFormLoading = true;
    this.cd.markForCheck();

    if (this.indexRowForm.valid) {
      const value = this.indexRowForm.getRawValue();
      this.addIndexRowControl(value);

      this.indexRowForm.reset();
      this.isIndexRowFormVisible = false;
    } else {
      updateAndMarkControlAsDirty(this.indexRowForm);
    }

    this.isIndexRowFormLoading = false;
    this.cd.markForCheck();
  }

  resetHour(date?: Date): Date | undefined {
    if (!date) {
      return date;
    }
    const targetDate = new Date(date);
    targetDate.setHours(8, 0, 0, 0);
    return targetDate;
  }

  doSearchIndexRow(event: any) {
    this.searchIndexRowFilename = event.target.value;
  }

  getFilteredIndexRow(): AbstractControl[] {
    return this.formIndexRows.controls.filter((control) =>
      this.searchIndexRowFilename ? control.get('text1')!.value.includes(this.searchIndexRowFilename) : true
    );
  }

  getWhatsappFilteredIndexRow(): AbstractControl[] {
    return this.formIndexRows.controls.filter((control) =>
      this.searchIndexRowName ? control.get('text1')!.value.includes(this.searchIndexRowName) : true
    );
  }

  isValidMalaysiaMobileNo(mobileNo: string) {
    if (mobileNo) {
      const malaysiaMobileNoRegex = /^\+?60\d{9,10}$/;
      return malaysiaMobileNoRegex.test(mobileNo);
    } else {
      return false;
    }
  }

  getWhatsappBodyContent(indexRowControl: AbstractControl) {
    if (this.whatsappTemplate) {
      let updatedBody = getWhatsAppTemplateBody(this.whatsappTemplate);
      if (this.transactionalEmailType?.whatsAppTemplateParams) {
        const params = this.transactionalEmailType.whatsAppTemplateParams;
        for (const param of params) {
          const field = this.transactionalEmailType.indexFields.find((field) => field.header === param.field);
          if (field) {
            const indexFieldName = getIndexFieldName(field);
            updatedBody = updatedBody.replace(param.index, indexRowControl.get(indexFieldName)?.value || param.field);
          }
        }
      }

      return updatedBody;
    }

    return '';
  }

  getWhatsappBodyContentByIndexRow(indexRow: BaseIndexRowModel) {
    if (this.whatsappTemplate) {
      let updatedBody = getWhatsAppTemplateBody(this.whatsappTemplate);
      if (this.transactionalEmailType?.whatsAppTemplateParams) {
        const params = this.transactionalEmailType.whatsAppTemplateParams;
        for (const param of params) {
          const field = this.transactionalEmailType.indexFields.find((field) => field.header === param.field);
          if (field) {
            const indexFieldName = getIndexFieldName(field);
            updatedBody = updatedBody.replace(param.index, (indexRow as any)[indexFieldName] || param.field);
          }
        }
      }

      return updatedBody;
    }

    return '';
  }

  validateWhatsappValidContent(indexRowControl: AbstractControl) {
    const bodyContent = this.getWhatsappBodyContent(indexRowControl);
    if (bodyContent.length > 1024) {
      return 'Exceed body content size 1024';
    }
    return '';
  }

  validateWhatsappValidContentByIndexRow(indexRow: BaseIndexRowModel) {
    const bodyContent = this.getWhatsappBodyContentByIndexRow(indexRow);
    if (bodyContent.length > 1024) {
      return 'Exceed body content size 1024';
    }
    return '';
  }

  addIndexRowControl(indexRow: BaseIndexRowModel): void {
    const indexFieldForm = this.fb.group({
      id: [this.formIndexRows.length + 1, [Validators.required]],
      seqOrder: [this.formIndexRows.length + 1, [Validators.required]],
      text1: [indexRow.text1],
      number1: [indexRow.number1],
      date1: [this.resetHour(indexRow.date1)],
      text2: [indexRow.text2],
      number2: [indexRow.number2],
      date2: [this.resetHour(indexRow.date2)],
      text3: [indexRow.text3],
      number3: [indexRow.number3],
      date3: [this.resetHour(indexRow.date3)],
      text4: [indexRow.text4],
      number4: [indexRow.number4],
      date4: [this.resetHour(indexRow.date4)],
      text5: [indexRow.text5],
      number5: [indexRow.number5],
      date5: [this.resetHour(indexRow.date5)],
      text6: [indexRow.text6],
      number6: [indexRow.number6],
      date6: [this.resetHour(indexRow.date6)],
      text7: [indexRow.text7],
      number7: [indexRow.number7],
      date7: [this.resetHour(indexRow.date7)],
      text8: [indexRow.text8],
      number8: [indexRow.number8],
      date8: [this.resetHour(indexRow.date8)],
      text9: [indexRow.text9],
      number9: [indexRow.number9],
      date9: [this.resetHour(indexRow.date9)],
      text10: [indexRow.text10],
      number10: [indexRow.number10],
      date10: [this.resetHour(indexRow.date10)],
    });
    this.formIndexRows.push(indexFieldForm);
    this.cd.markForCheck();
  }

  doCloseIndexRowErrorModal() {
    this.isIndexRowErrorModalVisible = false;
  }

  getMissingIndexRows(files: BaseFileModel[]) {
    const missingIndexRows: string[] = [];
    const attachmentFilenames = files.map((file) => file.name);
    const rowValues = this.formIndexRows.value;
    const filenames = rowValues.map((row: BaseIndexRowModel) => row.text2);

    for (const filename of attachmentFilenames) {
      if (!filenames.includes(filename) && !filenames.includes(filename)) {
        missingIndexRows.push(filename);
      }
    }

    return missingIndexRows;
  }

  getMissingAttachments(files: BaseFileModel[]) {
    const missingFiles: string[] = [];
    const attachmentFilenames = files.map((file) => file.name);
    const rowValues = this.formIndexRows.value;
    const filenames = rowValues.map((row: BaseIndexRowModel) => row.text2);

    for (const filename of filenames) {
      if (!attachmentFilenames.includes(filename) && !missingFiles.includes(filename)) {
        missingFiles.push(filename);
      }
    }

    return missingFiles;
  }

  togglePasswordVisible() {
    this.showPassword = !this.showPassword;
    this.cd.markForCheck();
  }

  doOpenScheduleModal() {
    this.isScheduleModalVisible = true;
  }

  doCloseScheduleModal() {
    this.isScheduleModalVisible = false;
  }

  doSchedule(type: TransactionalEmailTypeModel, files: BaseFileModel[]) {
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
    const mismatchMergeFields: string[] = [];

    const mergeFieldMatch = this.mjml.matchAll(/{{[\w _-]+}}/g);
    for (const match of mergeFieldMatch) {
      const field = match[0].replace('{{', '').replace('}}', '');
      if (field !== 'unsubscribe_link' && !this.availableFields.includes(field)) {
        mismatchMergeFields.push(field);
      }
    }

    return mismatchMergeFields;
  }

  getWhatsappTemplateHeader() {
    return this.whatsappTemplate ? getWhatsAppTemplateHeader(this.whatsappTemplate) : '-';
  }

  getWhatsappTemplateFooter() {
    return this.whatsappTemplate ? getWhatsAppTemplateFooter(this.whatsappTemplate) : '-';
  }

  getWhatsappTemplateButton() {
    return this.whatsappTemplate ? getWhatsAppTemplateButton(this.whatsappTemplate) : '-';
  }
}
