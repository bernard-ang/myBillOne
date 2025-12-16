import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  TrackByFunction,
  ViewChild,
} from '@angular/core';
import { SmsApi } from '../../../../api/sms.api';
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
import { finalize, map, Observable, of, switchMap, tap } from 'rxjs';
import {
  BaseIndexFieldModel,
  BaseIndexRowModel,
  ContactFieldModel,
  DashboardStatisticsModel,
  DataType,
  DomainType,
  getErrorMessage,
  makePageable,
  ProcessStatus,
  resolveErrorMessage,
  SmsActivityCreditUsagePayloadModel,
  SmsActivityModel,
  SmsFieldType,
  SmsTypeModel,
  updateAndMarkControlAsDirty,
  UserAuthorityModel,
} from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { IndexRowError, validateIndexRows } from '../../../../utils/validate-index-rows';
import { EmailEditorComponent } from '../../app-common/components/email-editor/email-editor.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { AuthState } from '../../../../states/auth/auth.state';
import { DashboardState } from '../../../../states/dashboard/dashboard.state';
import { SmsState } from '../../../../states/sms/sms.state';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { GetCurrentStatistics } from '../../../../states/dashboard/dashboard.state-actions';
import {
  CountSmsCreditUsage,
  GetSmsActivity,
  GetSmsType,
  ResetSmsActivity,
  UpdateSmsActivity,
} from '../../../../states/sms/sms.state-actions';
import { environment } from '../../../../environments/environment';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { catchError } from 'rxjs/operators';
import { isElementBottomVisible, isElementTopVisible } from '../../../../utils/is-in-viewport';
import { scrollToTargetAdjusted } from '../../../../utils/scroll-to-target-adjusted';
import { NzUploadFile } from 'ng-zorro-antd/upload';
import { getUploadCsvData } from '../../../../utils/get-upload-csv-data';
import { getUploadExcelData } from '../../../../utils/get-upload-excel-data';
import { doDeleteAllIndex } from '../../../../utils/manage-form-array';
import { ContactGroupApi } from '../../../../api/contact-group.api';
import { ContactFieldState } from '../../../../states/contact-field/contact-field.state';
import { observeContactFields } from '../../../../utils/observe-contact-fields';
import { smsContentHelp } from '../../../../utils/sms';
import { GetContactFields } from '../../../../states/contact-field/contact-field.state-actions';
import { countBytes, removePlaceholders } from '../../../../utils/count-bytes';
import { malaysiaMobileNoValidator } from "../../../../utils/phone-regex";

export const makeNameValidator = (smsApi: SmsApi, typeId: number, originalName: string): AsyncValidatorFn => {
  return (control: AbstractControl): Observable<ValidationErrors> => {
    const value = control.value;
    if (value === originalName) {
      return of({});
    }

    if (!/^[a-z0-9-: ]+$/i.test(value)) {
      return of({ alphanumericWithSpaceDashColonOnly: value });
    }

    return smsApi
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
  selector: 'grabbill-client-sms-type-edit-activity-detail',
  templateUrl: './sms-type-edit-activity-detail.component.html',
  styleUrls: ['./sms-type-edit-activity-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SmsTypeEditActivityDetailComponent extends NgxsBaseComponent {
  form: UntypedFormGroup;
  typeId?: number;
  availableFields: string[] = [];
  activityId?: number;
  isLoading = true;
  canUploadIndexRow = true;

  searchIndexRowFilename?: string;
  isIndexRowFormVisible = false;
  isIndexRowFormLoading = false;
  indexRowForm: UntypedFormGroup;
  addFormControls: AddFormControl[] = [];
  indexRows: BaseIndexRowModel[] = [];
  indexRowErrors: IndexRowError[] = [];
  isIndexRowErrorModalVisible = false;

  isScheduleModalVisible = false;
  scheduleForm: UntypedFormGroup;

  index = 1;
  lastScrollTop = 0;
  isScrolling = false;

  user?: UserAuthorityModel;
  currentStatistics?: DashboardStatisticsModel;

  nzFilterOption = (): boolean => true;
  listOfOption: Array<{ value: string; text: string }> = [];

  isContactGroupLoading = false;
  smsFieldType = SmsFieldType;
  contactFields: BaseIndexFieldModel[] = [];

  isSmsContactFieldType = true;

  smsContentHelp = smsContentHelp;

  @ViewChild('editor') editor!: ElementRef<EmailEditorComponent>;
  @ViewChild('step2') step2!: ElementRef<HTMLDivElement>;
  @ViewChild('step3') step3!: ElementRef<HTMLDivElement>;

  trackByFn: TrackByFunction<{
    label: string;
    idx: number;
    value: any;
  }> = (_, result) => `${result.label}-${result.idx}`;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  @Select(DashboardState.currentStatistics)
  currentStatistics$!: Observable<DashboardStatisticsModel>;

  @Select(SmsState.smsType)
  smsType$!: Observable<SmsTypeModel>;

  @Select(SmsState.smsActivityCreditUsage)
  smsActivityCreditUsage$!: Observable<SmsActivityCreditUsagePayloadModel>;

  @Select(SmsState.smsActivity)
  smsActivity$!: Observable<SmsActivityModel>;

  @Select(ContactFieldState.contactFields)
  contactFields$!: Observable<ContactFieldModel[]>;

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router,
    private actions$: Actions,
    private modal: NzModalService,
    private smsApi: SmsApi,
    protected override store: Store,
    protected override messageService: NzMessageService,
    private contactGroupApi: ContactGroupApi
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      smsContent: ['', [Validators.required, Validators.maxLength(255)]],
      contactGroup: [''],
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
    this.store.dispatch(new ResetSmsActivity());
    this.store.dispatch(new GetContactFields());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.activityId = params['activityId'];
          this.store.dispatch(new GetSmsType(this.typeId!));
          this.store.dispatch(new GetSmsActivity(this.typeId!, this.activityId!));
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
      this.smsType$.pipe(
        tap((smsType) => {
          if (smsType) {
            if (smsType.smsFieldType === SmsFieldType.INDEX_FIELD) {
              this.isSmsContactFieldType = false;
              this.availableFields = smsType.indexFields
                .filter((value) => value.applicable)
                .map((value) => value.header);
            }
            this.populateIndexRowFormFields(smsType);
          }
        })
      ),
      this.smsActivity$.pipe(
        tap((smsActivity) => {
          if (smsActivity) {
            this.form
              .get('name')!
              .addAsyncValidators(makeNameValidator(this.smsApi, Number(this.typeId), smsActivity.name));
            this.form.reset({
              name: smsActivity.name,
              smsContent: smsActivity.smsContent.replace('RM0 ', ''),
              contactGroup: smsActivity.contactGroupId ? smsActivity.contactGroupId.toString() : 'all',
              indexRows: [],
            });

            for (const row of smsActivity.indexRows) {
              this.addIndexRowControl(row);
            }

            this.search(smsActivity.contactGroupName ? smsActivity.contactGroupName : '');

            this.store.dispatch(
              new CountSmsCreditUsage(
                this.typeId!,
                this.activityId!,
                smsActivity.smsContent,
                smsActivity.contactGroupId ? smsActivity.contactGroupId.toString() : undefined,
                smsActivity.indexRows
              )
            );
          }
        })
      ),
      observeContactFields(this.contactFields$).pipe(
        tap((fields) => {
          this.contactFields = fields;
          if (this.isSmsContactFieldType) {
            this.availableFields = fields.filter((value) => value.applicable).map((value) => value.header);
          }
        })
      ),
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
        ofActionCompleted(GetSmsActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'sms', 'detail', this.typeId!], { tab: 'activities' });
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateSmsActivity),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          const status = data.action.request.status;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity updated`));
            if (status === ProcessStatus.DRAFT) {
              this.navigate(['/', 'sms', 'detail', this.typeId!], { tab: 'activities' });
            } else {
              this.navigate(['/', 'sms', 'detail', this.typeId!, 'activity', this.activityId!], {
                tab: 'info',
              });
            }
          }

          this.cd.markForCheck();
          return of(false);
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
      this.navigate(['/', 'sms', 'detail', this.typeId, 'edit']);
    } else if (index === 1) {
      scrollToTargetAdjusted(this.step2.nativeElement);
    } else if (index === 2) {
      scrollToTargetAdjusted(this.step3.nativeElement);
    }
    this.isScrolling = false;
  }

  doSubmitForm(isDraft: boolean, type: SmsTypeModel, scheduledTimestamp?: Date): void {
    if (this.form.valid) {
      if (!isDraft) {
        const mismatchMergeFields = this.getMismatchMergeFields();
        if (mismatchMergeFields.length > 0) {
          this.displayMismatchFieldErrorModal(mismatchMergeFields);
          return;
        }

        if (type.smsFieldType === SmsFieldType.INDEX_FIELD && this.formIndexRows.value.length === 0) {
          this.modal.error({
            nzTitle: 'Invalid Index Data',
            nzContent: 'Minimum 1 index row is required',
          });
          return;
        }
      }

      const value = this.form.getRawValue();
      value.scheduledTimestamp = scheduledTimestamp;

      const updatedindexRows = [];
      const currentindexRows = value.indexRows;
      for (let i = 0; i < currentindexRows.length; i++) {
        const row = currentindexRows[i];
        row.seqNo = i + 1;
        updatedindexRows.push(row);
      }

      /*if (
        this.currentStatistics!.smsSent + updatedindexRows.length >
        this.user!.subscription!.smsSize
      ) {
        this.modal.error({
          nzTitle: 'Maximum Email Reached',
          nzContent: 'Update plan to send more email.',
        });
        return;
      }*/

      if (!isDraft) {
        this.modal.confirm({
          nzTitle: scheduledTimestamp ? 'Schedule SMS' : 'Send SMS',
          nzOkText: 'Yes',
          nzOkType: 'primary',
          nzOkDanger: true,
          nzOnOk: () => {
            this.dispatchUpdateSmsActivity(value, isDraft, currentindexRows);
          },
          nzCancelText: 'No',
        });
      } else {
        this.dispatchUpdateSmsActivity(value, isDraft, currentindexRows);
      }
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  private dispatchUpdateSmsActivity(value: any, isDraft: boolean, indexRows: BaseIndexRowModel[]) {
    const user = this.store.selectSnapshot(AuthState.user);
    this.store.dispatch(
      new UpdateSmsActivity(this.typeId!, this.activityId!, {
        name: value.name,
        smsFrom: user?.account.companyContactNo || '',
        smsContent: value.smsContent,
        contactGroupId: value.contactGroup === 'all' ? undefined : Number(value.contactGroup),
        scheduledTimestamp: value.scheduledTimestamp,
        status: isDraft ? ProcessStatus.DRAFT : ProcessStatus.SUBMITTED,
        indexRows,
      })
    );
  }

  getIndexFields(type: SmsTypeModel): BaseIndexFieldModel[] {
    return type.indexFields.filter((field) => field.applicable);
  }

  getIndexFieldLabels(type: SmsTypeModel) {
    return this.getIndexFields(type).map((field) => field.label);
  }

  getIndexRowValues(
    control: AbstractControl,
    smsType: SmsTypeModel,
    idx: number
  ): {
    label: string;
    idx: number;
    value: any;
  }[] {
    return smsType.indexFields
      .filter((field) => field.applicable)
      .map((field) => {
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

  handleDataChange() {
    const smsContent = this.form.get('smsContent')?.value;
    const contactGroupId = this.form.get('contactGroup')?.value;
    const indexRowValue = this.formIndexRows.value;

    this.store.dispatch(
      new CountSmsCreditUsage(this.typeId!, this.activityId!, smsContent, contactGroupId, indexRowValue)
    );
  }

  doDeleteIndexRow(name: string, formArray: UntypedFormArray, seqOrder: number): void {
    this.modal.confirm({
      nzTitle: `Delete ${name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        for (let i = 0; i < formArray.length; i++) {
          const smsIndexRow = formArray.at(i);
          const currentSeqOrder = smsIndexRow.get('seqOrder')!;
          if (currentSeqOrder.value === seqOrder) {
            formArray.removeAt(i);
          }
        }
        for (let i = 0; i < formArray.length; i++) {
          const smsIndexRow = formArray.at(i);
          const currentSeqOrder = smsIndexRow.get('seqOrder')!;
          currentSeqOrder.setValue(i + 1);
        }

        this.handleDataChange();

        this.cd.markForCheck();
      },
      nzCancelText: 'No',
    });
  }

  beforeUploadIndexRow =
    (type: SmsTypeModel) =>
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

  private handleUploadData(type: SmsTypeModel) {
    return (rows: BaseIndexRowModel[]) => {
      this.indexRows = rows;
      const indexRowErrors = validateIndexRows(DomainType.SMS, rows, type.indexFields);
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
        this.handleDataChange();
      }
      this.canUploadIndexRow = true;
      this.cd.markForCheck();
    };
  }

  populateIndexRowFormFields(smsType: SmsTypeModel) {
    let addFormConfig: { [key: string]: any } = {};
    for (const smsIndexField of smsType.indexFields) {
      if (smsIndexField.applicable) {
        switch (smsIndexField.dataType) {
          case DataType.NUMBER:
            this.addFormControls.push({
              label: smsIndexField.label,
              name: `number${smsIndexField.seqOrder}`,
              type: DataType.NUMBER,
              required: smsIndexField.required,
            });

            addFormConfig[`number${smsIndexField.seqOrder}`] = [
              undefined,
              smsIndexField.required ? [Validators.required] : [],
            ];
            break;
          case DataType.DATE:
            this.addFormControls.push({
              label: smsIndexField.label,
              name: `date${smsIndexField.seqOrder}`,
              type: DataType.DATE,
              required: smsIndexField.required,
            });

            addFormConfig[`date${smsIndexField.seqOrder}`] = [
              undefined,
              smsIndexField.required ? [Validators.required] : [],
            ];
            break;
          case DataType.EMAIL:
            this.addFormControls.push({
              label: smsIndexField.label,
              name: `text${smsIndexField.seqOrder}`,
              type: DataType.EMAIL,
              required: smsIndexField.required,
            });

            addFormConfig[`text${smsIndexField.seqOrder}`] = [
              undefined,
              smsIndexField.required ? [Validators.required, Validators.email] : [Validators.email],
            ];
            break;
          default:
            this.addFormControls.push({
              label: smsIndexField.label,
              name: `text${smsIndexField.seqOrder}`,
              type: DataType.TEXT,
              required: smsIndexField.required,
            });

            const validators = [];
            if (smsIndexField.required || smsIndexField.label === 'Mobile No') {
              validators.push(Validators.required);
            }
            if (smsIndexField.label === 'Mobile No') {
              validators.push(malaysiaMobileNoValidator());
            }

            addFormConfig[`text${smsIndexField.seqOrder}`] = [undefined, validators];
            break;
        }
      }
    }
    this.indexRowForm = this.fb.group(addFormConfig);
    this.cd.markForCheck();
  }

  doSkipInvalidMobileNo(type: SmsTypeModel) {
    this.indexRows = this.indexRows.filter((indexRow) => {
      const email = indexRow['text1'];
      const control = new UntypedFormControl(email, Validators.email);
      return !(control.errors && control.errors['email']);
    });

    const indexRowErrors = validateIndexRows(DomainType.SMS, this.indexRows, type.indexFields, true);
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
      this.handleDataChange();

      this.isIndexRowErrorModalVisible = false;
    }
    this.canUploadIndexRow = true;
    this.cd.markForCheck();
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
      this.handleDataChange();

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

  doOpenScheduleModal() {
    this.isScheduleModalVisible = true;
  }

  doCloseScheduleModal() {
    this.isScheduleModalVisible = false;
  }

  doSchedule(type: SmsTypeModel) {
    if (this.scheduleForm.valid) {
      this.isScheduleModalVisible = false;
      this.doSubmitForm(false, type, this.scheduleForm.getRawValue().scheduledTimestamp);
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

  private displayMismatchFieldErrorModal(mismatchMergeFields: string[]) {
    this.modal.error({
      nzTitle: 'Mismatch field found',
      nzContent: `Mismatch field [${mismatchMergeFields.join(', ')}] found in email template`,
    });
  }

  getMismatchMergeFields(): string[] {
    const mismatchMergeFields: string[] = [];

    const mergeFieldMatch = this.form.get('smsContent')!.value.matchAll(/{{[\w _-]+}}/g);
    for (const match of mergeFieldMatch) {
      const field = match[0].replace('{{', '').replace('}}', '');
      if (!this.availableFields.includes(field)) {
        mismatchMergeFields.push(field);
      }
    }

    return mismatchMergeFields;
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

  byteCount(value: string): number {
    const text = 'RM0 ' + removePlaceholders(value);
    return countBytes(text.trim());
  }
}
