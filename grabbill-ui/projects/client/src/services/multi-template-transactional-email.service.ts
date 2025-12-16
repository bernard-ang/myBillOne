import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  BaseActivitySftpValidationSummaryModel,
  BaseIndexRowModel,
  DownloadFile,
  MultiTemplateTransactionalEmailActivityCreateRequestModel,
  MultiTemplateTransactionalEmailActivityModel,
  MultiTemplateTransactionalEmailActivityRequestModel,
  MultiTemplateTransactionalEmailTypeModel,
  MultiTemplateTransactionalEmailTypeRequestModel,
  NameCheckPayloadModel,
  PageableModel,
  ProcessStatus,
  SearchResultPayloadModel,
  TransactionalEmailActivityBasicModel,
  TransactionalEmailFilesPayloadModel,
  TransactionalEmailTypeBasicModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { MultiTemplateTransactionalEmailApi } from '../api/multi-template-transactional-email.api';

@Injectable({
  providedIn: 'root',
})
export class MultiTemplateTransactionalEmailService implements MultiTemplateTransactionalEmailApi {
  readonly baseRoute = `mt-txe-types`;

  constructor(private http: ApiHttpService) {}

  downloadReport(
    typeId: number,
    activityIds: number[],
    reportTypes: string[],
    encryptPassword: boolean
  ): Observable<DownloadFile> {
    return this.http.getBlob(
      `${this.baseRoute}/${typeId}/reports?activityIds=${activityIds.join(',')}` +
        `&reportTypes=${reportTypes.join(',')}` +
        `&encryptAttachmentPassword=${encryptPassword}` +
        `&tz=${Intl.DateTimeFormat().resolvedOptions().timeZone}`
    );
  }

  queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<TransactionalEmailTypeBasicModel>>> {
    return this.http.query(this.baseRoute, pageable, { name: name });
  }

  getType(typeId: number): Observable<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>> {
    return this.http.get<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>>(`${this.baseRoute}/${typeId}`);
  }

  newType(
    request: MultiTemplateTransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>> {
    return this.http.post<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>>(`${this.baseRoute}`, request);
  }

  duplicateType(
    request: MultiTemplateTransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>> {
    return this.http.post<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>>(
      `${this.baseRoute}/duplicate`,
      request
    );
  }

  updateType(
    typeId: number,
    request: MultiTemplateTransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>> {
    return this.http.put<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>>(
      `${this.baseRoute}/${typeId}`,
      request
    );
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
  ): Observable<ApiResponseModel<SearchResultPayloadModel<TransactionalEmailActivityBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<TransactionalEmailActivityBasicModel>>>(
      `${this.baseRoute}/${typeId}/activities`,
      pageable,
      { name: name, status: status }
    );
  }

  getActivity(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>> {
    return this.http.get<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`
    );
  }

  validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>> {
    return this.http.get(`${this.baseRoute}/${typeId}/activities/name/${encodeURIComponent(name)}`);
  }

  newActivity(
    typeId: number,
    request: MultiTemplateTransactionalEmailActivityCreateRequestModel
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>> {
    return this.http.post<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>>(
      `${this.baseRoute}/${typeId}/activities`,
      request
    );
  }

  updateActivity(
    typeId: number,
    activityId: number,
    request: MultiTemplateTransactionalEmailActivityRequestModel
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>> {
    return this.http.put<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`,
      request
    );
  }

  validateSftp(
    typeId: number,
    activityId: number,
    request: MultiTemplateTransactionalEmailActivityRequestModel
  ): Observable<ApiResponseModel<BaseActivitySftpValidationSummaryModel>> {
    return this.http.post<ApiResponseModel<BaseActivitySftpValidationSummaryModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/validate-sftp`,
      request
    );
  }

  deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${typeId}/activities/${activityId}`);
  }

  purgeFiles(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>> {
    return this.http.delete<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/purge/files`
    );
  }

  deleteFiles(typeId: number, activityId: number): Observable<ApiResponseModel<TransactionalEmailFilesPayloadModel>> {
    return this.http.delete<ApiResponseModel<TransactionalEmailFilesPayloadModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/files`
    );
  }

  uploadFile(
    typeId: number,
    activityId: number,
    fileFormData: FormData
  ): Observable<ApiResponseModel<TransactionalEmailFilesPayloadModel>> {
    return this.http.post<ApiResponseModel<TransactionalEmailFilesPayloadModel>>(
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
  ): Observable<ApiResponseModel<TransactionalEmailFilesPayloadModel>> {
    return this.http.delete<ApiResponseModel<TransactionalEmailFilesPayloadModel>>(
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
}
