import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { ApiResponseModel, PlanModel } from '@grabbill/lib';
import { PlanApi } from '../../api/plan.api';
import { GetPlans } from './plan.state-actions';
import { PlanStateModel } from './plan.state-model';

@State<PlanStateModel>({
  name: 'plan',
  defaults: {
    plans: [],
  },
})
@Injectable()
export class PlanState {
  constructor(private planApi: PlanApi) {}

  @Selector()
  static plans(state: PlanStateModel) {
    return state.plans;
  }

  @Action(GetPlans)
  getPlans(context: StateContext<PlanStateModel>) {
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
