import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { Observable, tap } from 'rxjs';
import { NzMessageService } from 'ng-zorro-antd/message';
import {
  getErrorMessage,
  resolveErrorMessage,
  TwoFactorAuthType,
  updateAndMarkControlAsDirty,
  UserAuthorityModel,
} from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { AuthState } from '../../../../states/auth/auth.state';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import {
  ActivateTwoFactorAuth,
  ChangePassword,
  GenerateQrcode,
  UpdateProfile,
  UpdateTwoFactorAuth,
} from '../../../../states/profile/profile.state-actions';
import { passwordValidator } from '../../../../utils/password-validator';
import { ProfileState } from '../../../../states/profile/profile.state';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import {
  GenerateActivateTwoFactorAuthorizationEmail
} from "../../../../states/auth/auth.state-actions";

@Component({
  selector: 'grabbill-client-profile-detail',
  templateUrl: './profile-detail.component.html',
  styleUrls: ['./profile-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfileDetailComponent extends NgxsBaseComponent {
  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  isProfileFormLoading = false;
  isChangePasswordFormLoading = false;
  profileForm: UntypedFormGroup;
  changePasswordForm: UntypedFormGroup;
  qrcodeImage?: SafeUrl;
  isSetupGoogleAuthModalVisible = false;
  otpForm: UntypedFormGroup;
  isActivateMfaModalVisible = false;
  isOtpFormLoading = false;
  google2FAEnabled: boolean = false;
  email2FAEnabled: boolean = false;
  isEnableEmailMfa: boolean = false;
  email = '';
  isEnableMfa: boolean = false;

  constructor(
    private fb: UntypedFormBuilder,
    protected override store: Store,
    protected override messageService: NzMessageService,
    private actions$: Actions,
    private cd: ChangeDetectorRef,
    private sanitizer: DomSanitizer
  ) {
    super(store, messageService);

    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      role: ['', [Validators.required, Validators.maxLength(255)]],
    });

    this.profileForm.get('email')!.disable();
    this.profileForm.get('role')!.disable();

    this.changePasswordForm = this.fb.group({
      password: ['', [Validators.maxLength(255)]],
      newPassword: ['', [Validators.required, passwordValidator, Validators.maxLength(255)]],
    });

    this.otpForm = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(6)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.user$.pipe(
        tap((user) => {
          this.email = user.email;
          this.profileForm.setValue({
            name: user.name,
            email: user.email,
            role: user.role,
          });
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateProfile),
        tap((data: ActionCompletion) => {
          this.isProfileFormLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', 'Update name successfully'));
            this.cd.markForCheck();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(ChangePassword),
        tap((data: ActionCompletion) => {
          this.isChangePasswordFormLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.changePasswordForm.reset();
            this.store.dispatch(new ShowMessage('info', 'Change password successfully'));
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GenerateQrcode),
        tap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(ProfileState.file);
            const objectUrl = URL.createObjectURL(file as Blob);
            this.qrcodeImage = this.sanitizer.bypassSecurityTrustUrl(objectUrl);
            this.isSetupGoogleAuthModalVisible = true;
            this.cd.markForCheck();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateTwoFactorAuth),
        tap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            if (this.isEnableMfa) {
              if (this.isEnableEmailMfa) {
                this.store.dispatch(new GenerateActivateTwoFactorAuthorizationEmail({ email: this.email }));
              } else {
                this.store.dispatch(new GenerateQrcode());
              }
            }
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GenerateActivateTwoFactorAuthorizationEmail),
        tap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.otpForm.reset();
            this.isActivateMfaModalVisible = true;
            this.cd.markForCheck();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(ActivateTwoFactorAuth),
        tap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.isActivateMfaModalVisible = false;
            this.cd.markForCheck();
          }
        })
      )
    );
  }

  submitProfile(): void {
    this.isProfileFormLoading = true;
    this.cd.markForCheck();

    if (this.profileForm.valid) {
      this.store.dispatch(new UpdateProfile(this.profileForm.value));
    } else {
      updateAndMarkControlAsDirty(this.profileForm);
      this.isProfileFormLoading = false;
      this.cd.markForCheck();
    }
  }

  changePassword(): void {
    this.isChangePasswordFormLoading = true;
    this.cd.markForCheck();

    if (this.changePasswordForm.valid) {
      this.store.dispatch(new ChangePassword(this.changePasswordForm.value));
    } else {
      updateAndMarkControlAsDirty(this.changePasswordForm);
      this.isChangePasswordFormLoading = false;
      this.cd.markForCheck();
    }
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doEnableEmailAuth(googleAuth: boolean) {
    this.google2FAEnabled = googleAuth;
    this.email2FAEnabled = true;
    this.isEnableEmailMfa = true;
    this.isEnableMfa = true;
    return this.store.dispatch(new UpdateTwoFactorAuth({ google2FAEnabled: googleAuth, email2FAEnabled: true }));
  }

  doDisableEmailAuth(googleAuth: boolean) {
    this.isEnableMfa = false;
    return this.store.dispatch(new UpdateTwoFactorAuth({ google2FAEnabled: googleAuth, email2FAEnabled: false }));
  }

  doEnableGoogleAuth(emailAuth: boolean) {
    this.google2FAEnabled = true;
    this.email2FAEnabled = emailAuth;
    this.isEnableEmailMfa = false;
    this.isEnableMfa = true;
    this.store.dispatch(new UpdateTwoFactorAuth({ google2FAEnabled: true, email2FAEnabled: emailAuth }));
  }

  doDisableGoogleAuth(emailAuth: boolean) {
    this.isEnableMfa = false;
    return this.store.dispatch(new UpdateTwoFactorAuth({ google2FAEnabled: false, email2FAEnabled: emailAuth }));
  }

  doActivateMfa() {
    if (this.otpForm.valid) {
      this.store.dispatch(
        new ActivateTwoFactorAuth({
          authType: this.isEnableEmailMfa ? TwoFactorAuthType.EMAIL : TwoFactorAuthType.GOOGLE,
          email: this.email,
          otp: +this.otpForm.get('code')?.value,
        })
      );
    } else {
      updateAndMarkControlAsDirty(this.otpForm);
      this.isOtpFormLoading = false;
      this.cd.markForCheck();
    }
  }

  doGoogleAuthMfaNext() {
    this.isSetupGoogleAuthModalVisible = false;
    this.otpForm.reset();
    this.isActivateMfaModalVisible = true;
  }

  doCloseGoogleAuthModal(): void {
    this.isSetupGoogleAuthModalVisible = false;
  }

  doCloseActivateMfaModal(): void {
    this.isActivateMfaModalVisible = false;
  }
}
