import { UserType } from "../data";

export interface LoginRequestModel {
  email: string;
  password: string;
  userType: UserType;
}
