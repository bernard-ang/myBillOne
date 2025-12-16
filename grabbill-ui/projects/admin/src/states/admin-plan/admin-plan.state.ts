import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { ApiResponseModel, PlanModel } from '@grabbill/lib';
import { AdminPlanApi } from '../../api/admin-plan.api';
import { GetPlans } from './admin-plan.state-actions';
import { AdminPlanStateModel } from './admin-plan.state-model';

@State<AdminPlanStateModel>({
  name: 'admin_plan',
  defaults: {
    plans: [],
  },
})
@Injectable()
export class AdminPlanState {
  constructor(private planApi: AdminPlanApi) {}

  @Selector()
  static plans(state: AdminPlanStateModel) {
    return state.plans;
  }

  @Action(GetPlans)
  getPlans(context: StateContext<AdminPlanStateModel>) {
    return this.planApi.getPlans().pipe(
      tap((response: ApiResponseModel<{ plans: PlanModel[] }>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.plans = response.data.plans;
          })
        );
      })
    );
  }
}
