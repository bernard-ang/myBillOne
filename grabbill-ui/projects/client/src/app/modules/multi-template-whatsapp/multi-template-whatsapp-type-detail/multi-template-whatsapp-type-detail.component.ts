import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import {
  BaseIndexRowModel,
  DataType,
  getErrorMessage,
  MultiTemplateWhatsappTypeModel,
  PageableModel,
  Privilege,
  ProcessStatus,
  SearchResultPayloadModel,
  UserAuthorityModel,
  WhatsAppActivityBasicModel, WhatsappTemplateModel, WhatsappTemplateStatus
} from "@grabbill/lib";
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import { ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { WhatsAppState } from '../../../../states/whatsapp/whatsapp.state';
import { AuthState } from '../../../../states/auth/auth.state';
import {
  DeleteMultiTemplateWhatsAppActivity,
  DeleteMultiTemplateWhatsAppFile,
  DownloadMultiTemplateWhatsAppFile,
  ExportMultiTemplateWhatsAppActivity,
  GetMultiTemplateWhatsAppType, GetWhatsAppTemplates,
  LoadMoreMultiTemplateWhatsAppActivities,
  PurgeMultiTemplateWhatsAppActivity,
  QueryMultiTemplateWhatsAppActivities,
  QueryMultiTemplateWhatsAppTypeFiles,
  ResetMultiTemplateWhatsAppType
} from "../../../../states/whatsapp/whatsapp.state-actions";
import { NzMessageModule, NzMessageService } from 'ng-zorro-antd/message';
import { ActivatedRoute, Params, RouterModule } from '@angular/router';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { environment } from '../../../../environments/environment';
import { filter } from 'rxjs/operators';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { createMultiTemplateWhatsApp, handleMultiTemplateNewWhatsApp } from '../../../../utils/whatsapp';
import { saveAs } from 'file-saver';
import { getCsvSeparatorLabel } from '../../../../utils/csv-separator';
import { format } from 'date-fns';
import { NzTableModule, NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { AppCommonModule } from '../../app-common/app-common.module';
import { CommonModule } from '@angular/common';
import { getIndexFieldLabels } from '../../../../utils/get-index-field-labels';
import { getCode } from '../../../../utils/get-code';
import { getStatusTag } from '../../../../utils/get-status-tag';
import { getIndexRowValues } from '../../../../utils/get-index-row-values';
import { hasPrivilege } from 'projects/client/src/utils/has-privilege';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzTabsModule } from 'ng-zorro-antd/tabs';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { IconsProviderModule } from '../../../icons-provider.module';
import {
  getWhatsAppTemplateBody, getWhatsAppTemplateButton,
  getWhatsAppTemplateButtonUrl, getWhatsAppTemplateFooter,
  getWhatsAppTemplateHeader
} from "../../../../utils/whatsapp-template";

@Component({
  selector: 'grabbill-client-multi-template-whatsapp-type-detail',
  standalone: true,
  imports: [
    AppCommonModule,
    CommonModule,
    ReactiveFormsModule,
    NzSkeletonModule,
    NzMessageModule,
    NzButtonModule,
    NzTableModule,
    NzTabsModule,
    NzTagModule,
    NzListModule,
    NzCardModule,
    NzModalModule,
    NzFormModule,
    NzSelectModule,
    NzInputNumberModule,
    NzDatePickerModule,
    RouterModule,
    NzInputModule,
    IconsProviderModule,
  ],
  templateUrl: './multi-template-whatsapp-type-detail.component.html',
  styleUrl: './multi-template-whatsapp-type-detail.component.less',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MultiTemplateWhatsappTypeDetailComponent extends NgxsBaseComponent {
  typeId?: number;
  user?: UserAuthorityModel;

  isInitialize = false;
  isFileTableLoading = false;

  isFilterModalVisible = false;
  filenameSearchChange$ = new BehaviorSubject('');
  filterForm: UntypedFormGroup;
  fileFilters: { [index: string]: any } = {};
  showPassword = false;
  attachmentFieldIndex = -1;
  passwordFieldIndex = -1;

  @Select(WhatsAppState.multiTemplateWhatsAppType)
  multiTemplateWhatsAppType$!: Observable<MultiTemplateWhatsappTypeModel>;

  @Select(WhatsAppState.multiTemplateWhatsAppActivityPageable)
  multiTemplateWhatsAppActivityPageable$!: Observable<PageableModel>;

  @Select(WhatsAppState.multiTemplateWhatsAppActivitySearchResult)
  multiTemplateWhatsAppActivitySearchResult$!: Observable<SearchResultPayloadModel<WhatsAppActivityBasicModel>>;

  @Select(WhatsAppState.whatsAppFilePageable)
  whatsAppFilePageable$!: Observable<PageableModel>;

  @Select(WhatsAppState.whatsAppFileSearchResult)
  fileSearchResult$!: Observable<SearchResultPayloadModel<BaseIndexRowModel>>;

  @Select(WhatsAppState.templates)
  whatsAppTemplates$!: Observable<WhatsappTemplateModel[]>;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  isListLoading = false;
  listQueryAction = QueryMultiTemplateWhatsAppActivities;
  listLoadMoreAction = LoadMoreMultiTemplateWhatsAppActivities;
  listState = WhatsAppState;
  whatsAppTemplates?: WhatsappTemplateModel[];

  constructor(
    protected override messageService: NzMessageService,
    public override store: Store,
    public actions$: Actions,
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private modal: NzModalService
  ) {
    super(store, messageService);
    this.filterForm = this.fb.group({
      field: ['', [Validators.required]],
      type: [''],
      textValue: [undefined, [Validators.required, Validators.maxLength(255)]],
      numberValue: [undefined, [Validators.required, Validators.max(99999999999)]],
      dateValue: [undefined, [Validators.required]],
    });
  }

  public get privilege(): typeof Privilege {
    return Privilege;
  }

  public get dateFormat(): string {
    return environment.config.dateFormat;
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetMultiTemplateWhatsAppType());
    this.store.dispatch(new GetWhatsAppTemplates());

    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.typeId = params['id'];
          this.store.dispatch(new GetMultiTemplateWhatsAppType(this.typeId!));
          this.store.dispatch(new QueryMultiTemplateWhatsAppActivities(this.typeId!));
        })
      ),
      this.multiTemplateWhatsAppType$.pipe(
        tap((type) => {
          if (type) {
            this.isInitialize = true;
            const indexFieldLabels = getIndexFieldLabels(type);
            for (let i = 0; i < indexFieldLabels.length; i++) {
              const indexFieldLabel = indexFieldLabels[i];
              if (indexFieldLabel === 'Attachment Filename') {
                this.attachmentFieldIndex = i;
              }

              if (indexFieldLabel === 'Attachment Password') {
                this.passwordFieldIndex = i;
              }
            }
            this.cd.markForCheck();
          }
        })
      ),
      this.whatsAppTemplates$.pipe(
        tap((whatsAppTemplates) => {
          if(whatsAppTemplates) {
            this.whatsAppTemplates = whatsAppTemplates;
          }
        })
      ),
      this.fileSearchResult$.pipe(
        tap(() => {
          this.isFileTableLoading = false;
        })
      ),
      this.filenameSearchChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((name: string) => {
            this.isFileTableLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new QueryMultiTemplateWhatsAppTypeFiles(
                this.typeId!,
                this.store.selectSnapshot(WhatsAppState.whatsAppFilePageable),
                name,
                this.store.selectSnapshot(WhatsAppState.fileFilters)
              )
            );
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
        ofActionCompleted(QueryMultiTemplateWhatsAppTypeFiles),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.isFileTableLoading = false;
          }
          return of(false);
        })
      ),
      handleMultiTemplateNewWhatsApp(this.actions$, this.store, this.navigate.bind(this)),
      this.actions$.pipe(
        ofActionCompleted(ExportMultiTemplateWhatsAppActivity, DownloadMultiTemplateWhatsAppFile),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(WhatsAppState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteMultiTemplateWhatsAppActivity),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity deleted`));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(PurgeMultiTemplateWhatsAppActivity, DeleteMultiTemplateWhatsAppFile),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Activity file(s) deleted`));
            this.store.dispatch(new QueryMultiTemplateWhatsAppActivities(this.typeId!));
            this.store.dispatch(new QueryMultiTemplateWhatsAppTypeFiles(this.typeId!));
          }
          return of(false);
        })
      )
    );
  }

  getCode(whatsAppType: MultiTemplateWhatsappTypeModel): string {
    return getCode(whatsAppType.code);
  }

  getStatusTag(activity: WhatsAppActivityBasicModel): string {
    return getStatusTag(activity.status);
  }

  getSeparatorLabel(csvSeparator: string): string {
    return getCsvSeparatorLabel(csvSeparator);
  }

  doEdit(typeId: number) {
    this.navigate(['/', 'mt-whatsapp', 'detail', typeId, 'edit']);
  }

  doViewActivity(item: WhatsAppActivityBasicModel) {
    if (item.status === ProcessStatus.DRAFT) {
      this.doEditActivity(this.typeId!, item.id);
    } else {
      this.navigate(['/', 'mt-whatsapp', 'detail', this.typeId!, 'activity', item.id], { tab: 'records' });
    }
  }

  doEditActivity(typeId: number, activityId: number) {
    this.navigate(['/', 'mt-whatsapp', 'detail', typeId, 'activity', activityId, 'edit']);
  }

  doCreateWhatsApp(type: MultiTemplateWhatsappTypeModel, whatsAppName: string) {
    createMultiTemplateWhatsApp(this.store, type, whatsAppName);
  }

  doDeleteActivity(typeId: number, activity: WhatsAppActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${activity.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteMultiTemplateWhatsAppActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  doExportActivity(typeId: number, activity: WhatsAppActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Export ${activity.name} Files`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new SetPageLoading(true));
        this.store.dispatch(new ExportMultiTemplateWhatsAppActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  doPurgeActivity(typeId: number, activity: WhatsAppActivityBasicModel) {
    this.modal.confirm({
      nzTitle: `Purge ${activity.name} Files`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new PurgeMultiTemplateWhatsAppActivity(typeId, activity.id));
      },
      nzCancelText: 'No',
    });
  }

  getIndexFieldLabels(whatsAppType: MultiTemplateWhatsappTypeModel) {
    return getIndexFieldLabels(whatsAppType);
  }

  getIndexRowValues(row: BaseIndexRowModel, whatsAppType: MultiTemplateWhatsappTypeModel) {
    return getIndexRowValues(row, whatsAppType);
  }

  getActivityStatusText(whatsAppActivity: WhatsAppActivityBasicModel): string {
    if (whatsAppActivity.lastModifiedDate != null) {
      const lastModifiedDate = new Date(whatsAppActivity.lastModifiedDate);
      return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
        whatsAppActivity.lastModifiedBy === this.user?.email ? 'me' : whatsAppActivity.lastModifiedBy
      }`;
    }
    if (whatsAppActivity.createdDate != null) {
      const createdDate = new Date(whatsAppActivity.createdDate);
      return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
        whatsAppActivity.createdBy === this.user?.email ? 'me' : whatsAppActivity.createdBy
      }`;
    }

    return '';
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }

  doSearchFilename(event: any) {
    this.filenameSearchChange$.next(event.target.value);
  }

  doQueryFile(event: NzTableQueryParams) {
    this.isFileTableLoading = true;
    this.cd.markForCheck();
    const pageable = produce(this.store.selectSnapshot(WhatsAppState.whatsAppFilePageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(
      new QueryMultiTemplateWhatsAppTypeFiles(
        this.typeId!,
        pageable,
        this.store.selectSnapshot(WhatsAppState.fileName),
        this.store.selectSnapshot(WhatsAppState.fileFilters)
      )
    );
  }

  doDeleteFile(indexRow: BaseIndexRowModel, whatsAppType: MultiTemplateWhatsappTypeModel) {
    this.modal.confirm({
      nzTitle: `Delete ${indexRow.text1}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isFileTableLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(
          new DeleteMultiTemplateWhatsAppFile(whatsAppType.id, indexRow.activityId, indexRow.file!.id!)
        );
      },
      nzCancelText: 'No',
    });
  }

  doDownloadFile(row: BaseIndexRowModel) {
    this.store.dispatch(new SetPageLoading(true));
    this.store.dispatch(
      new DownloadMultiTemplateWhatsAppFile(this.typeId!, row.activityId, row.file?.id!, row.file?.name!)
    );
  }

  doOpenFilterModal(type: MultiTemplateWhatsappTypeModel) {
    this.filterForm.reset();
    this.filterForm.get('field')!.setValue('idxf2');
    this.updateFilterType(type, 'idxf2');
    this.isFilterModalVisible = true;
    this.cd.markForCheck();
  }

  updateFilterType(type: MultiTemplateWhatsappTypeModel, fieldName: string) {
    const indexField = this.getIndexFieldByFieldName(fieldName, type);
    const value = indexField.dataType;
    this.filterForm.get('type')!.setValue(value);
    this.cd.markForCheck();
  }

  getIndexFieldByFieldName(fieldName: string, type: MultiTemplateWhatsappTypeModel) {
    const idx = parseInt(fieldName.replace('idxf', ''));
    return type.indexFields[idx - 1];
  }

  getIndexFieldValue(value: any) {
    if (value instanceof Date) {
      return format(value, 'dd/MM/yyyy');
    }
    return value;
  }

  doCloseFilterModal() {
    this.isFilterModalVisible = false;
    this.cd.markForCheck();
  }

  doFilterIndexFieldChange(type: MultiTemplateWhatsappTypeModel, value: string) {
    this.updateFilterType(type, value);
  }

  doAddFilter() {
    this.isFileTableLoading = true;
    this.cd.markForCheck();
    let updateFilters = { ...this.fileFilters };
    const field = this.filterForm.get('field')!.value;
    const type = this.filterForm.get('type')!.value;
    if (type === DataType.TEXT) {
      updateFilters[field] = this.filterForm.get('textValue')!.value;
    } else if (type === DataType.NUMBER) {
      updateFilters[field] = this.filterForm.get('numberValue')!.value;
    } else if (type === DataType.DATE) {
      const dateValue = this.filterForm.get('dateValue')!.value;
      const targetDate = new Date(dateValue);
      targetDate.setHours(0, 0, 0, 0);
      updateFilters[field] = targetDate;
    }

    this.fileFilters = updateFilters;

    this.isFilterModalVisible = false;
    this.cd.markForCheck();

    this.store.dispatch(
      new QueryMultiTemplateWhatsAppTypeFiles(
        this.typeId!,
        {
          ...this.store.selectSnapshot(WhatsAppState.whatsAppFilePageable),
          page: 0,
        },
        this.store.selectSnapshot(WhatsAppState.fileName),
        this.fileFilters
      )
    );
  }

  getApplicableFields(type: MultiTemplateWhatsappTypeModel) {
    return type.indexFields.filter((field) => field.applicable);
  }

  getFilters() {
    return Object.entries(this.fileFilters);
  }

  doRemoveFilter(filter: string) {
    this.isFileTableLoading = true;
    this.cd.markForCheck();
    let updateFilters: { [index: string]: any } = {};
    for (const entry of Object.entries(this.fileFilters)) {
      if (entry[0] !== filter) {
        updateFilters[entry[0]] = entry[1];
      }
    }
    this.fileFilters = updateFilters;

    this.store.dispatch(
      new QueryMultiTemplateWhatsAppTypeFiles(
        this.typeId!,
        this.store.selectSnapshot(WhatsAppState.whatsAppFilePageable),
        this.store.selectSnapshot(WhatsAppState.fileName),
        this.fileFilters
      )
    );
  }

  togglePasswordVisible() {
    this.showPassword = !this.showPassword;
    this.cd.markForCheck();
  }

  getWhatsappTemplateHeader(name: string) {
    const template = this.whatsAppTemplates?.find(template => template.name === name);
    return template ? getWhatsAppTemplateHeader(template) : '-';
  }

  getWhatsappTemplateBody(name: string) {
    const template = this.whatsAppTemplates?.find(template => template.name === name);
    return template ? getWhatsAppTemplateBody(template) : '-';
  }

  getWhatsappTemplateFooter(name: string) {
    const template = this.whatsAppTemplates?.find(template => template.name === name);
    return template ? getWhatsAppTemplateFooter(template) : '-';
  }

  getWhatsappTemplateButton(name: string) {
    const template = this.whatsAppTemplates?.find(template => template.name === name);
    return template ? getWhatsAppTemplateButton(template) : '-';
  }
}
