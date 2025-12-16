import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { filter, Observable, of, switchMap, tap } from 'rxjs';
import { Privilege, UserAuthorityModel } from '@grabbill/lib';
import { NavigationEnd, Router } from '@angular/router';
import { NgxsBaseComponent } from '../../components/ngxs-base.component';
import { AdminAuthState } from '../../../states/admin-auth/admin-auth.state';
import { AllRoutes, RouteInfo } from '../../../admin-route-info';
import { Logout, Me } from '../../../states/admin-auth/admin-auth.state-actions';
import buildInfo from '../../../../../../buildinfo.json';

@Component({
  selector: 'grabbill-admin-sidebar-layout',
  templateUrl: './admin-sidebar-layout.component.html',
  styleUrls: ['./admin-sidebar-layout.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSidebarLayoutComponent extends NgxsBaseComponent implements OnInit {
  @Select(AdminAuthState.user)
  user$!: Observable<UserAuthorityModel>;

  isLoading = true;
  isCollapsed = false;
  url = '';

  constructor(
    public router: Router,
    protected override store: Store,
    private cd: ChangeDetectorRef,
    private actions$: Actions
  ) {
    super(store);
    this.url = this.router.url;
  }

  public get buildInfo() {
    return buildInfo;
  }

  public get privilege(): typeof Privilege {
    return Privilege;
  }

  public get routes(): RouteInfo[] {
    return AllRoutes;
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.autoUnsubscribe(
      this.router.events.pipe(
        filter((event) => event instanceof NavigationEnd),
        tap((event) => {
          this.url = (event as { url: string }).url;
          this.cd.markForCheck();
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(Me),
        switchMap((data: ActionCompletion) => {
          this.cd.markForCheck();

          if (data.result.error) {
            return this.navigate(['/', 'login']);
          } else if (data.result.successful) {
            const user = this.store.selectSnapshot(AdminAuthState.user)!;
            if (!user || !user.active) {
              return ['/', 'login'];
            }
          }

          this.isLoading = false;
          this.cd.markForCheck();

          return of(false);
        })
      )
    );

    this.store.dispatch(new Me());
  }

  logout() {
    this.store.dispatch(new Logout());
    return this.navigate(['login']);
  }
}
