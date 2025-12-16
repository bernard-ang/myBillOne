import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { format } from 'date-fns';
import { saveAs } from 'file-saver';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import { Observable, of, switchMap, tap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import {
  BaseIndexFieldModel,
  BaseIndexRowModel,
  BaseTypeModel,
  ContactFieldModel,
  DataType,
  DomainType,
  getErrorMessage,
  PageableModel,
  SearchResultPayloadModel,
  SmsFieldType,
  SmsTypeModel,
} from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { SearchState } from '../../../../states/search/search.state';
import { DownloadFile, QueryTypeIndexRecords } from '../../../../states/search/search.state-actions';
import { getContactFieldLabels, getIndexFieldLabels } from '../../../../utils/get-index-field-labels';
import { getIndexFields, getIndexFieldsRowValues, getIndexRowValues } from "../../../../utils/get-index-row-values";

@Component({
  selector: 'grabbill-client-index-row-filter-table',
  templateUrl: './index-row-filter-table.component.html',
  styleUrls: ['./index-row-filter-table.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IndexRowFilterTableComponent extends NgxsBaseComponent {
  _type!: BaseTypeModel;

  @Input('domainType')
  domainType!: DomainType;

  @Input('contactFields')
  contactFields!: ContactFieldModel[];

  @Input('type')
  set type(type: BaseTypeModel) {
    this._type = type;
    this.store.dispatch(
      new QueryTypeIndexRecords(
        this.domainType,
        this.type.id,
        this.store.selectSnapshot(SearchState.indexRowPageable),
        this.store.selectSnapshot(SearchState.indexRowFilters)
      )
    );
  }

  get type() {
    return this._type;
  }

  @Input('pageable')
  pageable!: PageableModel | null;

  @Input('loading')
  loading!: boolean;

  @Output() loadingChange = new EventEmitter<boolean>();

  @Select(SearchState.indexRowPageable)
  indexRowPageable$!: Observable<PageableModel>;

  @Select(SearchState.indexRowSearchResult)
  indexRowSearchResult$!: Observable<SearchResultPayloadModel<BaseIndexRowModel>>;

  isFilterModalVisible = false;
  filterForm: UntypedFormGroup;
  fileFilters: { [index: string]: any } = {};
  attachmentFilenameFieldIndex = -1;
  attachmentPasswordFieldIndex = -1;
  showPassword = false;

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private actions$: Actions,
    protected override store: Store
  ) {
    super(store);
    this.filterForm = this.fb.group({
      field: ['', [Validators.required]],
      type: [''],
      textValue: [undefined, [Validators.required]],
      numberValue: [undefined, [Validators.required]],
      dateValue: [undefined, [Validators.required]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    const applicableFields = this.getApplicableFields(this.type);
    for (let i = 0; i < applicableFields.length; i++) {
      const indexField = applicableFields[i];
      if (indexField.label === 'Attachment Filename') {
        this.attachmentFilenameFieldIndex = i;
      }
      if (indexField.label === 'Attachment Password') {
        this.attachmentPasswordFieldIndex = i;
      }
    }

    this.autoUnsubscribe(
      this.indexRowSearchResult$.pipe(
        tap(() => {
          this.loadingChange.emit(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DownloadFile),
        switchMap((data: ActionCompletion) => {
          this.store.dispatch(new SetPageLoading(false));
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(SearchState.file)!;
            saveAs(file.blob, file.name);
          }
          return of(false);
        })
      )
    );
  }

  getIndexFieldValue(value: any) {
    if (value instanceof Date) {
      return format(value, 'dd/MM/yyyy');
    }
    return value;
  }

  getIndexFieldLabels(type: BaseTypeModel) {
    if (
      this.domainType === DomainType.EMAIL_CAMPAIGN ||
      (this.domainType === DomainType.SMS && (type as SmsTypeModel).smsFieldType === SmsFieldType.CONTACT_FIELD)
    ) {
      return getContactFieldLabels(this.contactFields);
    } else {
      return getIndexFieldLabels(type);
    }
  }

  getIndexRowValues(row: BaseIndexRowModel, type: BaseTypeModel) {
    if (
      this.domainType === DomainType.EMAIL_CAMPAIGN ||
      (this.domainType === DomainType.SMS && (type as SmsTypeModel).smsFieldType === SmsFieldType.CONTACT_FIELD)
    ) {
      return getIndexFields(row, this.getContactIndexFields());
    } else {
      return getIndexFields(row, type.indexFields);
    }
  }

  doDownloadFile(row: BaseIndexRowModel) {
    this.store.dispatch(new SetPageLoading(true));
    this.store.dispatch(
      new DownloadFile(this.domainType, this.type.id, row.activityId, row.file?.id!, row.file?.name!)
    );
  }

  doOpenFilterModal(type: BaseTypeModel) {
    this.filterForm.reset();
    this.filterForm.get('field')!.setValue('idxf1');
    this.updateFilterType(type, 'idxf1');
    this.isFilterModalVisible = true;
    this.cd.markForCheck();
  }

  doCloseFilterModal() {
    this.isFilterModalVisible = false;
    this.cd.markForCheck();
  }

  getIndexFieldByFieldName(type: BaseTypeModel, fieldName: string, indexFields: BaseIndexFieldModel[]) {
    const idx = parseInt(fieldName.replace('idxf', ''));
    if (this.domainType === DomainType.EMAIL_CAMPAIGN ||
      (this.domainType === DomainType.SMS && (type as SmsTypeModel).smsFieldType === SmsFieldType.CONTACT_FIELD)) {
      return this.getContactIndexFields()[idx - 1];
    } else {
      return indexFields[idx - 1];
    }
  }

  updateFilterType(type: BaseTypeModel, fieldName: string) {
    const indexField =
      this.domainType === DomainType.EMAIL_CAMPAIGN ||
      (this.domainType === DomainType.SMS && (type as SmsTypeModel).smsFieldType === SmsFieldType.CONTACT_FIELD)
        ? this.getIndexFieldByFieldName(type, fieldName, this.getContactIndexFields())
        : this.getIndexFieldByFieldName(type, fieldName, type.indexFields);
    const value = indexField.dataType;
    this.filterForm.get('type')!.setValue(value);
    this.cd.markForCheck();
  }

  doFilterIndexFieldChange(type: BaseTypeModel, value: string) {
    this.updateFilterType(type, value);
  }

  doQueryIndexRow(event: NzTableQueryParams, pageable: PageableModel) {
    this.loadingChange.emit(true);
    const updatedPageable = {
      ...pageable!,
      page: event.pageIndex,
    };
    this.store.dispatch(
      new QueryTypeIndexRecords(
        this.domainType,
        this.type.id,
        updatedPageable,
        this.store.selectSnapshot(SearchState.indexRowFilters)
      )
    );
  }

  doAddFilter() {
    this.loadingChange.emit(true);
    let updateFilters = { ...this.fileFilters };
    const field = this.filterForm.get('field')!.value;
    const type = this.filterForm.get('type')!.value;
    if (type === DataType.TEXT || type === DataType.EMAIL) {
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
      new QueryTypeIndexRecords(
        this.domainType,
        this.type.id,
        {
          ...this.store.selectSnapshot(SearchState.indexRowPageable),
          page: 0,
        },
        this.fileFilters
      )
    );
  }

  getFilters() {
    return Object.entries(this.fileFilters);
  }

  getApplicableFields(type: BaseTypeModel): BaseIndexFieldModel[] {
    if (
      this.domainType === DomainType.EMAIL_CAMPAIGN ||
      (this.domainType === DomainType.SMS && (type as SmsTypeModel).smsFieldType === SmsFieldType.CONTACT_FIELD)
    ) {
      return this.getContactIndexFields();
    } else {
      return type.indexFields;
    }
  }

  doRemoveFilter(filter: string) {
    this.loadingChange.emit(true);
    let updateFilters: { [index: string]: any } = {};
    for (const entry of Object.entries(this.fileFilters)) {
      if (entry[0] !== filter) {
        updateFilters[entry[0]] = entry[1];
      }
    }
    this.fileFilters = updateFilters;

    this.store.dispatch(
      new QueryTypeIndexRecords(
        this.domainType,
        this.type.id,
        this.store.selectSnapshot(SearchState.indexRowPageable),
        this.fileFilters
      )
    );
  }

  togglePasswordVisible() {
    this.showPassword = !this.showPassword;
    this.cd.markForCheck();
  }

  getContactIndexFields() {
    return this.contactFields.map((field) => ({ ...field, applicable: true, header: field.name }));
  }
}
