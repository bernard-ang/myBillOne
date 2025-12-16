import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Select, Store } from '@ngxs/store';
import { AuthState } from '../../states/auth/auth.state';
import { map, Observable } from 'rxjs';
import { UserAuthorityModel } from '@grabbill/lib';
import { hasPrivilege } from '../../utils/has-privilege';
import { AuthService } from '../../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class PrivilegeGuard  {
  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  constructor(private router: Router, private store: Store, private authService: AuthService) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree | Observable<boolean | UrlTree> {
    const user = this.store.selectSnapshot(AuthState.user);
    if (!user) {
      return this.authService.getAuthority().pipe(
        map((userResponse) => {
          return this.hasRoutePrivilege(route, userResponse.data);
        })
      );
    }

    return this.hasRoutePrivilege(route, user);
  }

  private hasRoutePrivilege(route: ActivatedRouteSnapshot, user: UserAuthorityModel) {
    const privilege = (route.data as any).privilege;
    if (hasPrivilege(user, privilege)) {
      return true;
    } else {
      return this.router.createUrlTree(['/', 'unauthorized']);
    }
  }
}
