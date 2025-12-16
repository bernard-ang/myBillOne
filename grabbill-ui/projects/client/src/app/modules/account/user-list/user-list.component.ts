import { ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, ViewChild } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import {
  getErrorMessage,
  PageableModel,
  resolveErrorMessage,
  SearchResultPayloadModel,
  updateAndMarkControlAsDirty,
  UserAuthorityModel,
  UserModel,
} from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { UserState } from '../../../../states/user/user.state';
import {
  ActivateUser,
  DeactivateUser,
  DeleteUser,
  NewUser,
  QueryUsers,
  ResetUserPassword,
  ResetUsers,
  UpdateUser,
} from '../../../../states/user/user.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { GetRoles } from '../../../../states/role/role.state-actions';
import { RoleState } from '../../../../states/role/role.state';
import { filter } from 'rxjs/operators';
import { AuthState } from '../../../../states/auth/auth.state';
import { isSaasMode, SAAS_MODE } from "../../../../utils/deployment-mode";
import { environment } from "../../../../environments/environment";

@Component({
  selector: 'grabbill-client-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserListComponent extends NgxsBaseComponent {
  @Select(RoleState.roles)
  roles$!: Observable<string[]>;

  @Select(UserState.userResult)
  userResult$!: Observable<SearchResultPayloadModel<UserModel>>;

  @Select(UserState.userPageable)
  userPageable$!: Observable<PageableModel>;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  isInitialize = false;
  isTableLoading = false;
  searchChange$ = new BehaviorSubject('');

  deploymentMode = environment.config.deploymentMode;
  isSaasMode = isSaasMode();

  isCreateFormLoading = false;
  isCreateFormVisible = false;
  isEditFormLoading = false;
  isEditFormVisible = false;
  isRoleInfoVisible = false;

  createForm: UntypedFormGroup;
  editForm: UntypedFormGroup;

  createFormCodeInput = new UntypedFormControl('');
  editFormCodeInput = new UntypedFormControl('');
  newCodeInputVisible = false;
  user?: UserAuthorityModel;
  @ViewChild('editFormAddCodeInputElement', { static: false }) editFormAddCodeInputElement?: ElementRef;
  @ViewChild('createFormAddCodeInputElement', { static: false }) createFormAddCodeInputElement?: ElementRef;

  items = [
    {
      item: 'Account',
      owner: 'View, Edit',
      admin: '',
      manager: '',
      author: '',
      viewer: '',
      mode: undefined,
    },
    {
      item: 'Account Usages',
      owner: 'View',
      admin: '',
      manager: '',
      author: '',
      viewer: '',
      mode: undefined,
    },
    {
      item: 'Manage Plan & Credits',
      owner: 'View, Edit',
      admin: '',
      manager: '',
      author: '',
      viewer: '',
      mode: SAAS_MODE,
    },
    {
      item: 'Manage Users',
      owner: 'View, Edit',
      admin: '',
      manager: '',
      author: '',
      viewer: '',
      mode: undefined,
    },
    {
      item: 'Mail Server Settings',
      owner: 'View, Edit',
      admin: 'View, Edit',
      manager: '',
      author: '',
      viewer: '',
      mode: undefined,
    },
    {
      item: 'Images',
      owner: 'View, Create, Edit, Delete',
      admin: 'View, Create, Edit, Delete',
      manager: '',
      author: '',
      viewer: '',
      mode: undefined,
    },
    {
      item: 'Bounced Emails',
      owner: 'View, Delete',
      admin: 'View, Delete',
      manager: '',
      author: '',
      viewer: '',
      mode: undefined,
    },
    {
      item: 'Unsubscribed Emails',
      owner: 'View, Delete',
      admin: 'View, Delete',
      manager: '',
      author: '',
      viewer: '',
      mode: undefined,
    },
    {
      item: 'WhatsApp Settings',
      owner: 'View, Edit',
      admin: '',
      manager: '',
      author: '',
      viewer: '',
      mode: undefined,
    },
    {
      item: 'WhatsApp Received Message',
      owner: 'View, Edit',
      admin: '',
      manager: '',
      author: '',
      viewer: '',
      mode: undefined,
    },
    {
      item: 'Contact',
      owner: 'View, Create, Edit, Delete',
      admin: 'View, Create, Edit, Delete',
      manager: 'View, Create, Edit, Delete',
      author: 'View',
      viewer: 'View',
      mode: undefined,
    },
    {
      item: 'Contact Field',
      owner: 'View, Create, Edit, Delete',
      admin: 'View, Create, Edit, Delete',
      manager: 'View, Create, Edit, Delete',
      author: 'View',
      viewer: 'View',
      mode: undefined,
    },
    {
      item: 'Contact Group',
      owner: 'View, Create, Edit, Delete',
      admin: 'View, Create, Edit, Delete',
      manager: 'View, Create, Edit, Delete',
      author: 'View',
      viewer: 'View',
      mode: undefined,
    },
    {
      item: 'Digital Filing',
      owner: 'View, Create, Edit, Delete',
      admin: 'View, Create, Edit, Delete',
      manager: 'View, Create, Edit, Delete',
      author: 'View',
      viewer: 'View',
      mode: undefined,
    },
    {
      item: 'Digital Filing Activity',
      owner: 'View, Create, Edit, Delete',
      admin: 'View, Create, Edit, Delete',
      manager: 'View, Create, Edit, Delete',
      author: 'View, Create, Edit, Delete',
      viewer: 'View',
      mode: undefined,
    },
    {
      item: 'Transactional Email',
      owner: 'View, Create, Edit, Delete',
      admin: 'View, Create, Edit, Delete',
      manager: 'View, Create, Edit, Delete',
      author: 'View',
      viewer: 'View',
      mode: undefined,
    },
    {
      item: 'Transactional Email Activity',
      owner: 'View, Create, Edit, Delete',
      admin: 'View, Create, Edit, Delete',
      manager: 'View, Create, Edit, Delete',
      author: 'View, Create, Edit, Delete',
      viewer: 'View',
      mode: undefined,
    },
    {
      item: 'Email Campaign',
      owner: 'View, Create, Edit, Delete',
      admin: 'View, Create, Edit, Delete',
      manager: 'View, Create, Edit, Delete',
      author: 'View',
      viewer: 'View',
      mode: undefined,
    },
    {
      item: 'Email Campaign Activity',
      owner: 'View, Create, Edit, Delete',
      admin: 'View, Create, Edit, Delete',
      manager: 'View, Create, Edit, Delete',
      author: 'View, Create, Edit, Delete',
      viewer: 'View',
      mode: undefined,
    },
    {
      item: 'Search Record',
      owner: 'View',
      admin: 'View',
      manager: 'View',
      author: 'View',
      viewer: 'View',
      mode: undefined,
    },
    {
      item: 'Audit',
      owner: 'View',
      admin: 'View',
      manager: '',
      author: '',
      viewer: '',
      mode: undefined,
    },
    {
      item: 'Dashboard',
      owner: 'View',
      admin: 'View',
      manager: 'View',
      author: 'View',
      viewer: 'View',
      mode: undefined,
    },
  ];

  constructor(
    private fb: UntypedFormBuilder,
    protected override store: Store,
    protected override messageService: NzMessageService,
    private modal: NzModalService,
    private actions$: Actions,
    private cd: ChangeDetectorRef
  ) {
    super(store, messageService);
    this.user = this.store.selectSnapshot(AuthState.user)!;

    this.createForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      role: ['', [Validators.required]],
      codes: [[]],
    });

    this.editForm = this.fb.group({
      id: ['', [Validators.required]],
      name: ['', [Validators.required, Validators.maxLength(255)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      role: ['', [Validators.required]],
      codes: [[]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetUsers());

    this.autoUnsubscribe(
      this.userResult$.pipe(
        tap(() => {
          this.isTableLoading = false;
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(QueryUsers),
        switchMap((data: ActionCompletion) => {
          this.isTableLoading = false;
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeactivateUser),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `User deactivated`));
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(ActivateUser),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `User activated`));
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(ResetUserPassword),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `User password reset`));
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(DeleteUser),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `User deleted`));
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(NewUser),
        switchMap((data: ActionCompletion) => {
          this.isCreateFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isCreateFormVisible = false;

            this.store.dispatch(new ShowMessage('info', `New user added`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateUser),
        switchMap((data: ActionCompletion) => {
          this.isEditFormLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isEditFormVisible = false;
            this.store.dispatch(new ShowMessage('info', `User updated`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      ),
      this.searchChange$
        .asObservable()
        .pipe(filter(() => this.isInitialize))
        .pipe(debounceTime(300))
        .pipe(
          switchMap((name: string) => {
            this.isTableLoading = true;
            this.cd.markForCheck();
            return this.store.dispatch(
              new QueryUsers(
                {
                  ...this.store.selectSnapshot(UserState.userPageable),
                  page: 1,
                },
                name
              )
            );
          })
        )
    );

    this.store.dispatch(new GetRoles());
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(UserState.userPageable), (draft) => {
      draft.page = event.pageIndex;
    });
    this.store.dispatch(new QueryUsers(pageable));
    this.isInitialize = true;
  }

  doSearch(event: any) {
    this.isTableLoading = true;
    this.searchChange$.next(event.target.value);
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doDeleteUser(user: UserModel) {
    this.modal.confirm({
      nzTitle: `Delete user ${user.name}`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new DeleteUser(user.id));
      },
      nzCancelText: 'No',
    });
  }

  doActivateUser(user: UserModel) {
    this.store.dispatch(new ActivateUser(user.id));
  }

  doResetPassword(user: UserModel) {
    this.modal.confirm({
      nzTitle: `Reset ${user.name} password`,
      nzOkText: 'Yes',
      nzOkType: 'primary',
      nzOkDanger: true,
      nzOnOk: () => {
        this.store.dispatch(new ResetUserPassword(user.id));
      },
      nzCancelText: 'No',
    });
  }

  doDeactivateUser(user: UserModel) {
    this.store.dispatch(new DeactivateUser(user.id));
  }

  doOpenCreateForm(totalUsers: number, maxUser: number) {
    if (totalUsers + 1 >= maxUser) {
      this.modal.info({
        nzTitle: 'Max Users Reached',
        nzContent: 'Update plan to get more users.',
      });
    } else {
      this.createForm.reset({ role: 'ADMIN', codes: [] });
      this.isCreateFormVisible = true;
      this.cd.markForCheck();
    }
  }

  doCreateUser() {
    this.isCreateFormLoading = true;
    this.cd.markForCheck();

    if (this.createForm.valid) {
      const value = this.createForm.getRawValue();
      this.store.dispatch(new NewUser({ email: value.email, name: value.name, role: value.role, codes: value.codes }));
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

  doOpenEditForm(user: UserModel) {
    this.editForm.reset({
      id: user.id,
      email: user.email,
      name: user.name,
      role: user.role,
      codes: user.codes ? user.codes : [],
    });
    this.isEditFormVisible = true;
    this.cd.markForCheck();
  }

  doUpdateUser() {
    this.isEditFormLoading = true;
    this.cd.markForCheck();

    if (this.editForm.valid) {
      const value = this.editForm.getRawValue();
      this.store.dispatch(
        new UpdateUser(value.id, { email: value.email, name: value.name, role: value.role, codes: value.codes })
      );
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

  getRoles(roles: string[]): string[] {
    return roles.filter((role) => role !== 'OWNER');
  }

  dateFormat() {
    return environment.config.dateFormat;
  }

  handleRemoveCode(form: UntypedFormGroup, removedCode: string): void {
    const codes = form.get('codes')!.value;
    form.get('codes')!.setValue(codes.filter((code: string) => code !== removedCode));
  }

  doAddCode(isCreate: boolean): void {
    this.newCodeInputVisible = true;
    this.cd.markForCheck();
    setTimeout(() => {
      if (isCreate) {
        this.createFormAddCodeInputElement?.nativeElement.focus();
      } else {
        this.editFormAddCodeInputElement?.nativeElement.focus();
      }
    }, 10);
  }

  handleAddCode(form: UntypedFormGroup, input: UntypedFormControl): void {
    const inputValue = input.value;
    const codesControl = form.get('codes')!;
    if (inputValue && codesControl.value.indexOf(inputValue) === -1) {
      codesControl.setValue([...codesControl.value, inputValue]);
    }
    input.setValue('');
    this.newCodeInputVisible = false;
  }

  doOpenRoleInfo(): void {
    this.isRoleInfoVisible = true;
    this.cd.markForCheck();
  }

  handleRoleInfoOk(): void {
    this.isRoleInfoVisible = false;
    this.cd.markForCheck();
  }
}
