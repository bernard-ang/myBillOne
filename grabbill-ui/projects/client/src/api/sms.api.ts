import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  BaseIndexRowModel,
  DownloadFile,
  NameCheckPayloadModel,
  PageableModel,
  ProcessStatus,
  SearchResultPayloadModel,
  SmsActivityBasicModel,
  SmsActivityCountCreditUsageRequestModel,
  SmsActivityCreateRequestModel,
  SmsActivityCreditUsagePayloadModel,
  SmsActivityModel,
  SmsActivityRequestModel,
  SmsTypeBasicModel,
  SmsTypeModel,
  SmsTypeRequestModel, SmsUsageSummaryPayloadModel
} from "@grabbill/lib";

export abstract class SmsApi {
  abstract queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<SmsTypeBasicModel>>>;

  abstract getType(typeId: number): Observable<ApiResponseModel<SmsTypeModel>>;

  abstract validateTypeName(name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newType(request: SmsTypeRequestModel): Observable<ApiResponseModel<SmsTypeModel>>;

  abstract duplicateType(request: SmsTypeRequestModel): Observable<ApiResponseModel<SmsTypeModel>>;

  abstract updateType(typeId: number, request: SmsTypeRequestModel): Observable<ApiResponseModel<SmsTypeModel>>;

  abstract deleteType(typeId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract queryActivities(
    typeId: number,
    pageable: PageableModel,
    name?: string,
    status?: ProcessStatus
  ): Observable<ApiResponseModel<SearchResultPayloadModel<SmsActivityBasicModel>>>;

  abstract getActivity(typeId: number, activityId: number): Observable<ApiResponseModel<SmsActivityModel>>;

  abstract validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newActivity(
    typeId: number,
    request: SmsActivityCreateRequestModel
  ): Observable<ApiResponseModel<SmsActivityModel>>;

  abstract updateActivity(
    typeId: number,
    activityId: number,
    request: SmsActivityRequestModel
  ): Observable<ApiResponseModel<SmsActivityModel>>;

  abstract deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract queryRecords(
    pageable: PageableModel,
    typeId: number,
    filterFilters?: { [index: string]: any }
  ): Observable<ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>>;

  abstract downloadReport(typeId: number, activityIds: number[], reportTypes: string[]): Observable<DownloadFile>;

  abstract countActivityCreditUsage(
    typeId: number,
    activityId: number,
    request: SmsActivityCountCreditUsageRequestModel
  ): Observable<ApiResponseModel<SmsActivityCreditUsagePayloadModel>>;

  abstract getUsageSummary(
    startDate: Date,
    endDate: Date,
  ): Observable<ApiResponseModel<SmsUsageSummaryPayloadModel>>;
}
