import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from "@angular/core";
import {
  ContactBasicModel,
  ContactFieldModel,
  ContactGroupBasicModel,
  ContactGroupModel,
  ContactRequestModel,
  DataType,
  DuplicateOption,
  getErrorMessage,
  makePageable,
  PageableModel,
  Privilege,
  resolveErrorMessage,
  SearchResultPayloadModel,
  updateAndMarkControlAsDirty,
  UserAuthorityModel,
} from '@grabbill/lib';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { ContactState } from '../../../../states/contact/contact.state';
import { ActivatedRoute } from '@angular/router';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import {
  BulkDeleteContacts,
  DeleteContact,
  NewContact,
  QueryContacts,
  QuerySearchContacts,
  ResetContacts,
  ResetSearchContacts,
  UploadContacts,
} from '../../../../states/contact/contact.state-actions';
import { catchError, filter } from 'rxjs/operators';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { ContactFieldState } from '../../../../states/contact-field/contact-field.state';
import { format } from 'date-fns';
import { FormField } from '../../app-common/components/drawer-form/drawer-form.component';
import { getDataValues } from '../../../../utils/get-index-row-values';
import { FilterField } from '../../app-common/components/modal-fliter-form/modal-filter-form.component';
import { getContactFields, getContactFormDrawerFields } from '../../../../utils/get-contact-fields';
import { ContactGroupState } from '../../../../states/contact-group/contact-group.state';
import { NzUploadFile } from 'ng-zorro-antd/upload';
import { getUploadContactCsvData } from '../../../../utils/get-upload-contact-csv-data';
import { ContactRowError, validateContactRows } from '../../../../utils/validate-contacts';
import { getUploadContactExcelData } from '../../../../utils/get-upload-contact-excel-data';
import { defaultContactFields } from '../../../../utils/default-contact-fields';
import { hasPrivilege } from '../../../../utils/has-privilege';
import { BaseComponent } from '../../../components/base.component';
import { DeleteContactGroup, NewContactGroup } from '../../../../states/contact-group/contact-group.state-actions';
import { isEqual } from 'lodash';
import { AuthState } from "../../../../states/auth/auth.state";

@Component({
  selector: 'grabbill-client-contact-list',
  templateUrl: './contact-list.component.html',
  styleUrls: ['./contact-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContactListComponent extends BaseComponent implements OnInit {
  @Input()
  store!: Store;

  @Input()
  messageService!: NzMessageService;

  user?: UserAuthorityModel;
  isInitialize = false;
  isFieldInitialize = false;
  isGroupInitialize = false;
  isLoading = false;

  isFilterModalVisible = false;
  filterForm: UntypedFormGroup;
  filters: { [index: string]: any } = {};

  isDeleteFilterModalVisible = false;
  deleteFilterForm: UntypedFormGroup;
  deleteFilters: { [index: string]: any } = {};

  privilege = Privilege;

  emailSearchChange$ = new BehaviorSubject('');

  @Select(ContactState.contactPageable)
  contactPageable$!: Observable<PageableModel>;

  @Select(ContactState.contactSearchResult)
  contactSearchResult$!: Observable<SearchResultPayloadModel<ContactBasicModel>>;

  @Select(ContactFieldState.contactFields)
  contactFields$!: Observable<ContactFieldModel[]>;

  @Select(ContactGroupState.contactGroupSearchResult)
  contactGroupSearchResult$!: Observable<SearchResultPayloadModel<ContactGroupBasicModel>>;

  @Select(ContactState.searchContactSearchResult)
  searchContactSearchResult$!: Observable<SearchResultPayloadModel<ContactBasicModel>>;

  @Select(ContactGroupState.isGroupInitialize)
  isGroupInitialize$!: Observable<boolean>;

  @Select(ContactFieldState.isFieldInitialize)
  isFieldInitialize$!: Observable<boolean>;

  isAddContactFormVisible = false;
  contactDrawerFields: FormField[] = [];

  canUpload = false;
  isContactRowErrorModalVisible = false;
  contacts: ContactRequestModel[] = [];
  contactRowErrors: ContactRowError[] = [];

  contactSearchChange$ = new BehaviorSubject('');
  previousContactSearchValue = '';
  contactOptions: ContactBasicModel[] = [];
  isBulkDeleteModalVisible = false;
  isBulkDeleteModalLoading = false;
  isContactLoading = false;
  bulkDeleteForm: UntypedFormGroup;
  filterBulkDeleteContactString = '';
  contactFields: ContactFieldModel[] = [];
  contactGroups: ContactGroupBasicModel[] = [];

  isDuplicateContactModalVisible = false;

  constructor(
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private modal: NzModalService,
    private actions$: Actions
  ) {
    super();
    this.filterForm = this.fb.group({
      field: ['', [Validators.required]],
      type: [''],
      textValue: [undefined, [Validators.required]],
      numberValue: [undefined, [Validators.required]],
      dateValue: [undefined, [Validators.required]],
    });
    this.deleteFilterForm = this.fb.group({
      field: ['', [Validators.required]],
      type: [''],
      textValue: [undefined, [Validators.required]],
      numberValue: [undefined, [Validators.required]],
      dateValue: [undefined, [Validators.required]],
    });
    this.bulkDeleteForm = this.fb.group({
      contacts: [[], [Validators.required]],
      contact: [],
    });
  }

  get bulkDeleteContacts() {
    return this.bulkDeleteForm.get('contacts');
  }

  ngOnInit(): void {
    this.user = this.store.selectSnapshot(AuthState.user);
    this.store.dispatch(new ResetContacts());

    this.autoUnsubscribe(
      this.route.queryParams.pipe(
        tap((params) => {
          if (params['group']) {
            this.filters['group'] = params['group'];
            this.store.dispatch(
              new QueryContacts(
                this.store.selectSnapshot(ContactState.contactPageable),
                this.store.selectSnapshot(ContactState.email),
                this.filters
              )
            );
            this.cd.markForCheck();
          } else {
            delete this.filters['group'];
            this.store.dispatch(
              new QueryContacts(
                this.store.selectSnapshot(ContactState.contactPageable),
                this.store.selectSnapshot(ContactState.email),
                this.filters
              )
            );
            this.cd.markForCheck();
          }
        })
      ),
      this.emailSearchChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((email: string) => {
            this.isLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new QueryContacts(
                this.store.selectSnapshot(ContactState.contactPageable),
                email,
                this.store.selectSnapshot(ContactState.filters)
              )
            );
          })
        ),
      this.actions$.pipe(
        ofActionCompleted(QueryContacts),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          this.isLoading = false;
          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewContact),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Contact added`));
            this.store.dispatch(new QueryContacts());
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteContact),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Contact deleted`));
            this.store.dispatch(new QueryContacts());
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(BulkDeleteContacts),
        switchMap((data: ActionCompletion) => {
          this.isBulkDeleteModalLoading = false;
          this.isBulkDeleteModalVisible = false;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Bulk delete contacts successfully`));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UploadContacts),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const duplicateOption = data.action.request.duplicateOption;
            const skipContacts = this.store.selectSnapshot(ContactState.skipContacts);
            const updatedContacts = this.store.selectSnapshot(ContactState.updatedContacts);
            if (duplicateOption === DuplicateOption.UPDATE_DUPLICATE) {
              this.store.dispatch(new ShowMessage('info', `Upload ${updatedContacts.length} contacts successfully.`));
            } else {
              this.store.dispatch(
                new ShowMessage(
                  'info',
                  `Upload ${updatedContacts.length} contacts successfully, skip ${skipContacts.length} contacts.`
                )
              );
            }
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteContactGroup, NewContactGroup),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
          }
          return of(false);
        })
      ),
      this.contactGroupSearchResult$.pipe(
        tap(() => {
          this.cd.markForCheck();
        })
      ),
      this.contactFields$.pipe(
        tap(() => {
          this.cd.markForCheck();
        })
      ),
      this.isGroupInitialize$.pipe(
        tap((isGroupInitialize) => {
          this.isGroupInitialize = isGroupInitialize;
          this.cd.markForCheck();
        })
      ),
      this.isFieldInitialize$.pipe(
        tap((isFieldInitialize) => {
          this.isFieldInitialize = isFieldInitialize;
          this.cd.markForCheck();
        })
      ),
      this.searchContactSearchResult$.pipe(
        tap((searchResult) => {
          this.contactOptions = searchResult.items;
          this.isContactLoading = false;
          this.cd.markForCheck();
        })
      ),
      this.contactSearchChange$.asObservable().pipe(
        debounceTime(200),
        tap((email) => {
          this.isContactLoading = true;
          this.store.dispatch(new QuerySearchContacts(makePageable(10, 1, 'email', 'asc'), email));
        })
      )
    );
  }

  doSearchEmail(event: any) {
    this.emailSearchChange$.next(event.target.value);
  }

  getFilters() {
    return Object.entries(this.filters);
  }

  getDeleteFilters() {
    return Object.entries(this.deleteFilters);
  }

  doRemoveFilter(filter: string) {
    this.isLoading = true;
    this.cd.markForCheck();

    let updateFilters: { [index: string]: any } = {};
    for (const entry of Object.entries(this.filters)) {
      if (entry[0] !== filter) {
        updateFilters[entry[0]] = entry[1];
      }
    }
    this.filters = updateFilters;

    this.store.dispatch(
      new QueryContacts(
        this.store.selectSnapshot(ContactState.contactPageable),
        this.store.selectSnapshot(ContactState.email),
        this.filters
      )
    );
  }

  doRemoveDeleteFilter(filter: string) {
    this.cd.markForCheck();

    let updateFilters: { [index: string]: any } = {};
    for (const entry of Object.entries(this.deleteFilters)) {
      if (entry[0] !== filter) {
        updateFilters[entry[0]] = entry[1];
      }
    }
    this.deleteFilters = updateFilters;

    this.store.dispatch(
      new QuerySearchContacts(
        this.store.selectSnapshot(ContactState.searchContactPageable),
        this.store.selectSnapshot(ContactState.searchEmail),
        this.deleteFilters
      )
    );
  }

  closeFilterForm() {
    this.isFilterModalVisible = false;
  }

  closeDeleteFilterForm() {
    this.isDeleteFilterModalVisible = false;
  }

  doFilter(filters: { [index: string]: any }) {
    this.filters = filters;

    this.store.dispatch(
      new QueryContacts(
        {
          ...this.store.selectSnapshot(ContactState.contactPageable),
          page: 0,
        },
        this.store.selectSnapshot(ContactState.email),
        this.filters
      )
    );
  }

  doDeleteFilter(filters: { [index: string]: any }) {
    this.deleteFilters = filters;

    this.store.dispatch(
      new QuerySearchContacts(
        {
          ...this.store.selectSnapshot(ContactState.searchContactPageable),
          page: 0,
        },
        this.store.selectSnapshot(ContactState.searchEmail),
        this.deleteFilters
      )
    );
  }

  getFilterLabel(filterFields: FilterField[], fieldName: string) {
    const filterField = filterFields.filter((field) => field.name === fieldName)[0];
    return filterField.label;
  }

  getContactFieldValue(value: any) {
    if (value instanceof Date) {
      return format(value, 'dd/MM/yyyy');
    }
    return value;
  }

  doQueryContact(event: NzTableQueryParams) {
    this.isLoading = true;
    this.cd.markForCheck();
    const pageable = produce(this.store.selectSnapshot(ContactState.contactPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(
      new QueryContacts(
        pageable,
        this.store.selectSnapshot(ContactState.email),
        this.store.selectSnapshot(ContactState.filters)
      )
    );
    this.isInitialize = true;
  }

  doOpenFilterModal() {
    this.isFilterModalVisible = true;
    this.cd.markForCheck();
  }

  doOpenDeleteFilterModal() {
    this.isDeleteFilterModalVisible = true;
    this.cd.markForCheck();
  }

  getContactFilterFields(contactFields: ContactFieldModel[], contactGroups: ContactGroupModel[]) {
    const fields: FilterField[] = [];

    fields.push({
      label: 'Groups',
      name: 'group',
      type: DataType.SELECT,
      required: false,
      options: contactGroups.map((group) => ({ value: group.name, label: group.name })),
    });

    fields.push({
      label: 'Mobile No',
      name: 'mobileNo',
      type: DataType.TEXT,
      required: false,
    });

    for (let i = 0; i < contactFields.length; i++) {
      const contactField = contactFields[i];
      fields.push({
        label: contactField.label,
        name: `ctf${i + 1}`,
        type: contactField.dataType,
        required: false,
      });
    }

    return fields;
  }

  getDataValues = (fields: ContactFieldModel[], row: ContactBasicModel) => {
    return [row.email, row.mobileNo, ...getDataValues(row, fields)];
  };

  openContactForm() {
    this.isAddContactFormVisible = true;
  }

  closeContactForm() {
    this.isAddContactFormVisible = false;
  }

  doAddContact(request: ContactRequestModel) {
    this.store.dispatch(new NewContact(request));
  }

  doDeleteContact(contact: ContactBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${contact.email}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteContact(contact.id));
      },
      nzCancelText: 'No',
    });
  }

  getContactFields(contactFields: ContactFieldModel[]) {
    return getContactFields(contactFields, undefined);
  }

  getContactFormFields(contactFields: ContactFieldModel[], contactGroups: ContactGroupBasicModel[]) {
    if (
      this.isFieldInitialize &&
      this.isGroupInitialize &&
      (!isEqual(this.contactGroups, contactGroups) || !isEqual(this.contactFields, contactFields))
    ) {
      this.contactFields = contactFields;
      this.contactGroups = contactGroups;
      this.contactDrawerFields = getContactFormDrawerFields(contactFields, undefined, contactGroups);
    }
    return this.contactDrawerFields;
  }

  beforeUploadContacts =
    (contactFields: ContactFieldModel[]) =>
    (file: NzUploadFile): boolean => {
      if (file instanceof File) {
        this.canUpload = false;
        const updatedContactFields: ContactFieldModel[] = [...defaultContactFields, ...contactFields];

        if (file.type === 'text/csv') {
          this.autoUnsubscribe(
            getUploadContactCsvData(file, updatedContactFields).pipe(
              tap(this.handleUploadData(updatedContactFields)),
              catchError((err) => {
                this.canUpload = true;
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
            getUploadContactExcelData(file, updatedContactFields).pipe(
              tap(this.handleUploadData(updatedContactFields)),
              catchError((err) => {
                this.canUpload = true;
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
          this.canUpload = true;
          this.store.dispatch(new ShowMessage('error', 'Only Excel or CSV is allowed'));
          return false;
        }
        this.cd.markForCheck();
      }

      this.cd.markForCheck();
      return false;
    };

  doUpdateDuplicateContact() {
    this.store.dispatch(new UploadContacts({ contacts: this.contacts, duplicateOption: DuplicateOption.UPDATE_DUPLICATE }));
    this.canUpload = true;
    this.cd.markForCheck();
    this.cd.markForCheck();
    this.doCloseDuplicateContactModal();
  }

  doSkipDuplicateContact() {
    this.store.dispatch(new UploadContacts({ contacts: this.contacts, duplicateOption: DuplicateOption.SKIP_DUPLICATE }));
    this.canUpload = true;
    this.cd.markForCheck();
    this.doCloseDuplicateContactModal();
  }

  private handleUploadData(contactFields: ContactFieldModel[]) {
    return (contacts: ContactRequestModel[]) => {
      this.contacts = contacts;
      const contactRowErrors = validateContactRows(contacts, contactFields);
      if (contactRowErrors.length > 0) {
        this.contactRowErrors = contactRowErrors;
        this.isContactRowErrorModalVisible = true;
        this.cd.markForCheck();
      } else {
        this.doOpenDuplicateContactModal();
      }
    };
  }

  doCloseContactRowErrorModal() {
    this.isContactRowErrorModalVisible = false;
  }

  doOpenBulkDeleteContactModal() {
    this.bulkDeleteForm.reset({ contacts: [] });
    this.deleteFilters = {};
    this.store.dispatch(new ResetSearchContacts());
    this.store.dispatch(new QuerySearchContacts(makePageable(100000, 1, 'email', 'asc'), undefined));
    this.isBulkDeleteModalVisible = true;
  }

  doBulkDeleteContacts() {
    this.isBulkDeleteModalLoading = true;
    this.cd.markForCheck();

    if (this.bulkDeleteForm.valid) {
      const contacts: ContactBasicModel[] = this.bulkDeleteForm.getRawValue().contacts;
      this.store.dispatch(new BulkDeleteContacts({ contactIds: contacts.map((contact) => contact.id) }));
    } else {
      updateAndMarkControlAsDirty(this.bulkDeleteForm);
      this.isBulkDeleteModalLoading = false;
      this.cd.markForCheck();
    }
  }

  doCloseBulkDeleteModal(): void {
    this.isBulkDeleteModalVisible = false;
    this.cd.markForCheck();
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doSearchContact(value: string) {
    if (this.previousContactSearchValue !== value) {
      this.isContactLoading = true;
      this.previousContactSearchValue = value;
      this.contactSearchChange$.next(value);
      this.cd.markForCheck();
    }
  }

  doRemoveAllDeleteContact() {
    this.bulkDeleteContacts?.setValue([]);
    this.cd.markForCheck();
  }

  doContactChange(contact?: ContactBasicModel | string) {
    this.bulkDeleteForm.get('contact')?.setValue(undefined);
    const selectedBulkDeleteContacts = this.bulkDeleteContacts?.value;

    if (contact === 'add-all') {
      for (const contactOption of this.contactOptions) {
        const filterContact = selectedBulkDeleteContacts.filter(
          (currentContact: ContactBasicModel) => currentContact.email === contactOption.email
        );
        if (contactOption && filterContact.length === 0) {
          selectedBulkDeleteContacts.push(contactOption);
        }
      }
      this.bulkDeleteContacts?.setValue(selectedBulkDeleteContacts);
      this.cd.markForCheck();
    } else if (contact) {
      const filterContact = selectedBulkDeleteContacts.filter(
        (currentContact: ContactBasicModel) => currentContact.email === (contact as ContactBasicModel).email
      );
      if (contact && filterContact.length === 0) {
        selectedBulkDeleteContacts.push(contact);
        this.bulkDeleteContacts?.setValue(selectedBulkDeleteContacts);
      }
      this.cd.markForCheck();
    }
  }

  doDeleteBulkDeleteContact(email: string) {
    const currentContacts: ContactBasicModel[] = this.bulkDeleteContacts?.value;
    this.bulkDeleteContacts?.setValue(currentContacts.filter((contact) => email !== contact.email));
  }

  doFilterContact(event: Event) {
    this.filterBulkDeleteContactString = (event.target as any).value;
    this.cd.markForCheck();
  }

  getFilteredContactList() {
    const currentContacts: ContactBasicModel[] = this.bulkDeleteContacts?.value;
    if (this.filterBulkDeleteContactString.length > 0) {
      return currentContacts.filter((contact) => contact.email.includes(this.filterBulkDeleteContactString));
    } else {
      return currentContacts;
    }
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }

  doOpenDuplicateContactModal() {
    this.isDuplicateContactModalVisible = true;
    this.cd.markForCheck();
  }

  doCloseDuplicateContactModal() {
    this.isDuplicateContactModalVisible = false;
    this.cd.markForCheck();
  }
}
