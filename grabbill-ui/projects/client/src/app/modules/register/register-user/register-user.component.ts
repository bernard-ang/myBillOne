import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { of, switchMap } from 'rxjs';
import { getErrorMessage, resolveErrorMessage, updateAndMarkControlAsDirty } from '@grabbill/lib';
import { SetPageLoading, ShowMessage } from '../../../../states/common/common.state-actions';
import { Register } from '../../../../states/account/account.state-actions';
import { passwordValidator } from '../../../../utils/password-validator';
import { getAuth, GoogleAuthProvider, signInWithPopup } from 'firebase/auth';
import { environment } from '../../../../environments/environment';
import { initializeApp } from 'firebase/app';
import { Authorize } from '../../../../states/auth/auth.state-actions';
import { AuthState } from '../../../../states/auth/auth.state';
import { verifyUserState } from '../../../../utils/verify-user-state';

@Component({
  selector: 'grabbill-client-register-user',
  templateUrl: './register-user.component.html',
  styleUrls: ['./register-user.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterUserComponent extends NgxsBaseComponent {
  isLoading = false;
  isGoogleLoading = false;
  form: UntypedFormGroup;
  email?: string;

  constructor(
    private fb: UntypedFormBuilder,
    protected override store: Store,
    protected override messageService: NzMessageService,
    private actions$: Actions,
    private cd: ChangeDetectorRef
  ) {
    super(store, messageService);
    this.form = this.fb.group({
      email: [null, [Validators.required, Validators.email, Validators.maxLength(255)]],
      password: [null, [Validators.required, Validators.minLength(8), passwordValidator, Validators.maxLength(255)]],
      term: [false],
    });
  }

  get environment() {
    return environment.config;
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(Register),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            return this.navigate(['/', 'register', 'verify-email'], { email: this.email });
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(Authorize),
        switchMap((data: ActionCompletion) => {
          this.isGoogleLoading = false;
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
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

  submitForm(): void {
    if (!this.form.valid) {
      updateAndMarkControlAsDirty(this.form);
      return;
    }

    const { email, password, term } = this.form.value;

    if (!term) {
      this.store.dispatch(new ShowMessage('error', 'Please agree to terms and privacy statement to register account'));
      return;
    }

    this.isLoading = true;
    this.cd.markForCheck();

    const name = email.substring(0, email.indexOf('@'));
    this.email = email;
    this.store.dispatch(new Register({ name, email, password }));
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
