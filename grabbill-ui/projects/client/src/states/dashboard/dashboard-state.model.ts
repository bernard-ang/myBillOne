import { AuditLogModel, DashboardStatisticsModel, PageableModel, SearchResultPayloadModel } from "@grabbill/lib";

export interface DashboardStateModel {
  currentStatistics?: DashboardStatisticsModel;
  recentActivitiesPageable: PageableModel;
  recentActivitiesSearchResult: SearchResultPayloadModel<AuditLogModel>;
}
