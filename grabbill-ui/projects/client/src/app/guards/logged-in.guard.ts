import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Select, Store } from '@ngxs/store';
import { AuthState } from '../../states/auth/auth.state';
import { Observable, of, switchMap } from 'rxjs';
import { UserAuthorityModel } from '@grabbill/lib';
import { verifyUserState } from '../../utils/verify-user-state';
import { Me } from '../../states/auth/auth.state-actions';

@Injectable({
  providedIn: 'root',
})
export class LoggedInGuard  {
  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  constructor(private router: Router, private store: Store) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree | Observable<boolean | UrlTree> {
    const user = this.store.selectSnapshot(AuthState.user);
    if (user) {
      return this.redirectUser(route, state);
    } else {
      return this.store.dispatch(new Me()).pipe(
        switchMap((response) => {
          if (response.error) {
            return of(this.router.createUrlTree(['/', 'login']));
          } else {
            return of(this.redirectUser(route, state));
          }
        })
      );
    }
  }

  private redirectUser(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const user = this.store.selectSnapshot(AuthState.user);
    if (user) {
      const redirectPaths = verifyUserState(user, false, state.url, route.queryParams);
      if (redirectPaths.paths.length > 0) {
        if (state.url !== redirectPaths.paths.map((path) => (path === '/' ? '' : path)).join('/')) {
          return this.router.createUrlTree(redirectPaths.paths, { queryParams: redirectPaths.queryParams });
        }
      }
      return true;
    } else {
      return this.router.createUrlTree(['/', 'login']);
    }
  }
}
