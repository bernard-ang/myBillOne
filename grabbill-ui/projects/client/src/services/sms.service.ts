import { Injectable } from '@angular/core';
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
  SmsTypeRequestModel,
  SmsUsageSummaryPayloadModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { SmsApi } from '../api/sms.api';

@Injectable({
  providedIn: 'root',
})
export class SmsService implements SmsApi {
  readonly baseRoute = `sms`;

  constructor(private http: ApiHttpService) {}

  queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<SmsTypeBasicModel>>> {
    return this.http.query(this.baseRoute, pageable, { name: name });
  }

  getType(typeId: number): Observable<ApiResponseModel<SmsTypeModel>> {
    return this.http.get<ApiResponseModel<SmsTypeModel>>(`${this.baseRoute}/${typeId}`);
  }

  newType(request: SmsTypeRequestModel): Observable<ApiResponseModel<SmsTypeModel>> {
    return this.http.post<ApiResponseModel<SmsTypeModel>>(`${this.baseRoute}`, request);
  }

  duplicateType(request: SmsTypeRequestModel): Observable<ApiResponseModel<SmsTypeModel>> {
    return this.http.post<ApiResponseModel<SmsTypeModel>>(`${this.baseRoute}/duplicate`, request);
  }

  updateType(typeId: number, request: SmsTypeRequestModel): Observable<ApiResponseModel<SmsTypeModel>> {
    return this.http.put<ApiResponseModel<SmsTypeModel>>(`${this.baseRoute}/${typeId}`, request);
  }

  deleteType(typeId: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${typeId}`);
  }

  validateTypeName(name: string): Observable<ApiResponseModel<NameCheckPayloadModel>> {
    return this.http.get(`${this.baseRoute}/name/${encodeURIComponent(name)}`);
  }

  queryActivities(
    typeId: number,
    pageable: PageableModel,
    name?: string,
    status?: ProcessStatus
  ): Observable<ApiResponseModel<SearchResultPayloadModel<SmsActivityBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<SmsActivityBasicModel>>>(
      `${this.baseRoute}/${typeId}/activities`,
      pageable,
      { name, status }
    );
  }

  getActivity(typeId: number, activityId: number): Observable<ApiResponseModel<SmsActivityModel>> {
    return this.http.get<ApiResponseModel<SmsActivityModel>>(`${this.baseRoute}/${typeId}/activities/${activityId}`);
  }

  validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>> {
    return this.http.get(`${this.baseRoute}/${typeId}/activities/name/${encodeURIComponent(name)}`);
  }

  newActivity(typeId: number, request: SmsActivityCreateRequestModel): Observable<ApiResponseModel<SmsActivityModel>> {
    return this.http.post<ApiResponseModel<SmsActivityModel>>(`${this.baseRoute}/${typeId}/activities`, request);
  }

  updateActivity(
    typeId: number,
    activityId: number,
    request: SmsActivityRequestModel
  ): Observable<ApiResponseModel<SmsActivityModel>> {
    return this.http.put<ApiResponseModel<SmsActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`,
      request
    );
  }

  deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${typeId}/activities/${activityId}`);
  }

  queryRecords(
    pageable: PageableModel,
    typeId: number,
    filterFilters?: { [index: string]: any }
  ): Observable<ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>>(
      `${this.baseRoute}/${typeId}/records`,
      pageable,
      filterFilters,
      { localDate: true }
    );
  }

  downloadReport(typeId: number, activityIds: number[], reportTypes: string[]): Observable<DownloadFile> {
    return this.http.getBlob(
      `${this.baseRoute}/${typeId}/reports?activityIds=${activityIds.join(',')}` +
        `&reportTypes=${reportTypes.join(',')}&tz=${Intl.DateTimeFormat().resolvedOptions().timeZone}`
    );
  }

  countActivityCreditUsage(
    typeId: number,
    activityId: number,
    request: SmsActivityCountCreditUsageRequestModel
  ): Observable<ApiResponseModel<SmsActivityCreditUsagePayloadModel>> {
    return this.http.post(`${this.baseRoute}/${typeId}/activities/${activityId}/count-credit-usage`, request);
  }

  getUsageSummary(startDate: Date, endDate: Date): Observable<ApiResponseModel<SmsUsageSummaryPayloadModel>> {
    startDate.setHours(0, 0, 0, 0);
    endDate.setHours(23, 59, 59, 9999);

    return this.http.get<ApiResponseModel<SmsUsageSummaryPayloadModel>>(`${this.baseRoute}/usage-summary`, {
      startDate,
      endDate,
    });
  }
}
