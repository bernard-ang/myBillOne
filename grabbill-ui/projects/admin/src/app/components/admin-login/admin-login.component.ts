import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { of, switchMap } from 'rxjs';
import { getErrorMessage, resolveErrorMessage, updateAndMarkControlAsDirty, UserType } from '@grabbill/lib';
import { Login } from '../../../states/admin-auth/admin-auth.state-actions';
import { ShowMessage } from '../../../states/admin-common/admin-common.state-actions';
import { AdminAuthState } from '../../../states/admin-auth/admin-auth.state';

@Component({
  selector: 'grabbill-admin-login',
  templateUrl: './admin-login.component.html',
  styleUrls: ['./admin-login.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminLoginComponent extends NgxsBaseComponent {
  isLoading = false;
  form: UntypedFormGroup;

  constructor(
    private fb: UntypedFormBuilder,
    protected override store: Store,
    protected override messageService: NzMessageService,
    private actions$: Actions,
    private cd: ChangeDetectorRef
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      email: [null, [Validators.required]],
      password: [null, [Validators.required]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(Login),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const user = this.store.selectSnapshot(AdminAuthState.user)!;
            if (user.email2FAEnabled || user.google2FAEnabled) {
              return this.navigate(['/', 'two-factor-auth']);
            }

            return this.navigate(['']);
          }

          return of(false);
        })
      )
    );
  }

  login(): void {
    this.isLoading = true;
    this.cd.markForCheck();

    if (this.form.valid) {
      const value = this.form.getRawValue();
      this.store.dispatch(new Login({ email: value.email, password: value.password, userType: UserType.ADMIN }));
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }
}
