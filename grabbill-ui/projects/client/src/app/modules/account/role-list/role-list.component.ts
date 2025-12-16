import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { RoleState } from '../../../../states/role/role.state';
import { Observable, of, switchMap } from 'rxjs';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { DeleteRole, GetRolesWithPrivilege, NewRole, UpdateRole } from '../../../../states/role/role.state-actions';
import {
  getErrorMessage,
  Privilege,
  PrivilegeModel,
  resolveErrorMessage,
  RoleModel,
  updateAndMarkControlAsDirty,
} from '@grabbill/lib';
import { ShowMessage } from '../../../../states/common/common.state-actions';

interface CheckboxData {
  label: string;
  value: number;
  disabled?: boolean;
  checked?: boolean;
}

@Component({
  selector: 'grabbill-client-role-list',
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleListComponent extends NgxsBaseComponent {
  @Select(RoleState.rolesWithPrivilege)
  roles$!: Observable<RoleModel[]>;

  @Select(RoleState.privileges)
  privileges$!: Observable<PrivilegeModel[]>;

  isCreateFormVisible = false;
  isCreateFormLoading = false;
  createForm: UntypedFormGroup;

  isEditFormVisible = false;
  isEditFormLoading = false;
  editForm: UntypedFormGroup;

  constructor(
    private fb: UntypedFormBuilder,
    protected override store: Store,
    protected override messageService: NzMessageService,
    private modal: NzModalService,
    private actions$: Actions,
    private cd: ChangeDetectorRef
  ) {
    super(store, messageService);
    this.createForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      privileges: [[]],
    });

    this.editForm = this.fb.group({
      id: ['', [Validators.required]],
      name: ['', [Validators.required, Validators.maxLength(255)]],
      privileges: [[]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new GetRolesWithPrivilege());

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(NewRole),
        switchMap((data: ActionCompletion) => {
          this.isCreateFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isCreateFormVisible = false;

            this.store.dispatch(new ShowMessage('info', `New role added`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateRole),
        switchMap((data: ActionCompletion) => {
          this.isEditFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isEditFormVisible = false;

            this.store.dispatch(new ShowMessage('info', `Role updated`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteRole),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Role deleted`));
          }

          return of(false);
        })
      ),
    );
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  closeCreateForm() {
    this.isCreateFormVisible = false;
  }

  doOpenCreateModal(privileges: PrivilegeModel[]) {
    this.createForm.reset({
      name: null,
      privileges: privileges.map((item) =>
        item.name === Privilege.MANAGE_PROFILE || item.name === Privilege.DASHBOARD
          ? { label: item.name, value: item.id, disabled: true, checked: true }
          : { label: item.name, value: item.id }
      ),
    });
    this.isCreateFormVisible = true;
  }

  doCreateRole() {
    this.isCreateFormLoading = true;
    this.cd.markForCheck();

    if (this.createForm.valid) {
      const value = this.createForm.getRawValue();
      this.store.dispatch(
        new NewRole({
          name: value.name.toLocaleUpperCase(),
          privilegeIds: value.privileges
            .filter((item: CheckboxData) => item.checked)
            .map((item: CheckboxData) => item.value),
        })
      );
    } else {
      updateAndMarkControlAsDirty(this.createForm);
      this.isCreateFormLoading = false;
      this.cd.markForCheck();
    }
  }

  doOpenEditModal(privileges: PrivilegeModel[], role: RoleModel) {
    const previousSelectedPrivilegeIds = role.privileges.map((item) => item.id);
    this.editForm.reset({
      id: role.id,
      name: role.name,
      privileges: privileges.map((item) =>
        item.name === Privilege.MANAGE_PROFILE || item.name === Privilege.DASHBOARD
          ? { label: item.name, value: item.id, disabled: true, checked: true }
          : { label: item.name, value: item.id, checked: previousSelectedPrivilegeIds.includes(item.id) }
      ),
    });
    this.isEditFormVisible = true;
  }

  closeEditForm() {
    this.isEditFormVisible = false;
  }

  doUpdateRole() {
    this.isEditFormLoading = true;
    this.cd.markForCheck();

    if (this.editForm.valid) {
      const value = this.editForm.getRawValue();
      this.store.dispatch(
        new UpdateRole(value.id, {
          name: value.name.toLocaleUpperCase(),
          privilegeIds: value.privileges
            .filter((item: CheckboxData) => item.checked)
            .map((item: CheckboxData) => item.value),
        })
      );
    } else {
      updateAndMarkControlAsDirty(this.editForm);
      this.isEditFormLoading = false;
      this.cd.markForCheck();
    }
  }

  doDeleteRole(role: RoleModel) {
    this.modal.confirm({
      nzTitle: `Delete role ${role.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeleteRole(role.id));
      },
      nzCancelText: 'No',
    });
  }
}
