import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ContactState } from '../../../../states/contact/contact.state';
import { Observable, of, switchMap, tap } from 'rxjs';
import {
  ContactFieldModel,
  ContactModel,
  ContactRequestModel,
  getErrorMessage, Privilege,
  UserAuthorityModel
} from "@grabbill/lib";
import { ActivatedRoute, Params } from '@angular/router';
import {
  GetContact,
  ResetContact,
  UpdateContact,
  UpdateContactGroups,
} from '../../../../states/contact/contact.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { environment } from '../../../../environments/environment';
import { GetContactFields } from '../../../../states/contact-field/contact-field.state-actions';
import { ContactFieldState } from '../../../../states/contact-field/contact-field.state';
import { getIndexFieldValue } from '../../../../utils/get-index-row-values';
import { getContactFields } from '../../../../utils/get-contact-fields';
import { FormField } from '../../app-common/components/drawer-form/drawer-form.component';
import { format } from 'date-fns';
import { AuthState } from '../../../../states/auth/auth.state';
import { hasPrivilege } from '../../../../utils/has-privilege';

@Component({
  selector: 'grabbill-client-contact-detail',
  templateUrl: './contact-detail.component.html',
  styleUrls: ['./contact-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContactDetailComponent extends NgxsBaseComponent {
  @Select(ContactState.contact)
  contact$!: Observable<ContactModel>;

  @Select(ContactFieldState.contactFields)
  contactFields$!: Observable<ContactFieldModel[]>;

  user?: UserAuthorityModel;
  id?: number;
  isGroupModalVisible = false;

  isUpdateContactFormVisible = false;
  isFieldInitialize = false;
  contactFields: ContactFieldModel[] = [];
  contactDrawerFields: FormField[] = [];

  privilege = Privilege;

  constructor(
    private route: ActivatedRoute,
    private actions$: Actions,
    private cd: ChangeDetectorRef,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetContact());
    this.store.dispatch(new GetContactFields());

    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.id = params['id'];
          this.store.dispatch(new GetContact(this.id!));
        })
      ),
      this.contactFields$.pipe(
        tap((contactFields) => {
          if (contactFields) {
            this.contactFields = contactFields;
          }
        })
      ),
      this.contact$.pipe(
        tap((contact) => {
          this.contactDrawerFields = getContactFields(this.contactFields!, contact);
          this.cd.markForCheck();
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetContact),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
            this.navigate(['/', 'contact', 'list']);
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateContactGroups),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Contact groups updated`));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateContact),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Contact updated`));
          }
          return of(false);
        })
      )
    );
  }

  getDateFormat() {
    return environment.config.dateFormat;
  }

  getContactFields(contact: ContactModel, contactFields: ContactFieldModel[]): { label: string; value: any }[] {
    const fields = [];
    for (const contactField of contactFields) {
      fields.push({
        label: contactField.label,
        value: getIndexFieldValue(contactField, contact),
      });
    }

    return fields;
  }

  doOpenUpdateGroupForm() {
    this.isGroupModalVisible = true;
  }

  doCloseForm() {
    this.isGroupModalVisible = false;
  }

  doUpdateGroup(groups: number[]) {
    this.store.dispatch(new UpdateContactGroups(this.id!, { groups }));
  }

  doOpenContactForm() {
    this.isUpdateContactFormVisible = true;
  }

  closeContactForm() {
    this.isUpdateContactFormVisible = false;
  }

  doUpdateContact(request: ContactRequestModel) {
    this.store.dispatch(new UpdateContact(this.id!, request));
  }

  getStatusText(contact: ContactModel): string {
    if (contact.lastModifiedDate != null) {
      const lastModifiedDate = new Date(contact.lastModifiedDate);
      return `Last modified at ${format(lastModifiedDate, environment.config.dateFormat)} by ${
        contact.lastModifiedBy === this.user?.email ? 'me' : contact.lastModifiedBy
      }`;
    }
    if (contact.createdDate != null) {
      const createdDate = new Date(contact.createdDate);
      return `Last created at ${format(createdDate, environment.config.dateFormat)} by ${
        contact.createdBy === this.user?.email ? 'me' : contact.createdBy
      }`;
    }

    return '';
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }
}
