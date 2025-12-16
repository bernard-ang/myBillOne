import { Observable } from "rxjs";
import {
  ApiResponseModel,
  AuditLogModel,
  DashboardStatisticsModel,
  PageableModel,
  SearchResultPayloadModel
} from "@grabbill/lib";

export abstract class DashboardApi {
  abstract getCurrentStatistics (): Observable<ApiResponseModel<DashboardStatisticsModel>>;

  abstract searchRecentActivities (pageable: PageableModel): Observable<ApiResponseModel<SearchResultPayloadModel<AuditLogModel>>>;
}
