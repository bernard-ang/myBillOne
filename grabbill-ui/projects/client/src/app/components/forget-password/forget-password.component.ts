import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { tap } from 'rxjs';
import { getErrorMessage } from '@grabbill/lib';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { ForgetPassword } from '../../../states/auth/auth.state-actions';
import { ShowMessage } from '../../../states/common/common.state-actions';

@Component({
  selector: 'grabbill-client-forget-password',
  templateUrl: './forget-password.component.html',
  styleUrls: ['./forget-password.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForgetPasswordComponent extends NgxsBaseComponent {
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
      email: [null, [Validators.required, Validators.email, Validators.maxLength(255)]],
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
