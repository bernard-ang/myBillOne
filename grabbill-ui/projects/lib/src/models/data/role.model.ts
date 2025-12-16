export interface RoleModel {
  id: number;
  name: string;
  privileges: PrivilegeModel[];
}


export interface PrivilegeModel {
  id: number;
  name: string;
}
