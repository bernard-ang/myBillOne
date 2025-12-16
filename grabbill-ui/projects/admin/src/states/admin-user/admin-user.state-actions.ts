import { AdminUserRequestModel, PageableModel } from '@grabbill/lib';

export class ResetUsers {
  static readonly type = '[Admin User] ResetUsers';
}

export class QueryUsers {
  static readonly type = '[Admin User] QueryUsers';

  constructor(public pageable?: PageableModel, public name?: string) {}
}

export class NewUser {
  static readonly type = '[Admin User] NewUser';

  constructor(public request: AdminUserRequestModel) {}
}

export class UpdateUser {
  static readonly type = '[Admin User] UpdateUser';

  constructor(public id: number, public request: AdminUserRequestModel) {}
}

export class DeleteUser {
  static readonly type = '[Admin User] DeleteUser';

  constructor(public id: number) {}
}

export class ActivateUser {
  static readonly type = '[Admin User] ActivateUser';

  constructor(public id: number) {}
}

export class DeactivateUser {
  static readonly type = '[Admin User] DeactivateUser';

  constructor(public id: number) {}
}
