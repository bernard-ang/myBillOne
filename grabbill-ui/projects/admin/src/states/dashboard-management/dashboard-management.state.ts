import { Injectable } from '@angular/core';
import produce from 'immer';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { AdminDashboardStatisticsModel, ApiResponseModel } from '@grabbill/lib';
import { DashboardManagementStateModel } from './dashboard-management.state-model';
import { DashboardManagementApi } from '../../api/dashboard-management.api';
import { GetCurrentStatistics } from './dashboard-management.state-actions';

@State<DashboardManagementStateModel>({
  name: 'dashboard_management',
  defaults: {},
})
@Injectable()
export class DashboardManagementState {
  constructor(private dashboardManagementApi: DashboardManagementApi) {}

  @Selector()
  static currentStatistics(state: DashboardManagementStateModel) {
    return state.currentStatistics;
  }

  @Action(GetCurrentStatistics)
  getCurrentStatistics(context: StateContext<DashboardManagementStateModel>) {
    return this.dashboardManagementApi.getCurrentStatistics().pipe(
      tap((response: ApiResponseModel<AdminDashboardStatisticsModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.currentStatistics = response.data;
          })
        );
      })
    );
  }
}
