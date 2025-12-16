import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { NzMessageService } from 'ng-zorro-antd/message';
import { tap } from 'rxjs';
import { getErrorMessage } from '@grabbill/lib';
import { ForgetPassword } from '../../../states/admin-auth/admin-auth.state-actions';
import { ShowMessage } from '../../../states/admin-common/admin-common.state-actions';

@Component({
  selector: 'grabbill-admin-forget-password',
  templateUrl: './admin-forget-password.component.html',
  styleUrls: ['./admin-forget-password.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminForgetPasswordComponent extends NgxsBaseComponent {
  isLoading = false;
  isForgetPasswordSuccessful = false;
  form: UntypedFormGroup;

  constructor(
    private fb: UntypedFormBuilder,
    private actions$: Actions,
    private cd: ChangeDetectorRef,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
    this.form = this.fb.group({
      email: [null, [Validators.required, Validators.email]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(ForgetPassword),
        tap((data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isForgetPasswordSuccessful = true;
            this.cd.markForCheck();
          }
        })
      )
    );
  }

  submitForm(): void {
    if (this.form.valid) {
      this.isLoading = true;
      this.cd.markForCheck();
      this.store.dispatch(new ForgetPassword(this.form.getRawValue()));
    } else {
      Object.values(this.form.controls).forEach((control) => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }
  }
}
