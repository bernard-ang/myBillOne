import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel, BaseActivitySftpValidationSummaryModel,
  BaseIndexRowModel,
  DigitalFilingActivityBasicModel,
  DigitalFilingActivityCreateRequestModel,
  DigitalFilingActivityModel,
  DigitalFilingActivityRequestModel,
  DigitalFilingFilesPayloadModel,
  DigitalFilingTypeBasicModel,
  DigitalFilingTypeModel,
  DigitalFilingTypeRequestModel,
  DownloadFile,
  NameCheckPayloadModel,
  PageableModel,
  ProcessStatus,
  SearchResultPayloadModel
} from "@grabbill/lib";
import { DigitalFilingApi } from '../api/digital-filing.api';
import { ApiHttpService } from './api-http.service';

@Injectable({
  providedIn: 'root',
})
export class DigitalFilingService implements DigitalFilingApi {
  readonly baseRoute = `df-types`;

  constructor(private http: ApiHttpService) {}

  queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<DigitalFilingTypeBasicModel>>> {
    return this.http.query(this.baseRoute, pageable, { name: name });
  }

  getType(typeId: number): Observable<ApiResponseModel<DigitalFilingTypeModel>> {
    return this.http.get<ApiResponseModel<DigitalFilingTypeModel>>(`${this.baseRoute}/${typeId}`);
  }

  newType(request: DigitalFilingTypeRequestModel): Observable<ApiResponseModel<DigitalFilingTypeModel>> {
    return this.http.post<ApiResponseModel<DigitalFilingTypeModel>>(`${this.baseRoute}`, request);
  }

  duplicateType(request: DigitalFilingTypeRequestModel): Observable<ApiResponseModel<DigitalFilingTypeModel>> {
    return this.http.post<ApiResponseModel<DigitalFilingTypeModel>>(`${this.baseRoute}/duplicate`, request);
  }

  updateType(
    typeId: number,
    request: DigitalFilingTypeRequestModel
  ): Observable<ApiResponseModel<DigitalFilingTypeModel>> {
    return this.http.put<ApiResponseModel<DigitalFilingTypeModel>>(`${this.baseRoute}/${typeId}`, request);
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
  ): Observable<ApiResponseModel<SearchResultPayloadModel<DigitalFilingActivityBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<DigitalFilingActivityBasicModel>>>(
      `${this.baseRoute}/${typeId}/activities`,
      pageable,
      { name: name, status: status }
    );
  }

  getActivity(typeId: number, activityId: number): Observable<ApiResponseModel<DigitalFilingActivityModel>> {
    return this.http.get<ApiResponseModel<DigitalFilingActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`
    );
  }

  validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>> {
    return this.http.get(`${this.baseRoute}/${typeId}/activities/name/${encodeURIComponent(name)}`);
  }

  newActivity(
    typeId: number,
    request: DigitalFilingActivityCreateRequestModel
  ): Observable<ApiResponseModel<DigitalFilingActivityModel>> {
    return this.http.post<ApiResponseModel<DigitalFilingActivityModel>>(
      `${this.baseRoute}/${typeId}/activities`,
      request
    );
  }

  updateActivity(
    typeId: number,
    activityId: number,
    request: DigitalFilingActivityRequestModel
  ): Observable<ApiResponseModel<DigitalFilingActivityModel>> {
    return this.http.put<ApiResponseModel<DigitalFilingActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`,
      request
    );
  }

  validateSftp(
    typeId: number,
    activityId: number,
    request: DigitalFilingActivityRequestModel
  ): Observable<ApiResponseModel<BaseActivitySftpValidationSummaryModel>> {
    return this.http.post<ApiResponseModel<BaseActivitySftpValidationSummaryModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/validate-sftp`,
      request
    );
  }

  deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${typeId}/activities/${activityId}`);
  }

  purgeFiles(typeId: number, activityId: number): Observable<ApiResponseModel<DigitalFilingActivityModel>> {
    return this.http.delete<ApiResponseModel<DigitalFilingActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/purge/files`
    );
  }

  deleteFiles(typeId: number, activityId: number): Observable<ApiResponseModel<DigitalFilingFilesPayloadModel>> {
    return this.http.delete<ApiResponseModel<DigitalFilingFilesPayloadModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/files`
    );
  }

  uploadFile(
    typeId: number,
    activityId: number,
    fileFormData: FormData
  ): Observable<ApiResponseModel<DigitalFilingFilesPayloadModel>> {
    return this.http.post<ApiResponseModel<DigitalFilingFilesPayloadModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/files`,
      fileFormData
    );
  }

  downloadReport(typeId: number, activityIds: number[], reportTypes: string[]): Observable<DownloadFile> {
    return this.http.getBlob(
      `${this.baseRoute}/${typeId}/reports?activityIds=${activityIds.join(',')}` +
        `&reportTypes=${reportTypes.join(',')}&tz=${Intl.DateTimeFormat().resolvedOptions().timeZone}`
    );
  }

  downloadFile(typeId: number, activityId: number, fileId: number): Observable<DownloadFile> {
    return this.http.getBlob(`${this.baseRoute}/${typeId}/activities/${activityId}/files/${fileId}/download`);
  }

  downloadFiles(typeId: number): Observable<DownloadFile> {
    return this.http.getBlob(`${this.baseRoute}/${typeId}/files?download=1`);
  }

  exportFile(typeId: number, activityId: number): Observable<DownloadFile> {
    return this.http.getBlob(`${this.baseRoute}/${typeId}/activities/${activityId}/export`);
  }

  deleteFile(
    typeId: number,
    activityId: number,
    fileId: number
  ): Observable<ApiResponseModel<DigitalFilingFilesPayloadModel>> {
    return this.http.delete<ApiResponseModel<DigitalFilingFilesPayloadModel>>(
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
      { idxf1: fileName, ...filterFilters },
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
}
