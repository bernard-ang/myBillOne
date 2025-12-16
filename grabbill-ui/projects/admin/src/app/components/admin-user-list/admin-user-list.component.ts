import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { BehaviorSubject, debounceTime, Observable, of, switchMap, tap } from 'rxjs';
import {
  getErrorMessage,
  PageableModel,
  resolveErrorMessage,
  SearchResultPayloadModel,
  updateAndMarkControlAsDirty,
  UserModel,
} from '@grabbill/lib';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import produce from 'immer';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { AdminUserState } from '../../../states/admin-user/admin-user.state';
import {
  ActivateUser,
  DeactivateUser,
  DeleteUser,
  NewUser,
  QueryUsers,
  ResetUsers,
  UpdateUser,
} from '../../../states/admin-user/admin-user.state-actions';
import { ShowMessage } from '../../../states/admin-common/admin-common.state-actions';
import { environment } from '../../../environments/environment';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'grabbill-admin-user-list',
  templateUrl: './admin-user-list.component.html',
  styleUrls: ['./admin-user-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminUserListComponent extends NgxsBaseComponent {
  @Select(AdminUserState.userResult)
  userResult$!: Observable<SearchResultPayloadModel<UserModel>>;

  @Select(AdminUserState.userPageable)
  userPageable$!: Observable<PageableModel>;

  isInitialize = false;
  isTableLoading = false;
  searchChange$ = new BehaviorSubject('');

  isCreateFormLoading = false;
  isCreateFormVisible = false;
  isEditFormLoading = false;
  isEditFormVisible = false;

  createForm: UntypedFormGroup;
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
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      password: ['', [Validators.required, Validators.maxLength(255)]],
    });

    this.editForm = this.fb.group({
      id: ['', [Validators.required]],
      name: ['', [Validators.required, Validators.maxLength(255)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      password: ['', [Validators.maxLength(255)]],
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
        ofActionCompleted(DeactivateUser),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Admin user deactivated`));
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
            this.store.dispatch(new ShowMessage('info', `Admin user activated`));
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
            this.store.dispatch(new ShowMessage('info', `Admin user deleted`));
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

            this.store.dispatch(new ShowMessage('info', `New admin user added`));
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
            this.store.dispatch(new ShowMessage('info', `Admin user updated`));
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
                  ...this.store.selectSnapshot(AdminUserState.userPageable),
                  page: 1,
                },
                name
              )
            );
          })
        )
    );
  }

  dateTimeFormat() {
    return environment.config.dateTimeFormat;
  }

  doQuery(event: NzTableQueryParams) {
    this.isTableLoading = true;
    const pageable = produce(this.store.selectSnapshot(AdminUserState.userPageable), (draft) => {
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
      nzTitle: `Delete admin user ${user.name}`,
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

  doDeactivateUser(user: UserModel) {
    this.store.dispatch(new DeactivateUser(user.id));
  }

  doOpenCreateForm() {
    this.createForm.reset();
    this.isCreateFormVisible = true;
    this.cd.markForCheck();
  }

  doCreateUser() {
    this.isCreateFormLoading = true;
    this.cd.markForCheck();

    if (this.createForm.valid) {
      const value = this.createForm.getRawValue();
      this.store.dispatch(new NewUser({ email: value.email, name: value.name, password: value.password }));
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
    this.editForm.reset({ id: user.id, email: user.email, name: user.name });
    this.isEditFormVisible = true;
    this.cd.markForCheck();
  }

  doUpdateUser() {
    this.isEditFormLoading = true;
    this.cd.markForCheck();

    if (this.editForm.valid) {
      const value = this.editForm.getRawValue();
      if (value.password) {
        this.store.dispatch(
          new UpdateUser(value.id, { email: value.email, name: value.name, password: value.password })
        );
      } else {
        this.store.dispatch(new UpdateUser(value.id, { email: value.email, name: value.name }));
      }
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
}
