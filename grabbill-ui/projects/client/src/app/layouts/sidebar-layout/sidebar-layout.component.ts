import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Select, Store } from '@ngxs/store';
import { filter, Observable, tap } from 'rxjs';
import { DashboardStatisticsModel, Privilege, UserAuthorityModel } from '@grabbill/lib';
import { NgxsBaseComponent } from '../../components/ngxs-base.component';
import { AuthState } from '../../../states/auth/auth.state';
import { Logout, Me } from '../../../states/auth/auth.state-actions';
import { hasPrivilege, hasSomePrivileges, isExperimental } from '../../../utils/has-privilege';
import { AccountRoutes, MiscRoutes, RouteInfo, TypeRoutes } from '../../../route-info';
import { OpenContextSensitiveHelp } from '../../../states/common/common.state-actions';
import { environment } from '../../../environments/environment';
import buildInfo from '../../../../../../buildinfo.json';
import { GetCurrentStatistics } from '../../../states/dashboard/dashboard.state-actions';
import { DashboardState } from '../../../states/dashboard/dashboard.state';
import { isOnPremiseMode, isSaasMode } from "../../../utils/deployment-mode";

@Component({
  selector: 'grabbill-client-sidebar-layout',
  templateUrl: './sidebar-layout.component.html',
  styleUrls: ['./sidebar-layout.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SidebarLayoutComponent extends NgxsBaseComponent implements OnInit {
  @Select(AuthState.user)
  user$!: Observable<UserAuthorityModel>;

  @Select(DashboardState.currentStatistics)
  currentStatistics$!: Observable<DashboardStatisticsModel>;

  isCollapsed = false;
  url = '';
  isSaas = isSaasMode()

  constructor(public router: Router, protected override store: Store, private cd: ChangeDetectorRef) {
    super(store);
    this.url = this.router.url;
  }

  public get buildInfo() {
    return buildInfo;
  }

  public get privilege(): typeof Privilege {
    return Privilege;
  }

  public get contextSensitiveHelp(): boolean {
    return environment.config.contextSensitiveHelp;
  }

  public get typeRoutes(): RouteInfo[] {
    return TypeRoutes;
  }

  public get accountRoutes(): RouteInfo[] {
    return AccountRoutes;
  }

  public get miscRoutes(): RouteInfo[] {
    return MiscRoutes;
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.router.events.pipe(
        filter((event) => event instanceof NavigationEnd),
        tap((event) => {
          this.url = (event as { url: string }).url;
          if (this.url.includes('/sms/')) {
            this.store.dispatch(new GetCurrentStatistics());
          }
          this.cd.markForCheck();
        })
      ),
      this.user$.pipe(
        tap((user) => {
          if (user && !user.guidedStepsViewed && environment.config.contextSensitiveHelp) {
            this.store.dispatch(new OpenContextSensitiveHelp('grabbill'));
          }
        })
      )
    );

    this.store.dispatch(new Me());
    this.store.dispatch(new GetCurrentStatistics());
  }

  logout() {
    this.store.dispatch(new Logout());
    return this.navigate(['login']);
  }

  hasPrivilege(user: UserAuthorityModel, privilege: Privilege) {
    return hasPrivilege(user, privilege);
  }

  isSameMode (mode?: string) {
    if (!mode) {
      return true;
    } else {
      return mode === environment.config.deploymentMode;
    }
  }

  hasWaba(user: UserAuthorityModel, isWabaRequired?: boolean) {
    if(isWabaRequired) {
      return !!user.account.wabaGuid || isOnPremiseMode()
    } else {
      return true
    }
  }

  hasSomePrivileges(user: UserAuthorityModel, privileges: Privilege[]) {
    return hasSomePrivileges(user, privileges);
  }

  doHelp() {
    this.store.dispatch(new OpenContextSensitiveHelp(this.url));
  }

  displayExperimentalFeature(isExperimentalRoute: boolean) {
    if (!isExperimentalRoute) {
      return true;
    } else {
      return isExperimental();
    }
  }
}
