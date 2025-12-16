import { PrivilegeModel, RoleModel } from "@grabbill/lib";

export interface RoleStateModel {
  roles: string[];
  rolesWithPrivilege: RoleModel[];
  privileges: PrivilegeModel[];
}
