import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  BaseIndexRowModel,
  DownloadFile,
  EmailCampaignActivityBasicModel,
  EmailCampaignActivityCreateRequestModel,
  EmailCampaignActivityModel,
  EmailCampaignActivityRequestModel,
  EmailCampaignFilesPayloadModel,
  EmailCampaignTypeBasicModel,
  EmailCampaignTypeModel,
  EmailCampaignTypeRequestModel,
  NameCheckPayloadModel,
  PageableModel,
  ProcessStatus,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { EmailCampaignApi } from '../api/email-campaign.api';

@Injectable({
  providedIn: 'root',
})
export class EmailCampaignService implements EmailCampaignApi {
  readonly baseRoute = `ec-types`;

  constructor(private http: ApiHttpService) {}

  queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<EmailCampaignTypeBasicModel>>> {
    return this.http.query(this.baseRoute, pageable, { name: name });
  }

  getType(typeId: number): Observable<ApiResponseModel<EmailCampaignTypeModel>> {
    return this.http.get<ApiResponseModel<EmailCampaignTypeModel>>(`${this.baseRoute}/${typeId}`);
  }

  newType(request: EmailCampaignTypeRequestModel): Observable<ApiResponseModel<EmailCampaignTypeModel>> {
    return this.http.post<ApiResponseModel<EmailCampaignTypeModel>>(`${this.baseRoute}`, request);
  }

  duplicateType(request: EmailCampaignTypeRequestModel): Observable<ApiResponseModel<EmailCampaignTypeModel>> {
    return this.http.post<ApiResponseModel<EmailCampaignTypeModel>>(`${this.baseRoute}/duplicate`, request);
  }

  updateType(
    typeId: number,
    request: EmailCampaignTypeRequestModel
  ): Observable<ApiResponseModel<EmailCampaignTypeModel>> {
    return this.http.put<ApiResponseModel<EmailCampaignTypeModel>>(`${this.baseRoute}/${typeId}`, request);
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
  ): Observable<ApiResponseModel<SearchResultPayloadModel<EmailCampaignActivityBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<EmailCampaignActivityBasicModel>>>(
      `${this.baseRoute}/${typeId}/activities`,
      pageable,
      { name, status }
    );
  }

  getActivity(typeId: number, activityId: number): Observable<ApiResponseModel<EmailCampaignActivityModel>> {
    return this.http.get<ApiResponseModel<EmailCampaignActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`
    );
  }

  validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>> {
    return this.http.get(`${this.baseRoute}/${typeId}/activities/name/${encodeURIComponent(name)}`);
  }

  newActivity(
    typeId: number,
    request: EmailCampaignActivityCreateRequestModel
  ): Observable<ApiResponseModel<EmailCampaignActivityModel>> {
    return this.http.post<ApiResponseModel<EmailCampaignActivityModel>>(
      `${this.baseRoute}/${typeId}/activities`,
      request
    );
  }

  updateActivity(
    typeId: number,
    activityId: number,
    request: EmailCampaignActivityRequestModel
  ): Observable<ApiResponseModel<EmailCampaignActivityModel>> {
    return this.http.put<ApiResponseModel<EmailCampaignActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`,
      request
    );
  }

  deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${typeId}/activities/${activityId}`);
  }

  purgeFiles(typeId: number, activityId: number): Observable<ApiResponseModel<EmailCampaignActivityModel>> {
    return this.http.delete<ApiResponseModel<EmailCampaignActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/purge/files`
    );
  }

  deleteFiles(typeId: number, activityId: number): Observable<ApiResponseModel<EmailCampaignFilesPayloadModel>> {
    return this.http.delete<ApiResponseModel<EmailCampaignFilesPayloadModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/files`
    );
  }

  uploadFile(
    typeId: number,
    activityId: number,
    fileFormData: FormData
  ): Observable<ApiResponseModel<EmailCampaignFilesPayloadModel>> {
    return this.http.post<ApiResponseModel<EmailCampaignFilesPayloadModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/files`,
      fileFormData
    );
  }

  downloadFile(typeId: number, activityId: number, fileId: number): Observable<DownloadFile> {
    return this.http.getBlob(`${this.baseRoute}/${typeId}/activities/${activityId}/files/${fileId}/download`);
  }

  exportFile(typeId: number, activityId: number): Observable<DownloadFile> {
    return this.http.getBlob(`${this.baseRoute}/${typeId}/activities/${activityId}/export`);
  }

  deleteFile(
    typeId: number,
    activityId: number,
    fileId: number
  ): Observable<ApiResponseModel<EmailCampaignFilesPayloadModel>> {
    return this.http.delete<ApiResponseModel<EmailCampaignFilesPayloadModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/files/${fileId}`
    );
  }

  queryFiles(
    pageable: PageableModel,
    typeId: number,
    fileName?: string,
    filterFilters?: { [index: string]: any }
  ): Observable<ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>>(
      `${this.baseRoute}/${typeId}/files`,
      pageable,
      { idxf2: fileName, ...filterFilters },
      { localDate: true }
    );
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
}
