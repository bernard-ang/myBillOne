import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { ActionCompletion, Actions, ofActionCompleted, Store } from "@ngxs/store";
import { NzMessageService } from "ng-zorro-antd/message";
import { tap } from "rxjs";
import { getErrorMessage, resolveErrorMessage } from "@grabbill/lib";
import { NgxsBaseComponent } from "../ngxs-base.component";
import { ResetPassword } from "../../../states/auth/auth.state-actions";
import { ShowMessage } from "../../../states/common/common.state-actions";
import { passwordValidator } from "../../../utils/password-validator";

@Component({
  selector: 'grabbill-client-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResetPasswordComponent extends NgxsBaseComponent {
  isLoading = false;
  isResetSuccessful = false;
  form: UntypedFormGroup;

  email?: string;
  code?: string;

  constructor(
    private fb: UntypedFormBuilder,
    private actions$: Actions,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
    this.form = this.fb.group({
      newPassword: [null, [Validators.required, Validators.minLength(8), Validators.maxLength(255), passwordValidator]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.email = this.route.snapshot.queryParams['email'];
    this.code = this.route.snapshot.queryParams['code'];

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(ResetPassword),
        tap((data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isResetSuccessful = true;
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
      this.store.dispatch(
        new ResetPassword({
          password: this.form.getRawValue().newPassword,
          email: this.email!,
          code: this.code!,
        })
      );
    } else {
      Object.values(this.form.controls).forEach((control) => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }
}
