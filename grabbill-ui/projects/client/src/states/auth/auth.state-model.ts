import { UserAuthorityModel } from "@grabbill/lib";

export interface AuthStateModel {
  user?: UserAuthorityModel;
  email?: string;
}
