import { Observable, of, switchMap } from 'rxjs';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import {
  AuditLogModel,
  DashboardStatisticsModel,
  DomainType,
  getErrorMessage,
  PageableModel,
  SearchResultPayloadModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { DashboardState } from '../../../states/dashboard/dashboard.state';
import {
  GetCurrentStatistics,
  LoadMoreRecentActivities,
  QueryRecentActivities,
  ResetRecentActivities,
} from '../../../states/dashboard/dashboard.state-actions';
import { environment } from '../../../environments/environment';
import { Params } from '@angular/router';
import lower from 'lodash/toLower';
import capitalize from 'lodash/capitalize';
import { AllRoutes } from '../../../route-info';
import { ShowMessage } from '../../../states/common/common.state-actions';
import { AuthState } from '../../../states/auth/auth.state';

@Component({
  selector: 'grabbill-client-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent extends NgxsBaseComponent {
  @Select(DashboardState.currentStatistics)
  currentStatistics$!: Observable<DashboardStatisticsModel>;

  @Select(DashboardState.recentActivitiesSearchResult)
  recentActivitiesSearchResult$!: Observable<SearchResultPayloadModel<AuditLogModel>>;

  @Select(DashboardState.recentActivitiesPageable)
  recentActivitiesPageable$!: Observable<PageableModel>;

  user?: UserAuthorityModel;

  constructor(
    protected override store: Store,
    protected override messageService: NzMessageService,
    private actions$: Actions
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.user = this.store.selectSnapshot(AuthState.user);

    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(QueryRecentActivities, LoadMoreRecentActivities),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      )
    );
    this.store.dispatch(new GetCurrentStatistics());
    this.store.dispatch(new QueryRecentActivities());
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    this.store.dispatch(new ResetRecentActivities());
  }

  doLoadMore() {
    this.store.dispatch(new LoadMoreRecentActivities());
  }

  getIcon(domainType: DomainType) {
    return AllRoutes.filter((route) => route.domainType === domainType)[0].icon;
  }

  getTypeLabel(domainType: DomainType, actionType: string): string {
    if (actionType.startsWith('TYPE_')) {
      return `${capitalize(actionType.replace('TYPE_', ''))} ${this.getDomainTypeLabel(domainType)} type`;
    }
    if (actionType.startsWith('ACTIVITY_FILE_')) {
      return `${capitalize(actionType.replace('ACTIVITY_FILE_', ''))} ${this.getDomainTypeLabel(domainType)} file`;
    }
    if (actionType.startsWith('ACTIVITY_')) {
      return `${capitalize(actionType.replace('ACTIVITY_', ''))} ${this.getDomainTypeLabel(domainType)} activity`;
    }
    if (actionType.startsWith('JOB_PROCESS_')) {
      return `${capitalize(actionType.replace('JOB_PROCESS_', ''))} ${this.getDomainTypeLabel(domainType)} job process`;
    }
    if (actionType.startsWith('JOB_')) {
      return `${capitalize(actionType.replace('JOB_', ''))} ${this.getDomainTypeLabel(domainType)} job`;
    }
    if (domainType === DomainType.USER_PROFILE) {
      return `${capitalize(actionType.split('_').join(' '))}`;
    }
    if (actionType === 'WABA_AUTO_REPLY_MESSAGE_UPDATE') {
      return `Update ${this.getDomainTypeLabel(domainType)}`;
    }
    if (
      actionType === 'WABA_WEBHOOK_REGISTERED' ||
      actionType === 'WABA_WEBHOOK_UNREGISTERED' ||
      actionType === 'WABA_UPDATE'
    ) {
      return `Update ${this.getDomainTypeLabel(domainType)}`;
    }

    return `${capitalize(actionType.split('_').join(' '))} ${this.getDomainTypeLabel(domainType)}`;
  }

  getDomainTypeLabel(domainType: DomainType) {
    return lower(domainType.split('_').join(' '));
  }

  doNavigate(log: AuditLogModel) {
    const routeInformation = this.getRouteInformation(log);

    if (routeInformation.path.length > 0) {
      this.navigate(routeInformation.path, routeInformation.queryParams);
    }
  }

  getRouteInformation(log: AuditLogModel): { path: (string | number)[]; queryParams?: Params } {
    if (
      log.domainType === DomainType.DIGITAL_FILING ||
      log.domainType === DomainType.TRANSACTIONAL_EMAIL ||
      log.domainType === DomainType.EMAIL_CAMPAIGN ||
      log.domainType === DomainType.WHATSAPP ||
      log.domainType === DomainType.MT_WHATSAPP ||
      log.domainType === DomainType.MT_TRANSACTIONAL_EMAIL
    ) {
      let domainPath = 'transactional-email';
      if (log.domainType === DomainType.DIGITAL_FILING) {
        domainPath = 'digital-filing';
      }
      if (log.domainType === DomainType.EMAIL_CAMPAIGN) {
        domainPath = 'email-campaign';
      }
      if (log.domainType === DomainType.WHATSAPP) {
        domainPath = 'whatsapp';
      }
      if (log.domainType === DomainType.MT_WHATSAPP) {
        domainPath = 'mt-whatsapp';
      }
      if (log.domainType === DomainType.MT_TRANSACTIONAL_EMAIL) {
        domainPath = 'mt-transactional-email';
      }

      if (log.actionType.includes('_DELETE')) {
        return { path: [] };
      }

      if (log.actionType.startsWith('TYPE_')) {
        return {
          path: [domainPath, 'detail', log.targetId],
          queryParams: { tab: 'activities' },
        };
      }

      if (log.parentTypeId && log.actionType.startsWith('ACTIVITY_')) {
        return {
          path: [domainPath, 'detail', log.parentTypeId, 'activity', log.targetId],
          queryParams: { tab: 'info' },
        };
      }

      return { path: [] };
    } else {
      if (
        log.actionType === 'WABA_WEBHOOK_REGISTERED' ||
        log.actionType === 'WABA_WEBHOOK_UNREGISTERED' ||
        log.actionType === 'WABA_UPDATE'
      ) {
        return {
          path: ['/', 'account', 'whatsapp-settings'],
          queryParams: { tab: 'waba' },
        };
      }
      if (log.actionType === 'WABA_AUTO_REPLY_MESSAGE_UPDATE') {
        return {
          path: ['/', 'account', 'whatsapp-settings'],
          queryParams: { tab: 'auto-reply' },
        };
      }
      if (log.actionType === 'SFTP_UPDATE') {
        return {
          path: ['/', 'account', 'sftp-settings'],
        };
      }

      const domainRouteInfo = AllRoutes.filter((route) => route.domainType === log.domainType)[0];
      return { path: domainRouteInfo.path, queryParams: domainRouteInfo.queryParams };
    }
  }

  getDateFormat() {
    return environment.config.dateFormat;
  }
}
