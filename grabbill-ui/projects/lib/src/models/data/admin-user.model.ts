export interface AdminUserModel {
  id: number;
  name: string;
  email: string;
  active: boolean;
  lastLoggedIn: Date;
  createdBy: string;
  createdDate: Date;
  google2FAEnabled: boolean;
  email2FAEnabled: boolean;
}
