import { EmailCampaignTypeModel, getErrorMessage } from '@grabbill/lib';
import { format } from 'date-fns';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { Observable, of, switchMap } from 'rxjs';
import { ShowMessage } from '../states/common/common.state-actions';
import { NavigationExtras, Params } from '@angular/router';
import { NewEmailCampaignActivity } from '../states/email-campaign/email-campaign.state-actions';
import { EmailCampaignState } from '../states/email-campaign/email-campaign.state';

export const createEmailCampaign = (store: Store, type: EmailCampaignTypeModel, name: string) => {
  const currentDate = new Date();
  const dateString = format(currentDate, 'yyyy-MM-dd HH:mm:ss');

  store.dispatch(
    new NewEmailCampaignActivity(type.id, {
      name: `${name} ${dateString}`,
      emailFrom: type.emailFrom,
      emailFromName: type.emailFromName,
      emailContent: type.emailContent,
      emailMjmlContent: type.emailMjmlContent,
      emailSubject: type.emailSubject,
    })
  );
};

export const handleNewEmailCampaign = (
  actions$: Actions,
  store: Store,
  navigate: (path: unknown[], queryParams?: Params, extras?: NavigationExtras) => Observable<any>
) => {
  return actions$.pipe(
    ofActionCompleted(NewEmailCampaignActivity),
    switchMap((data: ActionCompletion) => {
      if (data.result.error) {
        store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
      } else if (data.result.successful) {
        navigate([
          '/',
          'email-campaign',
          'detail',
          store.selectSnapshot(EmailCampaignState.emailCampaignType)!.id,
          'activity',
          store.selectSnapshot(EmailCampaignState.emailCampaignActivity)!.id,
          'edit',
        ]);
      }
      return of(false);
    })
  )
};
