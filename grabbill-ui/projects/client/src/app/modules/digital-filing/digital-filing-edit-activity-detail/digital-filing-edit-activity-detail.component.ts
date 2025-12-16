import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  Inject,
  ViewChild,
} from '@angular/core';
import {
  AbstractControl,
  AsyncValidatorFn,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Params } from '@angular/router';
import { DOCUMENT } from '@angular/common';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { map, Observable, of, Subject, switchMap, tap } from 'rxjs';
import { catchError, concatMap } from 'rxjs/operators';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzUploadFile } from 'ng-zorro-antd/upload';
import {
  BaseActivitySftpValidationSummaryModel,
  BaseFileModel, BaseIndexFieldModel,
  BaseIndexRowModel,
  DataType,
  DigitalFilingActivityModel,
  DigitalFilingTypeModel,
  DomainType,
  getErrorMessage,
  ProcessStatus,
  resolveErrorMessage,
  updateAndMarkControlAsDirty, UserAuthorityModel
} from "@grabbill/lib";
import { environment } from '../../../../environments/environment';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { DigitalFilingApi } from '../../../../api/digital-filing.api';
import { SetPageLoading, ShowMessage } from "../../../../states/common/common.state-actions";
import { DigitalFilingState } from '../../../../states/digital-filing/digital-filing.state';
import {
  DeleteDigitalFilingFile,
  DeleteDigitalFilingFiles,
  DownloadIndexData,
  GetDigitalFilingActivity,
  GetDigitalFilingType,
  ResetDigitalFilingActivity,
  UpdateDigitalFilingActivity,
  UploadDigitalFilingFile,
  ValidateDigitalFilingActivitySftp
} from "../../../../states/digital-filing/digital-filing.state-actions";
import { doDeleteAllIndex } from '../../../../utils/manage-form-array';
import { getUploadExcelData } from '../../../../utils/get-upload-excel-data';
import { IndexRowError, validateBaseIndexRows, validateIndexRows } from "../../../../utils/validate-index-rows";
import { getUploadCsvData } from '../../../../utils/get-upload-csv-data';
import { isElementBottomVisible, isElementTopVisible } from '../../../../utils/is-in-viewport';
import { scrollToTargetAdjusted } from '../../../../utils/scroll-to-target-adjusted';
import { filenameValidator } from '../../../../utils/filename-validator';
import {
  createWorkbook,
  downloadWorkbook,
  ExcelColumn,
  generateIndexRowArray, getSampleIndexData
} from "../../../../utils/download-index-row";
import { AuthState } from "../../../../states/auth/auth.state";
import { codeIndexField } from '../../../../utils/code-index-field';
import { ValidateMultiTemplateWhatsAppActivitySftp } from "../../../../states/whatsapp/whatsapp.state-actions";

export const makeNameValidator = (
  digitalFilingApi: DigitalFilingApi,
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

    return digitalFilingApi
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
  selector: 'grabbill-client-digital-filing-edit-activity-detail',
  templateUrl: './digital-filing-edit-activity-detail.component.html',
  styleUrls: ['./digital-filing-edit-activity-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DigitalFilingEditActivityDetailComponent extends NgxsBaseComponent {
  index = 1;
  lastScrollTop = 0;
  isScrolling = false;

  form: UntypedFormGroup;
  typeId?: number;
  activityId?: number;
  isLoading = true;
  canUploadAttachment = true;
  canUploadIndexRow = true;

  searchIndexRowFilename?: string;
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

  isRowFileModalVisible = false;

  user?: UserAuthorityModel;

  sftpPrePath = '';
  isSftp = false;
  isSftpErrorModalVisible = false;
  summary?: BaseActivitySftpValidationSummaryModel;
  codeIndexField: BaseIndexFieldModel = codeIndexField;

  @ViewChild('step2') step2!: ElementRef<HTMLDivElement>;
  @ViewChild('step3') step3!: ElementRef<HTMLDivElement>;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  @Select(DigitalFilingState.digitalFilingType)
  digitalFilingType$!: Observable<DigitalFilingTypeModel>;

  @Select(DigitalFilingState.digitalFilingActivity)
  digitalFilingActivity$!: Observable<DigitalFilingActivityModel>;

  @Select(DigitalFilingState.digitalFilingActivityFiles)
  digitalFilingActivityFiles$!: Observable<BaseFileModel[]>;

  uploadAttachmentQueue$ = new Subject<FormData>();

  constructor(
    @Inject(DOCUMENT) private document: Document,
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
    private modal: NzModalService,
    private digitalFilingApi: DigitalFilingApi,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      sftp: [false, []],
      sftpPath: ['', [Validators.required]],
      indexRows: this.fb.array([]),
    });

    this.indexRowForm = this.fb.group({});
  }

  get formIndexRows() {
    return this.form.controls['indexRows'] as UntypedFormArray;
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetDigitalFilingActivity());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.store.dispatch(new GetDigitalFilingType(this.typeId!));
          this.store.dispatch(new GetDigitalFilingActivity(this.typeId!, this.activityId!));
        })
      ),
      this.user$.pipe(
        tap((user) => {
          if (user) {
            this.user = user;
            if (user.account.sftpUsername) {
              this.sftpPrePath = `sftp://${user.account.sftpUsername}@${user.account.sftpHost}:${user.account.sftpPort}/`;
            } else {
              this.sftpPrePath = 'Please configure SFTP settings';
            }
          }
        })
      ),
      this.digitalFilingType$.pipe(
        tap((digitalFilingType) => {
          if (digitalFilingType) {
            this.populateIndexRowFormFields(digitalFilingType);
          }
        })
      ),
      this.digitalFilingActivity$.pipe(
        tap((digitalFilingActivity) => {
          if (digitalFilingActivity) {
            this.form
              .get('name')!
              .addAsyncValidators(
                makeNameValidator(this.digitalFilingApi, Number(this.typeId), digitalFilingActivity.name)
              );
            this.form.setValue({
              name: digitalFilingActivity.name,
              sftp: digitalFilingActivity.sftp,
              sftpPath: digitalFilingActivity.sftpPath || null,
              indexRows: [],
            });
            for (const row of digitalFilingActivity.indexRows) {
              this.addIndexRowControl(row);
            }

            this.toggleSftp();
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
        ofActionCompleted(GetDigitalFilingActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'digital-filing', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateDigitalFilingActivity),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          const status = data.action.request.status;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity updated`));
            if (status === ProcessStatus.DRAFT) {
              this.navigate(['/', 'digital-filing', 'detail', this.typeId!], { tab: 'activities' });
            } else {
              this.navigate(['/', 'digital-filing', 'detail', this.typeId!, 'activity', this.activityId!], {
                tab: 'info',
              });
            }
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UploadDigitalFilingFile),
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
        ofActionCompleted(DeleteDigitalFilingFile),
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
        ofActionCompleted(DeleteDigitalFilingFiles),
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
          const excelColumns: ExcelColumn[] = [this.codeIndexField, ...data.action.type.indexFields]
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
          )
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(ValidateDigitalFilingActivitySftp),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const validationSummary = this.store.selectSnapshot(DigitalFilingState.digitalFilingActivityError);

            if (validationSummary!.hasError) {
              this.summary = validationSummary;
              this.isSftpErrorModalVisible = true;
            } else {
              this.doSubmitForm(false, [], false);
            }
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.uploadAttachmentQueue$.pipe(
        concatMap((formData) => {
          return this.store.dispatch(new UploadDigitalFilingFile(this.typeId!, this.activityId!, formData));
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
    this.index = index;

    if (index === 0) {
      this.navigate(['/', 'digital-filing', 'detail', this.typeId, 'edit']);
    } else if (index === 1) {
      scrollToTargetAdjusted(this.step2.nativeElement);
    } else if (index === 2) {
      scrollToTargetAdjusted(this.step3.nativeElement);
    }
  }

  doSubmitForm(isDraft: boolean, files: BaseFileModel[], validateSftp = true): void {
    if (this.form.valid) {
      if (!isDraft) {
        if (this.isSftp && this.sftpPrePath === 'Please configure SFTP settings') {
          this.modal.error({
            nzTitle: 'Invalid SFTP Setting',
            nzContent: 'Please configure SFTP setting',
          });
          return;
        }

        if (files.length === 0 && !this.isSftp) {
          this.modal.info({
            nzTitle: 'Invalid Upload',
            nzContent: 'Minimum 1 attachment is required',
          });
          return;
        }

        const missingAttachments = this.getMissingAttachments(files);
        const missingIndexRows = this.getMissingIndexRows(files);
        if (missingAttachments.length > 0 || missingIndexRows.length > 0) {
          this.isRowFileModalVisible = true;
          this.cd.markForCheck();
          return;
        }
      }

      const value = this.form.getRawValue();

      const updatedindexRows = [];
      const currentindexRows = value.indexRows;
      for (let i = 0; i < currentindexRows.length; i++) {
        const row = currentindexRows[i];
        row.seqNo = i + 1;
        updatedindexRows.push(row);
      }

      if (isDraft) {
        this.dispatchUpdateDigitalActivity(value, isDraft, currentindexRows);
      } else {
        if (validateSftp && this.isSftp) {
          this.store.dispatch(
            new ValidateDigitalFilingActivitySftp(this.typeId!, this.activityId!, {
              name: value.name,
              status: isDraft ? ProcessStatus.DRAFT : ProcessStatus.SUBMITTED,
              sftp: value.sftp,
              sftpPath: value.sftpPath,
              indexRows: [],
            })
          );
          return;
        }

        this.modal.confirm({
          nzTitle: `Upload Files`,
          nzOkText: 'Yes',
          nzOkType: 'primary',
          nzOkDanger: true,
          nzOnOk: () => {
            this.dispatchUpdateDigitalActivity(value, isDraft, currentindexRows);
          },
          nzCancelText: 'No',
        });
      }
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  private dispatchUpdateDigitalActivity(value: any, isDraft: boolean, indexRows: BaseIndexRowModel[]) {
    this.store.dispatch(
      new UpdateDigitalFilingActivity(this.typeId!, this.activityId!, {
        name: value.name,
        status: isDraft ? ProcessStatus.DRAFT : ProcessStatus.SUBMITTED,
        sftp: value.sftp,
        sftpPath: value.sftpPath,
        indexRows,
      })
    );
  }

  getIndexFields(digitalFileType: DigitalFilingTypeModel) {
    return digitalFileType.indexFields.map((field) => field.label);
  }

  getIndexRowValues(control: AbstractControl, digitalFileType: DigitalFilingTypeModel) {
    return digitalFileType.indexFields.map((field) => {
      switch (field.dataType) {
        case DataType.NUMBER:
          return control.get(`number${field.seqOrder}`)!.value;
        case DataType.DATE:
          const dateValue = control.get(`date${field.seqOrder}`)!.value;
          return dateValue ? new Date(dateValue).toLocaleDateString() : '';
        default:
          return control.get(`text${field.seqOrder}`)!.value;
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
          const digitalFilingIndexRow = formArray.at(i);
          const currentSeqOrder = digitalFilingIndexRow.get('seqOrder')!;
          if (currentSeqOrder.value === seqOrder) {
            formArray.removeAt(i);
          }
        }
        for (let i = 0; i < formArray.length; i++) {
          const digitalFilingIndexRow = formArray.at(i);
          const currentSeqOrder = digitalFilingIndexRow.get('seqOrder')!;
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

    if (file.type === 'application/zip' && file.size! > environment.config.maxZipFileSizeBytes) {
      this.modal.error({
        nzTitle: 'Invalid file size',
        nzContent: 'Maximum ZIP size is 500MB',
      });
      this.canUploadAttachment = true;
      return false;
    } else if (file.size! > environment.config.maxPdfFileSizeBytes) {
      this.modal.error({
        nzTitle: 'Invalid file size',
        nzContent: 'Maximum file size is 20MB',
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
        this.store.dispatch(new DeleteDigitalFilingFile(this.typeId!, this.activityId!, file.id));
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
        this.store.dispatch(new DeleteDigitalFilingFiles(this.typeId!, this.activityId!));
      },
      nzCancelText: 'No',
    });
  }

  beforeUploadIndexRow =
    (type: DigitalFilingTypeModel) =>
    (file: NzUploadFile): boolean => {
      if (file instanceof File) {
        this.canUploadIndexRow = false;
        if (file.type === 'text/csv') {
          this.autoUnsubscribe(
            getUploadCsvData(file, [this.codeIndexField, ...type.indexFields], type.csvSeparator).pipe(
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
            getUploadExcelData(file, [this.codeIndexField, ...type.indexFields]).pipe(
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

  private handleUploadData(type: DigitalFilingTypeModel) {
    return (rows: BaseIndexRowModel[]) => {
      this.indexRows = rows;
      const indexRowErrors = validateBaseIndexRows(
        DomainType.DIGITAL_FILING, rows, [this.codeIndexField, ...type.indexFields], type.code
      );
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

  populateIndexRowFormFields(digitalFilingType: DigitalFilingTypeModel) {
    let addFormConfig: { [key: string]: any } = {};
    for (const digitalFilingIndexField of digitalFilingType.indexFields) {
      switch (digitalFilingIndexField.dataType) {
        case DataType.NUMBER:
          this.addFormControls.push({
            label: digitalFilingIndexField.label,
            name: `number${digitalFilingIndexField.seqOrder}`,
            type: DataType.NUMBER,
            required: digitalFilingIndexField.required,
          });

          addFormConfig[`number${digitalFilingIndexField.seqOrder}`] = [
            undefined,
            digitalFilingIndexField.required ? [Validators.required] : [],
          ];
          break;
        case DataType.DATE:
          this.addFormControls.push({
            label: digitalFilingIndexField.label,
            name: `date${digitalFilingIndexField.seqOrder}`,
            type: DataType.DATE,
            required: digitalFilingIndexField.required,
          });

          addFormConfig[`date${digitalFilingIndexField.seqOrder}`] = [
            undefined,
            digitalFilingIndexField.required ? [Validators.required] : [],
          ];
          break;
        default:
          this.addFormControls.push({
            label: digitalFilingIndexField.label,
            name: `text${digitalFilingIndexField.seqOrder}`,
            type: DataType.TEXT,
            required: digitalFilingIndexField.required,
          });

          const validators = [];
          if (digitalFilingIndexField.required) {
            validators.push(Validators.required);
          }
          if (digitalFilingIndexField.label === 'Attachment Filename') {
            validators.push(filenameValidator);
          }

          addFormConfig[`text${digitalFilingIndexField.seqOrder}`] = [undefined, validators];
          break;
      }
    }
    this.indexRowForm = this.fb.group(addFormConfig);
    this.cd.markForCheck();
  }

  doDownloadIndexRow(type: DigitalFilingTypeModel) {
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

  addIndexRowControl(indexRow: BaseIndexRowModel): void {
    const indexFieldForm = this.fb.group({
      id: [this.formIndexRows.length + 1, [Validators.required]],
      seqOrder: [this.formIndexRows.length + 1, [Validators.required]],
      text1: [indexRow.text1, [Validators.maxLength(255)]],
      number1: [indexRow.number1, [Validators.max(99999999999)]],
      date1: [this.resetHour(indexRow.date1)],
      text2: [indexRow.text2, [Validators.maxLength(255)]],
      number2: [indexRow.number2, [Validators.max(99999999999)]],
      date2: [this.resetHour(indexRow.date2)],
      text3: [indexRow.text3, [Validators.maxLength(255)]],
      number3: [indexRow.number3, [Validators.max(99999999999)]],
      date3: [this.resetHour(indexRow.date3)],
      text4: [indexRow.text4, [Validators.maxLength(255)]],
      number4: [indexRow.number4, [Validators.max(99999999999)]],
      date4: [this.resetHour(indexRow.date4)],
      text5: [indexRow.text5, [Validators.maxLength(255)]],
      number5: [indexRow.number5, [Validators.max(99999999999)]],
      date5: [this.resetHour(indexRow.date5)],
      text6: [indexRow.text6, [Validators.maxLength(255)]],
      number6: [indexRow.number6, [Validators.max(99999999999)]],
      date6: [this.resetHour(indexRow.date6)],
      text7: [indexRow.text7, [Validators.maxLength(255)]],
      number7: [indexRow.number7, [Validators.max(99999999999)]],
      date7: [this.resetHour(indexRow.date7)],
      text8: [indexRow.text8, [Validators.maxLength(255)]],
      number8: [indexRow.number8, [Validators.max(99999999999)]],
      date8: [this.resetHour(indexRow.date8)],
      text9: [indexRow.text9, [Validators.maxLength(255)]],
      number9: [indexRow.number9, [Validators.max(99999999999)]],
      date9: [this.resetHour(indexRow.date9)],
      text10: [indexRow.text10, [Validators.maxLength(255)]],
      number10: [indexRow.number10, [Validators.max(99999999999)]],
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
    const filenames = rowValues.map((row: BaseIndexRowModel) => row.text1);

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
    const filenames = rowValues.map((row: BaseIndexRowModel) => row.text1);

    for (const filename of filenames) {
      if (!attachmentFilenames.includes(filename) && !missingFiles.includes(filename)) {
        missingFiles.push(filename);
      }
    }

    return missingFiles;
  }

  doCloseRowFileModal() {
    this.isRowFileModalVisible = false;
  }

  toggleSftp(): void {
    const sftp = this.form.get('sftp')?.value;
    const sftpPathControl = this.form.get('sftpPath');
    const indexRowsControl = this.form.get('indexRows');
    if (sftp) {
      this.isSftp = true;
      indexRowsControl?.disable();
      sftpPathControl?.enable();
    } else {
      this.isSftp = false;
      indexRowsControl?.enable();
      sftpPathControl?.disable();
    }
  }

  doCloseSftpErrorModal() {
    this.isSftpErrorModalVisible = false;
  }

  getIndexDataStepTitle(isSftp: boolean) {
    return isSftp ? 'Index Data Settings': 'Upload Index Data';
  }

  protected readonly Object = Object;
}
