import { Observable } from 'rxjs';
import { ApiResponseModel, PlanModel } from '@grabbill/lib';

export abstract class AdminPlanApi {
  abstract getPlans(): Observable<ApiResponseModel<{ plans: PlanModel[] }>>;
}
