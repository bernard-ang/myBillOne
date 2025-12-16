import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { AuthState } from '../../../../states/auth/auth.state';
import { Observable, of, switchMap, tap } from 'rxjs';
import { getErrorMessage, resolveErrorMessage, updateAndMarkControlAsDirty, UserAuthorityModel } from '@grabbill/lib';
import { AbstractControl, ReactiveFormsModule, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { NzMessageService } from 'ng-zorro-antd/message';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { CommonModule } from '@angular/common';
import { AppCommonModule } from '../../app-common/app-common.module';
import { IconsProviderModule } from '../../../icons-provider.module';
import { UpdateSftp } from '../../../../states/account/account.state-actions';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { NzInputModule } from 'ng-zorro-antd/input';

@Component({
  selector: 'grabbill-client-sftp-detail',
  standalone: true,
  imports: [
    AppCommonModule,
    CommonModule,
    ReactiveFormsModule,
    IconsProviderModule,
    NzSpinModule,
    NzButtonModule,
    NzFormModule,
    NzCardModule,
    NzInputNumberModule,
    NzInputModule,
  ],
  templateUrl: './sftp-detail.component.html',
  styleUrl: './sftp-detail.component.less',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SftpDetailComponent extends NgxsBaseComponent {
  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  user?: UserAuthorityModel;

  form: UntypedFormGroup;

  isLoading = true;

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private actions$: Actions,
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      host: ['', [Validators.required, Validators.maxLength(255)]],
      port: ['', [Validators.required, Validators.max(99999999999)]],
      username: ['', [Validators.maxLength(255)]],
      password: ['', [Validators.maxLength(255)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.user$.pipe(
        tap((user) => {
          if (user) {
            this.user = user;
            this.form.setValue({
              host: user.account.sftpHost || null,
              port: user.account.sftpPort || null,
              username: user.account.sftpUsername || null,
              password: user.account.sftpPassword || null,
            });
            this.isLoading = false;
            this.cd.markForCheck();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateSftp),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `SFTP updated`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      )
    );
  }

  doSave() {
    this.isLoading = true;
    this.cd.markForCheck();
    const value = this.form.getRawValue();

    if (this.form.valid) {
      this.store.dispatch(
        new UpdateSftp({
          host: value.host,
          port: value.port,
          username: value.username,
          password: value.password,
        })
      );
    } else {
      updateAndMarkControlAsDirty(this.form);
      this.isLoading = false;
      this.cd.markForCheck();
    }
  }

  getErrorMessage(control: AbstractControl) {
    return resolveErrorMessage(control);
  }
}
