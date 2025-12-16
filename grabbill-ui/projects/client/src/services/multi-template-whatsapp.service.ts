import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  BaseActivitySftpValidationSummaryModel,
  BaseIndexRowModel,
  DownloadFile,
  MultiTemplateWhatsappActivityModel,
  MultiTemplateWhatsappTypeModel,
  MultiTemplateWhatsappTypeRequestModel,
  NameCheckPayloadModel,
  PageableModel,
  ProcessStatus,
  SearchResultPayloadModel,
  WhatsAppActivityBasicModel,
  WhatsAppActivityCreateRequestModel,
  WhatsAppActivityRequestModel,
  WhatsAppFilesPayloadModel,
  WhatsAppTypeBasicModel
} from "@grabbill/lib";
import { ApiHttpService } from './api-http.service';
import { MultiTemplateWhatsappApi } from '../api/multi-template-whatsapp.api';

@Injectable({
  providedIn: 'root',
})
export class MultiTemplateWhatsappService implements MultiTemplateWhatsappApi {
  readonly baseRoute = `mt-whatsapp-types`;

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
  ): Observable<ApiResponseModel<SearchResultPayloadModel<WhatsAppTypeBasicModel>>> {
    return this.http.query(this.baseRoute, pageable, { name: name });
  }

  getType(typeId: number): Observable<ApiResponseModel<MultiTemplateWhatsappTypeModel>> {
    return this.http.get<ApiResponseModel<MultiTemplateWhatsappTypeModel>>(`${this.baseRoute}/${typeId}`);
  }

  newType(
    request: MultiTemplateWhatsappTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateWhatsappTypeModel>> {
    return this.http.post<ApiResponseModel<MultiTemplateWhatsappTypeModel>>(`${this.baseRoute}`, request);
  }

  duplicateType(
    request: MultiTemplateWhatsappTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateWhatsappTypeModel>> {
    return this.http.post<ApiResponseModel<MultiTemplateWhatsappTypeModel>>(`${this.baseRoute}/duplicate`, request);
  }

  updateType(
    typeId: number,
    request: MultiTemplateWhatsappTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateWhatsappTypeModel>> {
    return this.http.put<ApiResponseModel<MultiTemplateWhatsappTypeModel>>(`${this.baseRoute}/${typeId}`, request);
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
  ): Observable<ApiResponseModel<SearchResultPayloadModel<WhatsAppActivityBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<WhatsAppActivityBasicModel>>>(
      `${this.baseRoute}/${typeId}/activities`,
      pageable,
      { name: name, status: status }
    );
  }

  getActivity(typeId: number, activityId: number): Observable<ApiResponseModel<MultiTemplateWhatsappActivityModel>> {
    return this.http.get<ApiResponseModel<MultiTemplateWhatsappActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`
    );
  }

  validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>> {
    return this.http.get(`${this.baseRoute}/${typeId}/activities/name/${encodeURIComponent(name)}`);
  }

  newActivity(
    typeId: number,
    request: WhatsAppActivityCreateRequestModel
  ): Observable<ApiResponseModel<MultiTemplateWhatsappActivityModel>> {
    return this.http.post<ApiResponseModel<MultiTemplateWhatsappActivityModel>>(
      `${this.baseRoute}/${typeId}/activities`,
      request
    );
  }

  updateActivity(
    typeId: number,
    activityId: number,
    request: WhatsAppActivityRequestModel
  ): Observable<ApiResponseModel<MultiTemplateWhatsappActivityModel>> {
    return this.http.put<ApiResponseModel<MultiTemplateWhatsappActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`,
      request
    );
  }

  validateSftp(
    typeId: number,
    activityId: number,
    request: WhatsAppActivityRequestModel
  ): Observable<ApiResponseModel<BaseActivitySftpValidationSummaryModel>> {
    return this.http.post<ApiResponseModel<BaseActivitySftpValidationSummaryModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/validate-sftp`,
      request
    );
  }

  deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${typeId}/activities/${activityId}`);
  }

  purgeFiles(typeId: number, activityId: number): Observable<ApiResponseModel<MultiTemplateWhatsappActivityModel>> {
    return this.http.delete<ApiResponseModel<MultiTemplateWhatsappActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/purge/files`
    );
  }

  deleteFiles(typeId: number, activityId: number): Observable<ApiResponseModel<WhatsAppFilesPayloadModel>> {
    return this.http.delete<ApiResponseModel<WhatsAppFilesPayloadModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}/files`
    );
  }

  uploadFile(
    typeId: number,
    activityId: number,
    fileFormData: FormData
  ): Observable<ApiResponseModel<WhatsAppFilesPayloadModel>> {
    return this.http.post<ApiResponseModel<WhatsAppFilesPayloadModel>>(
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
  ): Observable<ApiResponseModel<WhatsAppFilesPayloadModel>> {
    return this.http.delete<ApiResponseModel<WhatsAppFilesPayloadModel>>(
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
