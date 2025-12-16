import { PageableModel, UserRequestModel } from '@grabbill/lib';

export class ResetUsers {
  static readonly type = '[User] ResetUsers';
}

export class QueryUsers {
  static readonly type = '[User] QueryUsers';

  constructor(public pageable?: PageableModel, public name?: string) {}
}

export class NewUser {
  static readonly type = '[User] NewUser';

  constructor(public request: UserRequestModel) {}
}

export class UpdateUser {
  static readonly type = '[User] UpdateUser';

  constructor(public id: number, public request: UserRequestModel) {}
}

export class DeleteUser {
  static readonly type = '[User] DeleteUser';

  constructor(public id: number) {}
}

export class ActivateUser {
  static readonly type = '[User] ActivateUser';

  constructor(public id: number) {}
}

export class DeactivateUser {
  static readonly type = '[User] DeactivateUser';

  constructor(public id: number) {}
}

export class ResetUserPassword {
  static readonly type = '[User] UserResetPassword';

  constructor(public id: number) {}
}
