import { getErrorMessage } from '@grabbill/lib';
import { format } from 'date-fns';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { Observable, of, switchMap } from 'rxjs';
import { ShowMessage } from '../states/common/common.state-actions';
import { NavigationExtras, Params } from '@angular/router';
import { NewDigitalFilingActivity } from '../states/digital-filing/digital-filing.state-actions';
import { DigitalFilingState } from '../states/digital-filing/digital-filing.state';

export const createDigitalFiling = (store: Store, typeId: number, name: string) => {
  const currentDate = new Date();
  const dateString = format(currentDate, 'yyyy-MM-dd HH:mm:ss');

  store.dispatch(new NewDigitalFilingActivity(typeId, { name: `${name} ${dateString}` }));
};

export const handleNewDigitalFiling = (
  actions$: Actions,
  store: Store,
  navigate: (path: unknown[], queryParams?: Params, extras?: NavigationExtras) => Observable<any>
) => {
  return actions$.pipe(
    ofActionCompleted(NewDigitalFilingActivity),
    switchMap((data: ActionCompletion) => {
      if (data.result.error) {
        store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
      } else if (data.result.successful) {
        navigate([
          '/',
          'digital-filing',
          'detail',
          store.selectSnapshot(DigitalFilingState.digitalFilingType)!.id,
          'activity',
          store.selectSnapshot(DigitalFilingState.digitalFilingActivity)!.id,
          'edit',
        ]);
      }
      return of(false);
    })
  )
};
