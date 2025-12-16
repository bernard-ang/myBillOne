import { getErrorMessage, SmsTypeModel } from '@grabbill/lib';
import { format } from 'date-fns';
import { NewSmsActivity } from '../states/sms/sms.state-actions';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { Observable, of, switchMap } from 'rxjs';
import { ShowMessage } from '../states/common/common.state-actions';
import { SmsState } from '../states/sms/sms.state';
import { NavigationExtras, Params } from '@angular/router';

export const smsContentHelp =
  'To use contact fields / index fields in content, you may specify them using double curly bracket with header name e.g. {{mobileNumber}}';

export const createSms = (store: Store, type: SmsTypeModel, smsName: string) => {
  const currentDate = new Date();
  const dateString = format(currentDate, 'yyyy-MM-dd HH:mm:ss');

  store.dispatch(
    new NewSmsActivity(type.id, {
      name: `${smsName} ${dateString}`,
      smsFrom: type.smsFrom,
      smsContent: type.smsContent,
    })
  );
};

export const handleNewSms = (
  actions$: Actions,
  store: Store,
  navigate: (path: unknown[], queryParams?: Params, extras?: NavigationExtras) => Observable<any>
) => {
  return actions$.pipe(
    ofActionCompleted(NewSmsActivity),
    switchMap((data: ActionCompletion) => {
      if (data.result.error) {
        store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
      } else if (data.result.successful) {
        navigate([
          '/',
          'sms',
          'detail',
          store.selectSnapshot(SmsState.smsType)!.id,
          'activity',
          store.selectSnapshot(SmsState.smsActivity)!.id,
          'edit',
        ]);
      }
      return of(false);
    })
  );
};
