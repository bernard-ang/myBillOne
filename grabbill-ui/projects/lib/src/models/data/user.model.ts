import { AccountModel } from './account';

export interface UserModel {
  id: number;
  name: string;
  email: string;
  password: string;
  active: boolean;
  verified: boolean;
  role: string;
  privileges: string[];
  codes: string[];
  account: AccountModel;
  lastLoggedIn: Date;
  google2FAEnabled: boolean;
  email2FAEnabled: boolean;
}
