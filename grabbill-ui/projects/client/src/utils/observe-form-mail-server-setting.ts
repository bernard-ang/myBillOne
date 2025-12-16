import { environment } from '../environments/environment';
import { MailServerModel } from '@grabbill/lib';
import { Observable, tap } from 'rxjs';
import { UntypedFormGroup } from '@angular/forms';

export const observeTypeFormMailServerSetting = (
  mailServer$: Observable<MailServerModel>,
  form: UntypedFormGroup,
  isNew: boolean,
  mailServer?: MailServerModel
): Observable<MailServerModel> => {
  return mailServer$.pipe(
    tap((currentMailServer) => {
      if (currentMailServer) {
        mailServer = currentMailServer;
        if (!currentMailServer.customServer) {
          form.get('emailFrom')!.disable();
        }
        if (isNew) {
          form
            .get('emailFrom')!
            .setValue(currentMailServer.customServer ? currentMailServer.smtpFrom : environment.config.smtpFromEmail);
          form.get('emailFromName')!.setValue(currentMailServer.smtpFromName);
        }
      }
    })
  );
};

export const observeActivityFormMailServerSetting = (
  mailServer$: Observable<MailServerModel>,
  form: UntypedFormGroup,
  mailServer?: MailServerModel
): Observable<MailServerModel> => {
  return mailServer$.pipe(
    tap((value) => {
      if (value) {
        mailServer = value;
        if (!value.customServer) {
          form.get('emailFrom')!.disable();
          form.get('emailFrom')!.setValue(environment.config.smtpFromEmail);
        }
      }
    })
  );
};
