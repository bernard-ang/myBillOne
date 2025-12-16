import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { ApiResponseModel, PrivilegeModel, RoleModel } from '@grabbill/lib';
import { RoleApi } from '../../api/role.api';
import { DeleteRole, GetRoles, GetRolesWithPrivilege, NewRole, UpdateRole } from './role.state-actions';
import { RoleStateModel } from './role.state-model';
import { UserStateModel } from '../user/user.state-model';

@State<RoleStateModel>({
  name: 'role',
  defaults: {
    roles: [],
    rolesWithPrivilege: [],
    privileges: [],
  },
})
@Injectable()
export class RoleState {
  constructor(private roleApi: RoleApi) {}

  @Selector()
  static roles(state: RoleStateModel) {
    return state.roles;
  }

  @Selector()
  static rolesWithPrivilege(state: RoleStateModel) {
    return state.rolesWithPrivilege;
  }

  @Selector()
  static privileges(state: RoleStateModel) {
    return state.privileges;
  }

  @Action(GetRoles)
  getRoles(context: StateContext<RoleStateModel>) {
    return this.roleApi.getRoles().pipe(
      tap((response: ApiResponseModel<{ roles: string[] }>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.roles = response.data.roles;
          })
        );
      })
    );
  }

  @Action(GetRolesWithPrivilege)
  getRolesWithPrivilege(context: StateContext<RoleStateModel>) {
    return this.roleApi.getRolesWithPrivilege().pipe(
      tap((response: ApiResponseModel<{ roles: RoleModel[]; privileges: PrivilegeModel[] }>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.rolesWithPrivilege = response.data.roles;
            draft.privileges = response.data.privileges;
          })
        );
      })
    );
  }

  @Action(NewRole)
  newUser(context: StateContext<UserStateModel>, { request }: NewRole) {
    return this.roleApi.newRole(request).pipe(tap(() => context.dispatch(new GetRolesWithPrivilege())));
  }

  @Action(UpdateRole)
  updateUser(context: StateContext<UserStateModel>, { id, request }: UpdateRole) {
    return this.roleApi.updateRole(id, request).pipe(tap(() => context.dispatch(new GetRolesWithPrivilege())));
  }

  @Action(DeleteRole)
  deleteUser(context: StateContext<UserStateModel>, { id }: DeleteRole) {
    return this.roleApi.deleteRole(id).pipe(tap(() => context.dispatch(new GetRolesWithPrivilege())));
  }
}
