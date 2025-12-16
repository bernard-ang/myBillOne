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
  TransactionalEmailActivityBasicModel,
  TransactionalEmailActivityCreateRequestModel,
  TransactionalEmailActivityModel,
  TransactionalEmailActivityRequestModel,
  TransactionalEmailFilesPayloadModel,
  TransactionalEmailTypeBasicModel,
  TransactionalEmailTypeModel,
  TransactionalEmailTypeRequestModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { TransactionalEmailApi } from '../api/transactional-email.api';

@Injectable({
  providedIn: 'root',
})
export class TransactionalEmailService implements TransactionalEmailApi {
  readonly baseRoute = `txe-types`;

  constructor(private http: ApiHttpService) {}

  downloadReport(typeId: number, activityIds: number[], reportTypes: string[], encryptPassword: boolean): Observable<DownloadFile> {
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

  getType(typeId: number): Observable<ApiResponseModel<TransactionalEmailTypeModel>> {
    return this.http.get<ApiResponseModel<TransactionalEmailTypeModel>>(`${this.baseRoute}/${typeId}`);
  }

  newType(request: TransactionalEmailTypeRequestModel): Observable<ApiResponseModel<TransactionalEmailTypeModel>> {
    return this.http.post<ApiResponseModel<TransactionalEmailTypeModel>>(`${this.baseRoute}`, request);
  }

  duplicateType(
    request: TransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<TransactionalEmailTypeModel>> {
    return this.http.post<ApiResponseModel<TransactionalEmailTypeModel>>(`${this.baseRoute}/duplicate`, request);
  }

  updateType(
    typeId: number,
    request: TransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<TransactionalEmailTypeModel>> {
    return this.http.put<ApiResponseModel<TransactionalEmailTypeModel>>(`${this.baseRoute}/${typeId}`, request);
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

  getActivity(typeId: number, activityId: number): Observable<ApiResponseModel<TransactionalEmailActivityModel>> {
    return this.http.get<ApiResponseModel<TransactionalEmailActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`
    );
  }

  validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>> {
    return this.http.get(`${this.baseRoute}/${typeId}/activities/name/${encodeURIComponent(name)}`);
  }

  newActivity(
    typeId: number,
    request: TransactionalEmailActivityCreateRequestModel
  ): Observable<ApiResponseModel<TransactionalEmailActivityModel>> {
    return this.http.post<ApiResponseModel<TransactionalEmailActivityModel>>(
      `${this.baseRoute}/${typeId}/activities`,
      request
    );
  }

  updateActivity(
    typeId: number,
    activityId: number,
    request: TransactionalEmailActivityRequestModel
  ): Observable<ApiResponseModel<TransactionalEmailActivityModel>> {
    return this.http.put<ApiResponseModel<TransactionalEmailActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`,
      request
    );
  }

  deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${typeId}/activities/${activityId}`);
  }

  purgeFiles(typeId: number, activityId: number): Observable<ApiResponseModel<TransactionalEmailActivityModel>> {
    return this.http.delete<ApiResponseModel<TransactionalEmailActivityModel>>(
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
