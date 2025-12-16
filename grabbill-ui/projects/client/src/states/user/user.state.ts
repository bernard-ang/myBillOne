import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
  UserModel,
} from '@grabbill/lib';
import { UserStateModel } from './user.state-model';
import { UserApi } from '../../api/user.api';
import {
  ActivateUser,
  DeactivateUser,
  DeleteUser,
  NewUser,
  QueryUsers,
  ResetUserPassword,
  ResetUsers,
  UpdateUser,
} from './user.state-actions';

@State<UserStateModel>({
  name: 'user',
  defaults: {
    userPageable: makePageable(),
    userSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class UserState {
  constructor(private userApi: UserApi) {}

  @Selector()
  static userResult(state: UserStateModel) {
    return state.userSearchResult;
  }

  @Selector()
  static userPageable(state: UserStateModel) {
    return state.userPageable;
  }

  @Action(ResetUsers)
  resetUsers(context: StateContext<UserStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.userSearchResult = makeSearchResultPayload();
        draft.userPageable = makePageable();
        draft.name = undefined;
      })
    );
  }

  @Action(QueryUsers)
  queryUsers(context: StateContext<UserStateModel>, { pageable, name }: QueryUsers) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.userPageable = pageable ? pageable : draft.userPageable;
        draft.name = name !== undefined ? name : draft.name;
      })
    );

    return this.userApi.getUsers(context.getState().userPageable, context.getState().name).pipe(
      tap((response: ApiResponseModel<SearchResultPayloadModel<UserModel>>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.userSearchResult = response.data;
          })
        );
      })
    );
  }

  @Action(NewUser)
  newUser(context: StateContext<UserStateModel>, { request }: NewUser) {
    return this.userApi.newUser(request).pipe(tap(() => context.dispatch(new QueryUsers())));
  }

  @Action(UpdateUser)
  updateUser(context: StateContext<UserStateModel>, { id, request }: UpdateUser) {
    return this.userApi.updateUser(id, request).pipe(tap(() => context.dispatch(new QueryUsers())));
  }

  @Action(DeleteUser)
  deleteUser(context: StateContext<UserStateModel>, { id }: DeleteUser) {
    return this.userApi.deleteUser(id).pipe(tap(() => context.dispatch(new QueryUsers())));
  }

  @Action(ActivateUser)
  activateUser(context: StateContext<UserStateModel>, { id }: DeleteUser) {
    return this.userApi.activateUser(id).pipe(tap(() => context.dispatch(new QueryUsers())));
  }

  @Action(DeactivateUser)
  deactivateUser(context: StateContext<UserStateModel>, { id }: DeleteUser) {
    return this.userApi.deactivateUser(id).pipe(tap(() => context.dispatch(new QueryUsers())));
  }

  @Action(ResetUserPassword)
  resetPassword(context: StateContext<UserStateModel>, { id }: ResetUserPassword) {
    return this.userApi.resetPassword(id).pipe(tap(() => context.dispatch(new QueryUsers())));
  }
}
