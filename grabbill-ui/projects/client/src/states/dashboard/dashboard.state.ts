import { Injectable } from '@angular/core';
import produce from 'immer';
import { of, tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { ApiResponseModel, AuditLogModel, DashboardStatisticsModel, SearchResultPayloadModel } from '@grabbill/lib';
import { DashboardStateModel } from './dashboard-state.model';
import { DashboardApi } from '../../api/dashboard.api';
import {
  GetCurrentStatistics,
  LoadMoreRecentActivities,
  QueryRecentActivities,
  ResetRecentActivities,
} from './dashboard.state-actions';
import { makePageable, makeSearchResultPayload } from '../../../../lib/src/builders';

@State<DashboardStateModel>({
  name: 'dashboard',
  defaults: {
    recentActivitiesPageable: makePageable(),
    recentActivitiesSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class DashboardState {
  constructor(private dashboardApi: DashboardApi) {}

  @Selector()
  static currentStatistics(state: DashboardStateModel) {
    return state.currentStatistics;
  }

  @Selector()
  static recentActivitiesSearchResult(state: DashboardStateModel) {
    return state.recentActivitiesSearchResult;
  }

  @Selector()
  static recentActivitiesPageable(state: DashboardStateModel) {
    return state.recentActivitiesPageable;
  }

  @Action(GetCurrentStatistics)
  getCurrentStatistics(context: StateContext<DashboardStateModel>) {
    return this.dashboardApi.getCurrentStatistics().pipe(
      tap((response: ApiResponseModel<DashboardStatisticsModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.currentStatistics = response.data;
          })
        );
      })
    );
  }

  @Action(ResetRecentActivities)
  resetRecentActivities(context: StateContext<DashboardStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.recentActivitiesPageable = makePageable();
        draft.recentActivitiesSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QueryRecentActivities)
  queryRecentActivities(context: StateContext<DashboardStateModel>, { pageable }: QueryRecentActivities) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.recentActivitiesPageable = pageable ? pageable : draft.recentActivitiesPageable;
      })
    );

    return this.dashboardApi.searchRecentActivities(context.getState().recentActivitiesPageable).pipe(
      tap((response: ApiResponseModel<SearchResultPayloadModel<AuditLogModel>>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.recentActivitiesSearchResult = response.data;
          })
        );
      })
    );
  }

  @Action(LoadMoreRecentActivities)
  loadMoreRecentActivities(context: StateContext<DashboardStateModel>) {
    const pageable = context.getState().recentActivitiesPageable;
    const page = pageable.page;
    const totalPages = context.getState().recentActivitiesSearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.recentActivitiesPageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.dashboardApi.searchRecentActivities(context.getState().recentActivitiesPageable).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<AuditLogModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.recentActivitiesSearchResult = {
                items: [...draft.recentActivitiesSearchResult.items, ...response.data.items],
                totalItems: response.data.totalItems,
                totalPages: response.data.totalPages,
              };
            })
          );
        })
      );
    } else {
      return of(null);
    }
  }
}
