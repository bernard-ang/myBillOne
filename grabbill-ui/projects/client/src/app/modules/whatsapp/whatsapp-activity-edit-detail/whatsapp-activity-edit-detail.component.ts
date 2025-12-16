import {
  AbstractControl,
  AsyncValidatorFn,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { WhatsAppApi } from '../../../../api/whatsapp.api';
import { map, Observable, of, Subject, switchMap, tap } from 'rxjs';
import {
  BaseFileModel,
  BaseIndexFieldModel,
  BaseIndexRowModel,
  DataType,
  DomainType,
  getErrorMessage,
  ProcessStatus,
  resolveErrorMessage,
  updateAndMarkControlAsDirty,
  UserAuthorityModel,
  WhatsAppActivityModel,
  WhatsappTemplateModel,
  WhatsappTemplateStatus,
  WhatsAppTypeModel,
} from '@grabbill/lib';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  TrackByFunction,
  ViewChild,
} from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { IndexRowError, validateIndexRows } from '../../../../utils/validate-index-rows';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { AuthState } from '../../../../states/auth/auth.state';
import { WhatsAppState } from '../../../../states/whatsapp/whatsapp.state';
import { ActivatedRoute, Params } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import {
  DeleteWhatsAppFile,
  DeleteWhatsAppFiles,
  DownloadIndexData,
  GetWhatsAppActivity,
  GetWhatsAppTemplates,
  GetWhatsAppType,
  ResetWhatsAppActivity,
  UpdateWhatsAppActivity,
  UploadWhatsAppFile,
} from '../../../../states/whatsapp/whatsapp.state-actions';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { isElementBottomVisible, isElementTopVisible } from '../../../../utils/is-in-viewport';
import { scrollToTargetAdjusted } from '../../../../utils/scroll-to-target-adjusted';
import { NzUploadFile } from 'ng-zorro-antd/upload';
import prettyBytes from 'pretty-bytes';
import { getUploadCsvData } from '../../../../utils/get-upload-csv-data';
import { catchError, concatMap } from 'rxjs/operators';
import { getUploadExcelData } from '../../../../utils/get-upload-excel-data';
import { doDeleteAllIndex } from '../../../../utils/manage-form-array';
import { pdfFilenameValidator } from '../../../../utils/filename-validator';
import {
  createWorkbook,
  downloadWorkbook,
  ExcelColumn,
  generateIndexRowArray,
  getSampleIndexData,
} from '../../../../utils/download-index-row';
import {
  getWhatsAppTemplateBody,
  getWhatsAppTemplateButton,
  getWhatsAppTemplateFooter,
  getWhatsAppTemplateHeader,
} from '../../../../utils/whatsapp-template';
import { getIndexFieldName } from '../../../../utils/get-index-field-name';
import { environment } from '../../../../environments/environment';
import { whatsappNoRegex, whatsappNoValidator } from "../../../../utils/phone-regex";

export const makeNameValidator = (whatsAppApi: WhatsAppApi, typeId: number, originalName: string): AsyncValidatorFn => {
  return (control: AbstractControl): Observable<ValidationErrors> => {
    const value = control.value;
    if (value === originalName) {
      return of({});
    }

    if (!/^[a-z0-9-: ]+$/i.test(value)) {
      return of({ alphanumericWithSpaceDashColonOnly: value });
    }

    return whatsAppApi
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
  selector: 'grabbill-client-whatsapp-activity-edit-detail',
  templateUrl: './whatsapp-activity-edit-detail.component.html',
  styleUrls: ['./whatsapp-activity-edit-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WhatsappActivityEditDetailComponent extends NgxsBaseComponent {
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

  index = 1;
  lastScrollTop = 0;
  isScrolling = false;

  user?: UserAuthorityModel;

  whatsAppType?: WhatsAppTypeModel;

  whatsappTemplates: WhatsappTemplateModel[] = [];
  whatsappTemplate?: WhatsappTemplateModel;

  @ViewChild('step2') step2!: ElementRef<HTMLDivElement>;
  @ViewChild('step3') step3!: ElementRef<HTMLDivElement>;
  @ViewChild('step4') step4!: ElementRef<HTMLDivElement>;

  trackByFn: TrackByFunction<{
    label: string;
    idx: number;
    value: any;
    hiddenValue?: any;
  }> = (_, result) => `${result.label}-${result.idx}`;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  @Select(WhatsAppState.whatsAppType)
  whatsAppType$!: Observable<WhatsAppTypeModel>;

  @Select(WhatsAppState.whatsAppActivity)
  whatsAppActivity$!: Observable<WhatsAppActivityModel>;

  @Select(WhatsAppState.whatsAppActivityFiles)
  whatsAppActivityFiles$!: Observable<BaseFileModel[]>;

  @Select(WhatsAppState.templates)
  whatsappTemplates$!: Observable<WhatsappTemplateModel[]>;

  uploadAttachmentQueue$ = new Subject<FormData>();

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    private modal: NzModalService,
    private whatsAppApi: WhatsAppApi,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
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

    this.store.dispatch(new GetWhatsAppTemplates());
    this.store.dispatch(new ResetWhatsAppActivity());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.store.dispatch(new GetWhatsAppType(this.typeId!));
          this.store.dispatch(new GetWhatsAppActivity(this.typeId!, this.activityId!));
        })
      ),
      this.user$.pipe(
        tap((user) => {
          if (user) {
            this.user = user;
          }
        })
      ),
      this.whatsappTemplates$.pipe(
        tap((whatsappTemplates) => {
          this.whatsappTemplates = whatsappTemplates;
          if (this.whatsAppType) {
            this.whatsappTemplate = whatsappTemplates.find(
              (item) => item.name === this.whatsAppType?.whatsAppTemplateName
            );
          }
        })
      ),
      this.whatsAppType$.pipe(
        tap((whatsAppType) => {
          if (whatsAppType) {
            this.whatsAppType = whatsAppType;
            this.availableFields = whatsAppType.indexFields
              .filter((value) => value.applicable)
              .map((value) => value.header);
            this.populateIndexRowFormFields(whatsAppType);

            if (this.whatsappTemplates.length > 0) {
              this.whatsappTemplate = this.whatsappTemplates.find(
                (item) => item.name === this.whatsAppType?.whatsAppTemplateName
              );
            }
          }
        })
      ),
      this.whatsAppActivity$.pipe(
        tap((whatsAppActivity) => {
          if (whatsAppActivity) {
            this.form
              .get('name')!
              .addAsyncValidators(makeNameValidator(this.whatsAppApi, Number(this.typeId), whatsAppActivity.name));
            this.form.setValue({
              name: whatsAppActivity.name,
              indexRows: [],
            });

            for (const row of whatsAppActivity.indexRows) {
              this.addIndexRowControl(row);
            }
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
        ofActionCompleted(GetWhatsAppActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'whatsapp', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateWhatsAppActivity),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          const status = data.action.request.status;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity updated`));
            if (status === ProcessStatus.DRAFT) {
              this.navigate(['/', 'whatsapp', 'detail', this.typeId!], { tab: 'activities' });
            } else {
              this.navigate(['/', 'whatsapp', 'detail', this.typeId!, 'activity', this.activityId!], {
                tab: 'info',
              });
            }
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UploadWhatsAppFile),
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
        ofActionCompleted(DeleteWhatsAppFile),
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
        ofActionCompleted(DeleteWhatsAppFiles),
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
          return this.store.dispatch(new UploadWhatsAppFile(this.typeId!, this.activityId!, formData));
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
      this.navigate(['/', 'whatsapp', 'detail', this.typeId, 'edit']);
    } else if (index === 1) {
      scrollToTargetAdjusted(this.step2.nativeElement);
    } else if (index === 2) {
      scrollToTargetAdjusted(this.step3.nativeElement);
    } else if (index === 3) {
      scrollToTargetAdjusted(this.step4.nativeElement);
    }
    this.isScrolling = false;
  }

  doSubmitForm(isDraft: boolean, type: WhatsAppTypeModel, files: BaseFileModel[], scheduledTimestamp?: Date): void {
    if (this.form.valid) {
      if (!isDraft) {
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

      if (!value.skipWhatsAppMessage) {
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
              !this.isValidWhatsappNo(row.text1) || this.validateWhatsappValidContentByIndexRow(row) !== ''
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
                this.updateWhatsAppActivity(isDraft, scheduledTimestamp, value, currentIndexRows);
              },
            });
          } else {
            this.updateWhatsAppActivity(isDraft, scheduledTimestamp, value, currentIndexRows);
          }
        } else {
          this.updateWhatsAppActivity(isDraft, scheduledTimestamp, value, currentIndexRows);
        }
      } else {
        this.updateWhatsAppActivity(isDraft, scheduledTimestamp, value, currentIndexRows);
      }
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  private updateWhatsAppActivity(
    isDraft: boolean,
    scheduledTimestamp: Date | undefined,
    value: any,
    currentIndexRows: BaseIndexRowModel[]
  ) {
    if (!isDraft) {
      this.modal.confirm({
        nzTitle: scheduledTimestamp ? 'Schedule WhatsApp' : 'Send WhatsApp',
        nzOkText: 'Yes',
        nzOkType: 'primary',
        nzOkDanger: true,
        nzOnOk: () => {
          this.dispatchUpdateWhatsAppActivity(value, isDraft, currentIndexRows);
        },
        nzCancelText: 'No',
      });
    } else {
      this.dispatchUpdateWhatsAppActivity(value, isDraft, currentIndexRows);
    }
  }

  private dispatchUpdateWhatsAppActivity(value: any, isDraft: boolean, indexRows: BaseIndexRowModel[]) {
    this.store.dispatch(
      new UpdateWhatsAppActivity(this.typeId!, this.activityId!, {
        name: value.name,
        scheduledTimestamp: value.scheduledTimestamp,
        status: isDraft ? ProcessStatus.DRAFT : ProcessStatus.SUBMITTED,
        indexRows,
      })
    );
  }

  getIndexFields(type: WhatsAppTypeModel): BaseIndexFieldModel[] {
    return type.indexFields.filter((field) => field.applicable);
  }

  getIndexFieldLabels(type: WhatsAppTypeModel) {
    return this.getIndexFields(type).map((field) => field.label);
  }

  getIndexRowValues(
    control: AbstractControl,
    whatsAppType: WhatsAppTypeModel,
    idx: number
  ): {
    label: string;
    idx: number;
    value: any;
    hiddenValue?: any;
  }[] {
    return whatsAppType.indexFields
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
          const whatsAppIndexRow = formArray.at(i);
          const currentSeqOrder = whatsAppIndexRow.get('seqOrder')!;
          if (currentSeqOrder.value === seqOrder) {
            formArray.removeAt(i);
          }
        }
        for (let i = 0; i < formArray.length; i++) {
          const whatsAppIndexRow = formArray.at(i);
          const currentSeqOrder = whatsAppIndexRow.get('seqOrder')!;
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
        this.store.dispatch(new DeleteWhatsAppFile(this.typeId!, this.activityId!, file.id));
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
        this.store.dispatch(new DeleteWhatsAppFiles(this.typeId!, this.activityId!));
      },
      nzCancelText: 'No',
    });
  }

  beforeUploadIndexRow =
    (type: WhatsAppTypeModel) =>
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

  private handleUploadData(type: WhatsAppTypeModel) {
    return (rows: BaseIndexRowModel[]) => {
      this.indexRows = rows;
      const indexRowErrors = validateIndexRows(DomainType.WHATSAPP, rows, type.indexFields);
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

  populateIndexRowFormFields(whatsAppType: WhatsAppTypeModel) {
    let addFormConfig: { [key: string]: any } = {};
    for (const whatsAppIndexField of whatsAppType.indexFields) {
      if (whatsAppIndexField.applicable) {
        switch (whatsAppIndexField.dataType) {
          case DataType.NUMBER:
            this.addFormControls.push({
              label: whatsAppIndexField.label,
              name: `number${whatsAppIndexField.seqOrder}`,
              type: DataType.NUMBER,
              required: whatsAppIndexField.required,
            });

            addFormConfig[`number${whatsAppIndexField.seqOrder}`] = [
              undefined,
              whatsAppIndexField.required ? [Validators.required] : [],
            ];
            break;
          case DataType.DATE:
            this.addFormControls.push({
              label: whatsAppIndexField.label,
              name: `date${whatsAppIndexField.seqOrder}`,
              type: DataType.DATE,
              required: whatsAppIndexField.required,
            });

            addFormConfig[`date${whatsAppIndexField.seqOrder}`] = [
              undefined,
              whatsAppIndexField.required ? [Validators.required] : [],
            ];
            break;
          case DataType.EMAIL:
            this.addFormControls.push({
              label: whatsAppIndexField.label,
              name: `text${whatsAppIndexField.seqOrder}`,
              type: DataType.EMAIL,
              required: whatsAppIndexField.required,
            });

            addFormConfig[`text${whatsAppIndexField.seqOrder}`] = [
              undefined,
              whatsAppIndexField.required ? [Validators.required, Validators.email] : [Validators.email],
            ];
            break;
          default:
            this.addFormControls.push({
              label: whatsAppIndexField.label,
              name: `text${whatsAppIndexField.seqOrder}`,
              type: DataType.TEXT,
              required: whatsAppIndexField.required,
            });

            const validators = [];
            if (whatsAppIndexField.required) {
              validators.push(Validators.required);
            }
            if (whatsAppIndexField.label === 'Attachment Filename') {
              validators.push(pdfFilenameValidator);
            }
            if (whatsAppIndexField.label === 'WhatsApp No') {
              validators.push(whatsappNoValidator());
            }

            addFormConfig[`text${whatsAppIndexField.seqOrder}`] = [undefined, validators];
            break;
        }
      }
    }
    this.indexRowForm = this.fb.group(addFormConfig);
    this.cd.markForCheck();
  }

  doDownloadIndexRow(type: WhatsAppTypeModel) {
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

  isValidWhatsappNo(whatsappNo: string | undefined) {
    if (whatsappNo) {
      return whatsappNoRegex.test(whatsappNo);
    } else {
      return false;
    }
  }

  getWhatsappBodyContent(indexRowControl: AbstractControl) {
    if (this.whatsappTemplate) {
      let updatedBody = getWhatsAppTemplateBody(this.whatsappTemplate);
      if (this.whatsAppType?.whatsAppTemplateParams) {
        const params = this.whatsAppType.whatsAppTemplateParams;
        for (const param of params) {
          const field = this.whatsAppType.indexFields.find((field) => field.header === param.field);
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
      if (this.whatsAppType?.whatsAppTemplateParams) {
        const params = this.whatsAppType.whatsAppTemplateParams;
        for (const param of params) {
          const field = this.whatsAppType.indexFields.find((field) => field.header === param.field);
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

  doSchedule(type: WhatsAppTypeModel, files: BaseFileModel[]) {
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
