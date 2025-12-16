import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
  UnsubscribedEmailModel,
} from '@grabbill/lib';
import { UnsubscribedEmailStateModel } from './unsubscribed-email.state-model';
import { UnsubscribedEmailApi } from '../../api/unsubscribed-email.api';
import {
  DeleteUnsubscribedEmail,
  QueryUnsubscribedEmails,
  ResetUnsubscribedEmails,
} from './unsubscribed-email.state-actions';
import { UserStateModel } from '../user/user.state-model';

@State<UnsubscribedEmailStateModel>({
  name: 'unsubscribed_email',
  defaults: {
    unsubscribedEmailPageable: makePageable(10),
    unsubscribedEmailSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class UnsubscribedEmailState {
  constructor(private unsubscribedEmailApi: UnsubscribedEmailApi) {}

  @Selector()
  static unsubscribedEmailSearchResult(state: UnsubscribedEmailStateModel) {
    return state.unsubscribedEmailSearchResult;
  }

  @Selector()
  static unsubscribedEmailPageable(state: UnsubscribedEmailStateModel) {
    return state.unsubscribedEmailPageable;
  }

  @Selector()
  static email(state: UnsubscribedEmailStateModel) {
    return state.email;
  }

  @Selector()
  static startDate(state: UnsubscribedEmailStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: UnsubscribedEmailStateModel) {
    return state.endDate;
  }

  @Action(ResetUnsubscribedEmails)
  resetUnsubscribedEmails(context: StateContext<UnsubscribedEmailStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.unsubscribedEmailPageable = makePageable(10);
        draft.unsubscribedEmailSearchResult = makeSearchResultPayload();
        draft.email = undefined;
        draft.startDate = undefined;
        draft.endDate = undefined;
      })
    );
  }

  @Action(QueryUnsubscribedEmails)
  queryUnsubscribedEmails(
    context: StateContext<UnsubscribedEmailStateModel>,
    { pageable, email, startDate, endDate }: QueryUnsubscribedEmails
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.unsubscribedEmailPageable = pageable ? pageable : draft.unsubscribedEmailPageable;
        draft.startDate = startDate;
        draft.endDate = endDate;
        draft.email = email !== undefined ? email : draft.email;
      })
    );

    return this.unsubscribedEmailApi
      .getUnsubscribedEmails(
        context.getState().unsubscribedEmailPageable,
        context.getState().email,
        context.getState().startDate,
        context.getState().endDate
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<UnsubscribedEmailModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.unsubscribedEmailSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(DeleteUnsubscribedEmail)
  deleteUnsubscribedEmail(context: StateContext<UserStateModel>, { id }: DeleteUnsubscribedEmail) {
    return this.unsubscribedEmailApi
      .deleteUnsubscribedEmail(id)
      .pipe(tap(() => context.dispatch(new QueryUnsubscribedEmails())));
  }
}
