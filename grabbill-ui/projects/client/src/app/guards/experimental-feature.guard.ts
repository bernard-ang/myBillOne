import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { isExperimental } from '../../utils/has-privilege';

@Injectable({
  providedIn: 'root',
})
export class ExperimentalFeatureGuard  {
  constructor(private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    if (!isExperimental()) {
      return this.router.createUrlTree(['/', 'experimental-feature']);
    }
    return true;
  }
}
