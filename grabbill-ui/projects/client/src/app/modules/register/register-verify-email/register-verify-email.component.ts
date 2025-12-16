import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { of, switchMap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { getErrorMessage } from '@grabbill/lib';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { ResendVerifyEmail, VerifyEmail } from '../../../../states/account/account.state-actions';

@Component({
  selector: 'grabbill-client-register-verify-email',
  templateUrl: './register-verify-email.component.html',
  styleUrls: ['./register-verify-email.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterVerifyEmailComponent extends NgxsBaseComponent {
  isVerifyingEmail = false;
  email?: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cd: ChangeDetectorRef,
    private actions$: Actions,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.email = this.route.snapshot.queryParams['email'];

    const verificationCode = this.route.snapshot.queryParams['verificationCode'];

    if (verificationCode) {
      this.isVerifyingEmail = true;
      this.store.dispatch(new VerifyEmail({ email: this.email!, verificationCode }));
      this.cd.markForCheck();
    }

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(ResendVerifyEmail),
        switchMap((data: ActionCompletion) => {
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', 'Verification email resent'));
          }

          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(VerifyEmail),
        switchMap((data: ActionCompletion) => {
          this.cd.markForCheck();

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', 'Email verified successfully, you may proceed to login'));
            this.navigate(['login']);
          }

          return of(false);
        })
      )
    );
  }

  doResendVerifyEmail() {
    this.store.dispatch(new ResendVerifyEmail({ email: this.email! }));
  }
}
