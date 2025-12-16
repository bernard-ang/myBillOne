import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from "@angular/core";
import { AbstractControl, UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, Validators } from "@angular/forms";
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from "@ngxs/store";
import { Observable, of, switchMap, tap } from "rxjs";
import { NzMessageService } from "ng-zorro-antd/message";
import {
  getErrorMessage,
  MailServerModel,
  ProtocolEncryption,
  resolveErrorMessage,
  updateAndMarkControlAsDirty,
  UserAuthorityModel
} from "@grabbill/lib";
import { environment } from "projects/client/src/environments/environment";
import { NgxsBaseComponent } from "../../../components/ngxs-base.component";
import { ShowMessage } from "../../../../states/common/common.state-actions";
import { MailServerState } from "../../../../states/mail-server/mail-server.state";
import { GetMailServer, UpdateMailServer } from "../../../../states/mail-server/mail-server.state-actions";
import { AuthState } from "../../../../states/auth/auth.state";
import { isSaasMode } from "../../../../utils/deployment-mode";

@Component({
  selector: 'grabbill-client-mail-server-detail',
  templateUrl: './mail-server-detail.component.html',
  styleUrls: ['./mail-server-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailServerDetailComponent extends NgxsBaseComponent {
  @Select(MailServerState.mailServer)
  mailServer$!: Observable<MailServerModel>;

  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  user?: UserAuthorityModel;
  customServer = new UntypedFormControl(false);
  isLoading = false;
  isDataLoading = true;
  form: UntypedFormGroup;

  isSaas = isSaasMode()

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private actions$: Actions,
    private fb: UntypedFormBuilder,
    private cd: ChangeDetectorRef
  ) {
    super(store, messageService);

    this.form = this.fb.group({
      smtpHost: ['', [Validators.required, Validators.maxLength(255)]],
      smtpPort: ['', [Validators.required, Validators.max(99999999999)]],
      smtpEncryption: [ProtocolEncryption.SSL, [Validators.required]],
      smtpUsername: ['', [Validators.maxLength(255)]],
      smtpPassword: ['', [Validators.maxLength(255)]],
      smtpFrom: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      smtpFromName: ['', [Validators.required, Validators.maxLength(255)]],
      imapHost: ['', [Validators.required, Validators.maxLength(255)]],
      imapPort: ['', [Validators.required, Validators.max(99999999999)]],
      imapEncryption: [ProtocolEncryption.SSL, [Validators.required]],
      imapUsername: ['', [Validators.maxLength(255)]],
      imapPassword: ['', [Validators.maxLength(255)]],
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.toggleCustomServer(false);
    this.cd.markForCheck();

    this.autoUnsubscribe(
      this.user$.pipe(
        tap((user) => {
          if (user) {
            this.user = user;
          }
        })
      ),
      this.mailServer$.pipe(
        tap((mailServer) => {
          if (mailServer) {
            this.customServer.setValue(mailServer.customServer);
            this.form.patchValue({
              smtpFrom: mailServer.smtpFrom ? mailServer.smtpFrom : environment.config.smtpFromEmail,
              smtpFromName: mailServer.smtpFromName,
            });

            if (mailServer.customServer) {
              this.form.patchValue({
                smtpHost: mailServer.smtpHost,
                smtpPort: mailServer.smtpPort,
                smtpEncryption: mailServer.smtpEncryption,
                smtpUsername: mailServer.smtpUsername,
                smtpPassword: mailServer.smtpPassword,
                imapHost: mailServer.imapHost,
                imapPort: mailServer.imapPort,
                imapEncryption: mailServer.imapEncryption,
                imapUsername: mailServer.imapUsername,
                imapPassword: mailServer.imapPassword,
              });
              this.toggleCustomServer(true);
            }

            if (!this.user!.subscription!.customSmtp) {
              this.customServer.setValue(false);
              this.customServer.disable();
            }

            this.isDataLoading = false;
            this.cd.markForCheck();
          }
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetMailServer),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false
          this.isDataLoading = false
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(UpdateMailServer),
        switchMap((data: ActionCompletion) => {
          this.isLoading = false;

          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          } else if (data.result.successful) {
            this.store.dispatch(new ShowMessage('info', `Mail server updated`));
          }

          this.cd.markForCheck();
          return of(false);
        })
      )
    );
    this.store.dispatch(new GetMailServer());
  }

  toggleCustomServer(value: boolean) {
    if (value) {
      this.enableCustomServerFields();
    } else {
      this.disableCustomServerFields();
    }
  }

  private enableCustomServerFields() {
    this.form.get('smtpHost')?.enable();
    this.form.get('smtpPort')?.enable();
    this.form.get('smtpEncryption')?.enable();
    this.form.get('smtpUsername')?.enable();
    this.form.get('smtpPassword')?.enable();

    this.form.get('imapHost')?.enable();
    this.form.get('imapPort')?.enable();
    this.form.get('imapEncryption')?.enable();
    this.form.get('imapUsername')?.enable();
    this.form.get('imapPassword')?.enable();

    this.form.get('smtpFrom')?.enable();
  }

  private disableCustomServerFields() {
    this.form.get('smtpHost')?.disable();
    this.form.get('smtpHost')?.disable();
    this.form.get('smtpPort')?.disable();
    this.form.get('smtpEncryption')?.disable();
    this.form.get('smtpUsername')?.disable();
    this.form.get('smtpPassword')?.disable();

    this.form.get('imapHost')?.disable();
    this.form.get('imapPort')?.disable();
    this.form.get('imapEncryption')?.disable();
    this.form.get('imapUsername')?.disable();
    this.form.get('imapPassword')?.disable();

    this.form.get('smtpFrom')?.setValue(environment.config.smtpFromEmail);
    this.form.get('smtpFrom')?.disable();
  }

  doSave() {
    this.isLoading = true;
    this.cd.markForCheck();
    const value = this.form.getRawValue();

    if (this.form.valid) {
      this.store.dispatch(
        new UpdateMailServer(
          this.customServer.value
            ? {
                ...value,
                customServer: this.customServer.value,
              }
            : {
                smtpFrom: value.smtpFrom,
                smtpFromName: value.smtpFromName,
                customServer: this.customServer.value,
              }
        )
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
