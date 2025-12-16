import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  AdminUserModel,
  ApiResponseModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { AdminUserStateModel } from './admin-user.state-model';
import { AdminUserApi } from '../../api/admin-user.api';
import {
  ActivateUser,
  DeactivateUser,
  DeleteUser,
  NewUser,
  QueryUsers,
  ResetUsers,
  UpdateUser,
} from './admin-user.state-actions';

@State<AdminUserStateModel>({
  name: 'admin_user',
  defaults: {
    userPageable: makePageable(10),
    userSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class AdminUserState {
  constructor(private adminUserApi: AdminUserApi) {}

  @Selector()
  static userResult(state: AdminUserStateModel) {
    return state.userSearchResult;
  }

  @Selector()
  static userPageable(state: AdminUserStateModel) {
    return state.userPageable;
  }

  @Action(ResetUsers)
  resetUsers(context: StateContext<AdminUserStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.userSearchResult = makeSearchResultPayload();
        draft.userPageable = makePageable(10);
        draft.name = undefined;
      })
    );
  }

  @Action(QueryUsers)
  queryUsers(context: StateContext<AdminUserStateModel>, { pageable, name }: QueryUsers) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.userPageable = pageable ? pageable : draft.userPageable;
        draft.name = name !== undefined ? name : draft.name;
      })
    );

    return this.adminUserApi.getUsers(context.getState().userPageable, context.getState().name).pipe(
      tap((response: ApiResponseModel<SearchResultPayloadModel<AdminUserModel>>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.userSearchResult = response.data;
          })
        );
      })
    );
  }

  @Action(NewUser)
  newUser(context: StateContext<AdminUserStateModel>, { request }: NewUser) {
    return this.adminUserApi.newUser(request).pipe(tap(() => context.dispatch(new QueryUsers())));
  }

  @Action(UpdateUser)
  updateUser(context: StateContext<AdminUserStateModel>, { id, request }: UpdateUser) {
    return this.adminUserApi.updateUser(id, request).pipe(tap(() => context.dispatch(new QueryUsers())));
  }

  @Action(DeleteUser)
  deleteUser(context: StateContext<AdminUserStateModel>, { id }: DeleteUser) {
    return this.adminUserApi.deleteUser(id).pipe(tap(() => context.dispatch(new QueryUsers())));
  }

  @Action(ActivateUser)
  activateUser(context: StateContext<AdminUserStateModel>, { id }: DeleteUser) {
    return this.adminUserApi.activateUser(id).pipe(tap(() => context.dispatch(new QueryUsers())));
  }

  @Action(DeactivateUser)
  deactivateUser(context: StateContext<AdminUserStateModel>, { id }: DeleteUser) {
    return this.adminUserApi.deactivateUser(id).pipe(tap(() => context.dispatch(new QueryUsers())));
  }
}
