import { RoleRequestModel } from '@grabbill/lib';

export class GetRoles {
  static readonly type = '[Role] GetRoles';

  constructor() {}
}

export class GetRolesWithPrivilege {
  static readonly type = '[Role] GetRolesWithPrivilege';

  constructor() {}
}

export class NewRole {
  static readonly type = '[Role] NewRole';

  constructor(public request: RoleRequestModel) {}
}

export class UpdateRole {
  static readonly type = '[Role] UpdateRole';

  constructor(public id: number, public request: RoleRequestModel) {}
}

export class DeleteRole {
  static readonly type = '[Role] DeleteRole';

  constructor(public id: number) {}
}
