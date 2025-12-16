import { Observable } from 'rxjs';
import { AdminDashboardStatisticsModel, ApiResponseModel } from '@grabbill/lib';

export abstract class DashboardManagementApi {
  abstract getCurrentStatistics(): Observable<ApiResponseModel<AdminDashboardStatisticsModel>>;
}
