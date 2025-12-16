import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit } from "@angular/core";
import { AbstractControl, UntypedFormArray, UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators } from "@angular/forms";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { NzMessageService } from "ng-zorro-antd/message";
import {
  GetContactFields,
  ResetContactFields,
  UpdateContactFields
} from "../../../../states/contact-field/contact-field.state-actions";
import { Observable, of, switchMap, tap } from "rxjs";
import {
  ContactFieldModel,
  DataType,
  getErrorMessage,
  Privilege,
  resolveErrorMessage,
  updateAndMarkControlAsDirty,
  UserAuthorityModel
} from "@grabbill/lib";
import { ContactFieldState } from "../../../../states/contact-field/contact-field.state";
import { noWhitespaceValidator } from "../../../../utils/no-whitespace-validator";
import { doDeleteIndex } from "../../../../utils/manage-form-array";
import { NzModalService } from "ng-zorro-antd/modal";
import { ShowMessage } from "../../../../states/common/common.state-actions";
import { BaseComponent } from "../../../components/base.component";
import { QueryContacts } from "../../../../states/contact/contact.state-actions";
import { camelCase } from "lodash";
import { hasPrivilege } from "../../../../utils/has-privilege";
import { AuthState } from "../../../../states/auth/auth.state";

@Component({
  selector: 'grabbill-client-contact-field-list',
  templateUrl: './contact-field-list.component.html',
  styleUrls: ['./contact-field-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContactFieldListComponent extends BaseComponent implements OnInit {
  @Input()
  store!: Store;

  @Input()
  messageService!: NzMessageService;

  @Select(ContactFieldState.contactFields)
  contactFields$!: Observable<ContactFieldModel[]>;

  form: UntypedFormGroup;
  isLoading = false;
  emailControl = new UntypedFormControl('checked');

  privilege = Privilege;
  user?: UserAuthorityModel;

  constructor(
    public actions$: Actions,
    private modal: NzModalService,
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef
  ) {
    super();

    this.form = this.fb.group({
      contactFields: this.fb.array([]),
    });

    this.emailControl.setValue(true);
  }

  get contactFields() {
    return this.form.controls['contactFields'] as UntypedFormArray;
  }

  getDataTypeOptions() {
    return [DataType.TEXT, DataType.NUMBER, DataType.DATE];
  }

  ngOnInit(): void {
    this.user = this.store.selectSnapshot(AuthState.user);
    this.store.dispatch(new ResetContactFields());

    this.autoUnsubscribe(
      this.contactFields$.pipe(
        tap((fields) => {
          this.isLoading = false;

          this.contactFields.clear();

          fields.map((field) => {
            this.addField(field);
          });

          this.cd.markForCheck();
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetContactFields),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateContactFields),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Contact fields updated`));
            this.store.dispatch(new QueryContacts());
          }
          return of(false);
        })
      )
    );

    this.isLoading = true;
    this.store.dispatch(new GetContactFields());
  }

  addField(field?: ContactFieldModel): void {
    const fieldForm = this.fb.group({
      id: [field ? field.id : null],
      seqOrder: [field ? field.seqOrder : this.contactFields.controls.length + 1, [Validators.required]],
      label: [field ? field.label : '', [Validators.required, Validators.maxLength(255)]],
      name: [field ? field.name : '', [Validators.required, noWhitespaceValidator, Validators.maxLength(255)]],
      required: [field ? field.required : false, [Validators.required]],
      dataType: [field ? field.dataType : DataType.TEXT],
      referenced: [field ? field.referenced : false, [Validators.required]],
    });
    if(!this.hasPrivilege(Privilege.CONTACT_FIELD_EDIT)) {
      fieldForm.disable();
    }
    this.contactFields.push(fieldForm);
    this.cd.markForCheck();
  }

  doDeleteIndex(fieldForm: AbstractControl, formArray: UntypedFormArray, index: number): void {
    if (fieldForm.get('referenced')!.value) {
      this.modal.confirm({
        nzTitle: `Delete Contact Field`,
        nzContent: 'Deleting a contact field also deletes all data in your list associated with it. Are you sure?',
        nzOkText: 'Yes',
        nzOkType: 'primary',
        nzOkDanger: true,
        nzOnOk: () => {
          this.cd.markForCheck();
          this.store.dispatch(new UpdateContactFields({ contactFieldRequests: this.form.getRawValue().contactFields }));
        },
        nzCancelText: 'No',
      });
    } else {
      doDeleteIndex(formArray, index, this.cd);
    }
  }

  doUpdateFields() {
    if (this.form.valid) {
      this.modal.confirm({
        nzTitle: `Update Contact Fields`,
        nzOkText: 'Yes',
        nzOkType: 'primary',
        nzOkDanger: true,
        nzOnOk: () => {
          this.cd.markForCheck();
          this.store.dispatch(new UpdateContactFields({ contactFieldRequests: this.form.getRawValue().contactFields }));
        },
        nzCancelText: 'No',
      });
    } else {
      updateAndMarkControlAsDirty(this.form);
    }
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doLabelChange(control: AbstractControl) {
    const name = control.get('name')?.value;
    if (!name || name === '') {
      const label = control.get('label')?.value;
      control.get('name')?.setValue(camelCase(label));
    }
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }
}
