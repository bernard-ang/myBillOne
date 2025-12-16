import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseModel, PlanModel } from '@grabbill/lib';
import { AdminPlanApi } from '../api/admin-plan.api';
import { ApiHttpService } from './api-http.service';

@Injectable({
  providedIn: 'root',
})
export class AdminPlanService implements AdminPlanApi {
  readonly baseRoute = `plans`;

  constructor(private http: ApiHttpService) {}

  getPlans(): Observable<ApiResponseModel<{ plans: PlanModel[] }>> {
    return this.http.get<ApiResponseModel<{ plans: PlanModel[] }>>(this.baseRoute);
  }
}
