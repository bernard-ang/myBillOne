import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminDashboardStatisticsModel, ApiResponseModel } from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { DashboardManagementApi } from '../api/dashboard-management.api';

@Injectable({
  providedIn: 'root',
})
export class DashboardManagementService implements DashboardManagementApi {
  readonly baseRoute = `mgmt/dashboard`;

  constructor(private http: ApiHttpService) {}

  getCurrentStatistics(): Observable<ApiResponseModel<AdminDashboardStatisticsModel>> {
    return this.http.get<ApiResponseModel<AdminDashboardStatisticsModel>>(`${this.baseRoute}/current-statistics`);
  }
}
