import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { of, switchMap } from 'rxjs';
import { NzMessageService } from 'ng-zorro-antd/message';
import { getErrorMessage, TwoFactorAuthType } from '@grabbill/lib';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { ShowMessage } from '../../../states/common/common.state-actions';
import { AuthState } from '../../../states/auth/auth.state';
import { GenerateOtpEmail, TwoFactorAuthorization } from '../../../states/auth/auth.state-actions';
import { verifyUserState } from '../../../utils/verify-user-state';

@Component({
  selector: 'grabbill-client-two-factor-authentication',
  templateUrl: './two-factor-authentication.component.html',
  styleUrls: ['./two-factor-authentication.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TwoFactorAuthenticationComponent extends NgxsBaseComponent implements OnInit {
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

    const user = this.store.selectSnapshot(AuthState.user)!;
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
              const user = this.store.selectSnapshot(AuthState.user)!;
              const redirectPaths = verifyUserState(user, true);
              if (redirectPaths.paths.length > 0) {
                return this.navigate(redirectPaths.paths, redirectPaths.queryParams);
              }
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
    const user = this.store.selectSnapshot(AuthState.user)!;
    this.store.dispatch(new GenerateOtpEmail({ email: user.email }));
  }
}
