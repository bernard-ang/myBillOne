import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  BouncedEmailModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { BouncedEmailStateModel } from './bounced-email.state-model';
import { BouncedEmailApi } from '../../api/bounced-email.api';
import { DeleteBouncedEmail, QueryBouncedEmails, ResetBouncedEmails } from './bounced-email.state-actions';
import { UserStateModel } from '../user/user.state-model';

@State<BouncedEmailStateModel>({
  name: 'bounced_email',
  defaults: {
    bouncedEmailPageable: makePageable(10),
    bouncedEmailSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class BouncedEmailState {
  constructor(private bouncedEmailApi: BouncedEmailApi) {}

  @Selector()
  static bouncedEmailSearchResult(state: BouncedEmailStateModel) {
    return state.bouncedEmailSearchResult;
  }

  @Selector()
  static bouncedEmailPageable(state: BouncedEmailStateModel) {
    return state.bouncedEmailPageable;
  }

  @Selector()
  static email(state: BouncedEmailStateModel) {
    return state.email;
  }

  @Selector()
  static startDate(state: BouncedEmailStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: BouncedEmailStateModel) {
    return state.endDate;
  }

  @Action(ResetBouncedEmails)
  resetUsers(context: StateContext<BouncedEmailStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.bouncedEmailSearchResult = makeSearchResultPayload();
        draft.bouncedEmailPageable = makePageable(10);
        draft.email = undefined;
        draft.startDate = undefined;
        draft.endDate = undefined;
      })
    );
  }

  @Action(QueryBouncedEmails)
  queryBouncedEmails(
    context: StateContext<BouncedEmailStateModel>,
    { pageable, email, startDate, endDate }: QueryBouncedEmails
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.bouncedEmailPageable = pageable ? pageable : draft.bouncedEmailPageable;
        draft.email = email !== undefined ? email : draft.email;
        draft.startDate = startDate;
        draft.endDate = endDate;
      })
    );

    return this.bouncedEmailApi
      .getBouncedEmails(
        context.getState().bouncedEmailPageable,
        context.getState().email,
        context.getState().startDate,
        context.getState().endDate
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<BouncedEmailModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.bouncedEmailSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(DeleteBouncedEmail)
  deleteBouncedEmail(context: StateContext<UserStateModel>, { id }: DeleteBouncedEmail) {
    return this.bouncedEmailApi.deleteBouncedEmail(id).pipe(tap(() => context.dispatch(new QueryBouncedEmails())));
  }
}
