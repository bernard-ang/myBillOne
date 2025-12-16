import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { of, switchMap } from 'rxjs';
import { getErrorMessage, TwoFactorAuthType } from '@grabbill/lib';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { AdminAuthState } from '../../../states/admin-auth/admin-auth.state';
import { GenerateOtpEmail, TwoFactorAuthorization } from '../../../states/admin-auth/admin-auth.state-actions';
import { ShowMessage } from '../../../states/admin-common/admin-common.state-actions';

@Component({
  selector: 'grabbill-admin-two-factor-authentication',
  templateUrl: './admin-two-factor-authentication.component.html',
  styleUrls: ['./admin-two-factor-authentication.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminTwoFactorAuthenticationComponent extends NgxsBaseComponent implements OnInit {
  isLoading = false;
  form: UntypedFormGroup;
  isGoogleAuth = false;
  isEmailAuth = false;
  email = '';

  constructor(
    private fb: UntypedFormBuilder,
    private actions$: Actions,
    private cd: ChangeDetectorRef,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
    this.form = this.fb.group({
      code: [null, [Validators.required, Validators.maxLength(6)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    const user = this.store.selectSnapshot(AdminAuthState.user)!;
    if (!user) {
      this.navigate(['login']);
    } else {
      this.email = user.email;
      if (user.google2FAEnabled) {
        this.isGoogleAuth = true;
      }

      if (user.email2FAEnabled) {
        this.isEmailAuth = true;
        this.store.dispatch(new GenerateOtpEmail({ email: user.email }));
      }

      this.autoUnsubscribe(
        this.actions$.pipe(
          ofActionCompleted(TwoFactorAuthorization),
          switchMap((data: ActionCompletion) => {
            if (data.result.error) {
              this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
              this.isLoading = false;
              this.cd.markForCheck();
            } else if (data.result.successful) {
              return this.navigate(['']);
            }

            return of(false);
          })
        )
      );
    }
  }

  submitForm(): void {
    if (this.form.valid) {
      this.isLoading = true;
      this.cd.markForCheck();
      if (this.isGoogleAuth && this.isEmailAuth) {
        this.store.dispatch(
          new TwoFactorAuthorization({
            authType: TwoFactorAuthType.BOTH,
            email: this.email,
            otp: this.form.getRawValue().code,
          })
        );
      } else if (this.isGoogleAuth) {
        this.store.dispatch(
          new TwoFactorAuthorization({
            authType: TwoFactorAuthType.GOOGLE,
            email: this.email,
            otp: this.form.getRawValue().code,
          })
        );
      } else if (this.isEmailAuth) {
        this.store.dispatch(
          new TwoFactorAuthorization({
            authType: TwoFactorAuthType.EMAIL,
            email: this.email,
            otp: this.form.getRawValue().code,
          })
        );
      }
    } else {
      Object.values(this.form.controls).forEach((control) => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }
  }

  doResendCode() {
    const user = this.store.selectSnapshot(AdminAuthState.user)!;
    this.store.dispatch(new GenerateOtpEmail({ email: user.email }));
  }
}
