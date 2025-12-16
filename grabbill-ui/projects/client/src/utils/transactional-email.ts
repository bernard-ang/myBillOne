import { getErrorMessage, MultiTemplateTransactionalEmailTypeModel, TransactionalEmailTypeModel } from '@grabbill/lib';
import { format } from 'date-fns';
import { NewTransactionalEmailActivity } from '../states/transactional-email/transactional-email.state-actions';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { Observable, of, switchMap } from 'rxjs';
import { ShowMessage } from '../states/common/common.state-actions';
import { TransactionalEmailState } from '../states/transactional-email/transactional-email.state';
import { NavigationExtras, Params } from '@angular/router';
import { NewMultiTemplateTransactionalEmailActivity } from '../states/multi-template-transactional-email/multi-template-transactional-email.state-actions';
import { MultiTemplateTransactionalEmailState } from '../states/multi-template-transactional-email/multi-template-transactional-email.state';

export const createTransactionalEmail = (
  store: Store,
  type: TransactionalEmailTypeModel,
  transactionalEmailName: string
) => {
  const currentDate = new Date();
  const dateString = format(currentDate, 'yyyy-MM-dd HH:mm:ss');

  store.dispatch(
    new NewTransactionalEmailActivity(type.id, {
      name: `${transactionalEmailName} ${dateString}`,
      emailFrom: type.emailFrom,
      emailFromName: type.emailFromName,
      emailContent: type.emailContent,
      emailMjmlContent: type.emailMjmlContent,
      emailSubject: type.emailSubject,
    })
  );
};

export const handleNewTransactionEmail = (
  actions$: Actions,
  store: Store,
  navigate: (path: unknown[], queryParams?: Params, extras?: NavigationExtras) => Observable<any>
) => {
  return actions$.pipe(
    ofActionCompleted(NewTransactionalEmailActivity),
    switchMap((data: ActionCompletion) => {
      if (data.result.error) {
        store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
      } else if (data.result.successful) {
        navigate([
          '/',
          'transactional-email',
          'detail',
          store.selectSnapshot(TransactionalEmailState.transactionalEmailType)!.id,
          'activity',
          store.selectSnapshot(TransactionalEmailState.transactionalEmailActivity)!.id,
          'edit',
        ]);
      }
      return of(false);
    })
  );
};

export const createMultiTemplateTransactionalEmail = (
  store: Store,
  type: MultiTemplateTransactionalEmailTypeModel,
  transactionalEmailName: string
) => {
  const currentDate = new Date();
  const dateString = format(currentDate, 'yyyy-MM-dd HH:mm:ss');

  store.dispatch(
    new NewMultiTemplateTransactionalEmailActivity(type.id, {
      name: `${transactionalEmailName} ${dateString}`
    })
  );
};

export const handleNewMultiTemplateTransactionEmail = (
  actions$: Actions,
  store: Store,
  navigate: (path: unknown[], queryParams?: Params, extras?: NavigationExtras) => Observable<any>
) => {
  return actions$.pipe(
    ofActionCompleted(NewMultiTemplateTransactionalEmailActivity),
    switchMap((data: ActionCompletion) => {
      if (data.result.error) {
        store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
      } else if (data.result.successful) {
        navigate([
          '/',
          'mt-transactional-email',
          'detail',
          store.selectSnapshot(MultiTemplateTransactionalEmailState.transactionalEmailType)!.id,
          'activity',
          store.selectSnapshot(MultiTemplateTransactionalEmailState.transactionalEmailActivity)!.id,
          'edit',
        ]);
      }
      return of(false);
    })
  );
};
