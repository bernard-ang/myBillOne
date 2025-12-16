import { getErrorMessage, MultiTemplateWhatsappTypeModel, WhatsAppTypeModel } from "@grabbill/lib";
import { format } from "date-fns";
import { ActionCompletion, Actions, ofActionCompleted, Store } from "@ngxs/store";
import { Observable, of, switchMap } from "rxjs";
import { ShowMessage } from "../states/common/common.state-actions";
import { NavigationExtras, Params } from "@angular/router";
import {
  NewMultiTemplateWhatsAppActivity,
  NewWhatsAppActivity,
  ResetMultiTemplateWhatsAppActivity
} from "../states/whatsapp/whatsapp.state-actions";
import { WhatsAppState } from "../states/whatsapp/whatsapp.state";

export const createWhatsApp = (
  store: Store,
  type: WhatsAppTypeModel,
  whatsAppName: string
) => {
  const currentDate = new Date();
  const dateString = format(currentDate, 'yyyy-MM-dd HH:mm:ss');

  store.dispatch(
    new NewWhatsAppActivity(type.id, {
      name: `${whatsAppName} ${dateString}`,
    })
  );
};

export const createMultiTemplateWhatsApp = (
  store: Store,
  type: MultiTemplateWhatsappTypeModel,
  whatsAppName: string
) => {
  const currentDate = new Date();
  const dateString = format(currentDate, 'yyyy-MM-dd HH:mm:ss');

  store.dispatch(
    new NewMultiTemplateWhatsAppActivity(type.id, {
      name: `${whatsAppName} ${dateString}`,
    })
  );
};

export const handleNewWhatsApp = (
  actions$: Actions,
  store: Store,
  navigate: (path: unknown[], queryParams?: Params, extras?: NavigationExtras) => Observable<any>
) => {
  return actions$.pipe(
    ofActionCompleted(NewWhatsAppActivity),
    switchMap((data: ActionCompletion) => {
      if (data.result.error) {
        store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
      } else if (data.result.successful) {
        navigate([
          '/',
          'whatsapp',
          'detail',
          store.selectSnapshot(WhatsAppState.whatsAppType)!.id,
          'activity',
          store.selectSnapshot(WhatsAppState.whatsAppActivity)!.id,
          'edit',
        ]);
      }
      return of(false);
    })
  );
};

export const handleMultiTemplateNewWhatsApp = (
  actions$: Actions,
  store: Store,
  navigate: (path: unknown[], queryParams?: Params, extras?: NavigationExtras) => Observable<any>
) => {
  return actions$.pipe(
    ofActionCompleted(NewMultiTemplateWhatsAppActivity),
    switchMap((data: ActionCompletion) => {
      if (data.result.error) {
        store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
      } else if (data.result.successful) {
        navigate([
          '/',
          'mt-whatsapp',
          'detail',
          store.selectSnapshot(WhatsAppState.multiTemplateWhatsAppType)!.id,
          'activity',
          store.selectSnapshot(WhatsAppState.multiTemplateWhatsAppActivity)!.id,
          'edit',
        ]);
      }
      return of(false);
    })
  );
};
