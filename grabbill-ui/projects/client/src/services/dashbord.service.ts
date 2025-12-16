import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import {
  ApiResponseModel,
  AuditLogModel,
  DashboardStatisticsModel,
  PageableModel,
  SearchResultPayloadModel
} from "@grabbill/lib";
import { ApiHttpService } from "./api-http.service";
import { DashboardApi } from "../api/dashboard.api";

@Injectable({
  providedIn: "root"
})
export class DashbordService implements DashboardApi {
  readonly baseRoute = `dashboard`;

  constructor (private http: ApiHttpService) {
  }

  getCurrentStatistics (): Observable<ApiResponseModel<DashboardStatisticsModel>> {
    return this.http.get(`${this.baseRoute}/current-statistics`);
  }

  searchRecentActivities (pageable: PageableModel): Observable<ApiResponseModel<SearchResultPayloadModel<AuditLogModel>>> {
    return this.http.query(`${this.baseRoute}/recent-activities`, pageable);
  }
}
