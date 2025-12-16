import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { BehaviorSubject, Observable, of, switchMap, tap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { ContactGroupState } from '../../../../states/contact-group/contact-group.state';
import {
  DeleteContactGroup,
  LoadMoreContactGroups,
  NewContactGroup,
  QueryContactGroups,
  ResetContactGroups,
  UpdateContactGroup,
  UploadContactGroupContact,
} from '../../../../states/contact-group/contact-group.state-actions';
import {
  ContactFieldModel,
  ContactGroupBasicModel,
  ContactRequestModel,
  getErrorMessage,
  PageableModel,
  Privilege,
  resolveErrorMessage,
  SearchResultPayloadModel,
  updateAndMarkControlAsDirty,
  UserAuthorityModel,
} from '@grabbill/lib';
import { hasPrivilege } from '../../../../utils/has-privilege';
import { AuthState } from '../../../../states/auth/auth.state';
import { format } from 'date-fns';
import { environment } from '../../../../environments/environment';
import { NzUploadFile } from 'ng-zorro-antd/upload';
import { emailContactField } from '../../../../utils/default-contact-fields';
import { getUploadContactCsvData } from '../../../../utils/get-upload-contact-csv-data';
import { catchError } from 'rxjs/operators';
import { ContactRowError, validateContactRows } from '../../../../utils/validate-contacts';
import { getUploadContactExcelData } from '../../../../utils/get-upload-contact-excel-data';
import { BaseComponent } from '../../../components/base.component';

@Component({
  selector: 'grabbill-client-contact-group-list',
  templateUrl: './contact-group-list.component.html',
  styleUrls: ['./contact-group-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContactGroupListComponent extends BaseComponent implements OnInit {
  @Input()
  store!: Store;

  @Input()
  messageService!: NzMessageService;

  @Select(ContactGroupState.contactGroupSearchResult)
  contactGroupSearchResult$!: Observable<SearchResultPayloadModel<ContactGroupBasicModel>>;

  @Select(ContactGroupState.contactGroupPageable)
  contactGroupPageable$!: Observable<PageableModel>;

  isInitialize = false;
  isListLoading = false;
  queryAction = QueryContactGroups;
  resetAction = ResetContactGroups;
  loadMoreAction = LoadMoreContactGroups;
  state = ContactGroupState;

  searchChange$ = new BehaviorSubject('');
  sortBy = new UntypedFormControl('name');

  user?: UserAuthorityModel;

  createForm: UntypedFormGroup;
  isCreateFormLoading = false;
  isCreateFormVisible = false;
  editForm: UntypedFormGroup;
  isEditFormLoading = false;
  isEditFormVisible = false;

  canUpload = false;
  isContactRowErrorModalVisible = false;
  contacts: ContactRequestModel[] = [];
  contactRowErrors: ContactRowError[] = [];
  contactGroup?: ContactGroupBasicModel;

  privilege = Privilege;

  constructor(
    private readonly fb: UntypedFormBuilder,
    private readonly cd: ChangeDetectorRef,
    private readonly modal: NzModalService,
    public actions$: Actions
  ) {
    super();

    this.createForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(255)]],
    });

    this.editForm = this.fb.group({
      id: ['', [Validators.required]],
      name: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(255)]],
    });
  }

  ngOnInit(): void {
    this.store.dispatch(new ResetContactGroups());

    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.autoUnsubscribe(
      this.contactGroupSearchResult$.pipe(
        tap(() => {
          this.isListLoading = false;
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteContactGroup),
        switchMap((data: ActionCompletion) => {
          this.isListLoading = false;
          this.cd.markForCheck();
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Contact group deleted`));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewContactGroup),
        switchMap((data: ActionCompletion) => {
          this.isCreateFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isCreateFormVisible = false;

            this.store.dispatch(new ShowMessage('info', `New contact group added`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateContactGroup),
        switchMap((data: ActionCompletion) => {
          this.isEditFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isEditFormVisible = false;
            this.store.dispatch(new ShowMessage('info', `Contact group updated`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UploadContactGroupContact),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const emailUpdated = this.store.selectSnapshot(ContactGroupState.emailsUpdated);
            const invalidEmails = this.store.selectSnapshot(ContactGroupState.invalidEmails);
            if (invalidEmails.length === 0) {
              this.store.dispatch(
                new ShowMessage('info', `${this.contactGroup!.name}'s ${emailUpdated.length} contacts updated`)
              );
            } else {
              this.modal.warning({
                nzTitle: 'Assign Existing Contacts to Group',
                nzContent: `${this.contactGroup!.name}'s ${
                  emailUpdated.length
                } contacts updated.\n\nEmail not available in contacts: \n -${invalidEmails.join('\n -')}`,
              });
            }
          }

          this.cd.markForCheck();
          return of(false);
        })
      )
    );

    this.isInitialize = true;
  }

  doSortChange(value: string) {
    if (this.isInitialize) {
      this.isListLoading = true;
      this.store.dispatch(
        new QueryContactGroups(
          {
            ...this.store.selectSnapshot(ContactGroupState.contactGroupPageable),
            page: 1,
            sort: value,
            direction: value === 'name' ? 'ASC' : 'DESC',
          },
          this.store.selectSnapshot(ContactGroupState.contactGroupName)
        )
      );
    }
  }

  doDelete(contactGroup: ContactGroupBasicModel) {
    this.modal.confirm({
      nzTitle: `Delete ${contactGroup.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.isListLoading = true;
        this.cd.markForCheck();
        this.store.dispatch(new DeleteContactGroup(contactGroup.id));
      },
      nzCancelText: 'No',
    });
  }

  doLoadMore() {
    this.isListLoading = true;
    this.cd.markForCheck();
    this.store.dispatch(new LoadMoreContactGroups());
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }

  getMessage() {
    return (contactGroup: ContactGroupBasicModel): string => {
      if (contactGroup.lastModifiedDate != null) {
        const lastModifiedDate = new Date(contactGroup.lastModifiedDate);
        return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
          contactGroup.lastModifiedBy === this.user?.email ? 'me' : contactGroup.lastModifiedBy
        }`;
      }
      if (contactGroup.createdDate != null) {
        const createdDate = new Date(contactGroup.createdDate);
        return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
          contactGroup.createdBy === this.user?.email ? 'me' : contactGroup.createdBy
        }`;
      }

      return '';
    };
  }

  doOpenCreateForm() {
    this.createForm.reset();
    this.isCreateFormVisible = true;
    this.cd.markForCheck();
  }

  doCreateContactGroup() {
    this.isCreateFormLoading = true;
    this.cd.markForCheck();

    if (this.createForm.valid) {
      const value = this.createForm.getRawValue();
      this.store.dispatch(new NewContactGroup({ name: value.name, description: value.description }));
    } else {
      updateAndMarkControlAsDirty(this.createForm);
      this.isCreateFormLoading = false;
      this.cd.markForCheck();
    }
  }

  closeCreateForm(): void {
    this.isCreateFormVisible = false;
    this.cd.markForCheck();
  }

  doOpenEditForm(contactGroup: ContactGroupBasicModel) {
    this.editForm.reset({ id: contactGroup.id, name: contactGroup.name, description: contactGroup.description });
    this.isEditFormVisible = true;
    this.cd.markForCheck();
  }

  doUpdateContactGroup() {
    this.isEditFormLoading = true;
    this.cd.markForCheck();

    if (this.editForm.valid) {
      const value = this.editForm.getRawValue();
      this.store.dispatch(new UpdateContactGroup(value.id, { name: value.name, description: value.description }));
    } else {
      updateAndMarkControlAsDirty(this.editForm);
      this.isEditFormLoading = false;
      this.cd.markForCheck();
    }
  }

  closeEditForm(): void {
    this.isEditFormVisible = false;
    this.cd.markForCheck();
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  beforeUploadContacts = (contactGroup: ContactGroupBasicModel) => {
    return (file: NzUploadFile): boolean => {
      this.contactGroup = contactGroup;
      if (file instanceof File) {
        this.canUpload = false;

        const fields = [emailContactField];

        if (file.type === 'text/csv') {
          this.autoUnsubscribe(
            getUploadContactCsvData(file, fields).pipe(
              tap(this.handleUploadData(fields)),
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
            getUploadContactExcelData(file, fields).pipe(
              tap(this.handleUploadData(fields)),
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
        }
        this.cd.markForCheck();
      }

      this.cd.markForCheck();
      return false;
    };
  };

  doCloseContactRowErrorModal() {
    this.isContactRowErrorModalVisible = false;
    this.cd.markForCheck();
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
        const requestContacts = contacts.map((contact) => contact.email);
        this.store.dispatch(new UploadContactGroupContact(this.contactGroup!.id, { emails: requestContacts }));
      }
    };
  }
}
