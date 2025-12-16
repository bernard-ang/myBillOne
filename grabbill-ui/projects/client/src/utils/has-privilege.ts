import { Privilege, UserAuthorityModel } from '@grabbill/lib';
import { environment } from '../environments/environment';

export const hasPrivilege = (user: UserAuthorityModel, privilege: Privilege): boolean => {
  if (!user) {
    return false;
  }
  return user.privileges.includes(privilege);
};

export const hasSomePrivileges = (user: UserAuthorityModel, privileges: Privilege[]): boolean => {
  if (!user) {
    return false;
  }
  return user.privileges.some((current) => privileges.includes(current));
};

export const isExperimental = (): boolean => {
  if (localStorage.getItem('GRABBILL_EXPERIMENTAL_FEATURE')) {
    return true;
  }
  return environment.config.experimentalFeature;
};
