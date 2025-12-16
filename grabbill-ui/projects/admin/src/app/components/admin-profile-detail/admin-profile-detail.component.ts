import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { Observable, tap } from 'rxjs';
import {
  getErrorMessage,
  resolveErrorMessage,
  TwoFactorAuthType,
  updateAndMarkControlAsDirty,
  UserAuthorityModel,
} from '@grabbill/lib';
import { AdminAuthState } from '../../../states/admin-auth/admin-auth.state';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import {
  ActivateTwoFactorAuth,
  GenerateQrcode,
  UpdateTwoFactorAuth,
} from '../../../states/admin-profile/admin-profile.state-actions';
import { ShowMessage } from '../../../states/admin-common/admin-common.state-actions';
import { AdminProfileState } from '../../../states/admin-profile/admin-profile.state';
import {
  GenerateActivateTwoFactorAuthorizationEmail
} from "../../../states/admin-auth/admin-auth.state-actions";

@Component({
  selector: 'grabbill-admin-profile-detail',
  templateUrl: './admin-profile-detail.component.html',
  styleUrls: ['./admin-profile-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminProfileDetailComponent extends NgxsBaseComponent {
  @Select(AdminAuthState.user)
  user$!: Observable<UserAuthorityModel>;

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
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GenerateQrcode),
        tap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const file = this.store.selectSnapshot(AdminProfileState.file);
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
