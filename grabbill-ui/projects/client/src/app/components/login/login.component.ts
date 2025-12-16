import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { of, switchMap } from 'rxjs';
import { getErrorMessage, resolveErrorMessage, updateAndMarkControlAsDirty, UserType } from '@grabbill/lib';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { Authorize, Login } from '../../../states/auth/auth.state-actions';
import { SetPageLoading, ShowMessage } from '../../../states/common/common.state-actions';
import { AuthState } from '../../../states/auth/auth.state';
import { verifyUserState } from '../../../utils/verify-user-state';
import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider, signInWithPopup } from 'firebase/auth';
import { environment } from '../../../environments/environment';
import { isSaasMode } from "../../../utils/deployment-mode";

@Component({
  selector: 'grabbill-client-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent extends NgxsBaseComponent {
  isLoading = false;
  isGoogleLoading = false;
  form: UntypedFormGroup;
  isSaas = isSaasMode();

  constructor(
    private fb: UntypedFormBuilder,
    protected override store: Store,
    protected override messageService: NzMessageService,
    private actions$: Actions,
    private cd: ChangeDetectorRef
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      email: [null, [Validators.required, Validators.maxLength(255)]],
      password: [null, [Validators.required, Validators.maxLength(255)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(Login, Authorize),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          this.isGoogleLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            const user = this.store.selectSnapshot(AuthState.user)!;
            if (user.email2FAEnabled || user.google2FAEnabled) {
              return this.navigate(['/', 'two-factor-auth']);
            }

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

  login(): void {
    this.isLoading = true;
    this.cd.markForCheck();

    if (this.form.valid) {
      const value = this.form.getRawValue();
      this.store.dispatch(new Login({ email: value.email, password: value.password, userType: UserType.CLIENT }));
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }

  doGoogleLogin(): void {
    this.isGoogleLoading = true;
    this.cd.markForCheck();

    const provider = new GoogleAuthProvider();
    const firebaseConfig = environment.config.firebase;
    initializeApp(firebaseConfig);

    const auth = getAuth();

    this.store.dispatch(new SetPageLoading(true));
    signInWithPopup(auth, provider)
      .then(() => {
        auth.currentUser?.getIdToken().then((idToken) => {
          this.store.dispatch(new Authorize({ authProvider: 'google', token: idToken }));
        });
      })
      .catch((error) => {
        this.store.dispatch(new ShowMessage('error', `[${error.code}] ${error.message}`));
        this.isGoogleLoading = false;
        this.cd.markForCheck();
      })
      .finally(() => {
        this.store.dispatch(new SetPageLoading(false));
      });
  }
}
