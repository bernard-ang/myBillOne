import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import {
  ApiMessage,
  ApiResponseModel,
  BaseIndexRowModel,
  DownloadFile,
  NameCheckPayloadModel,
  PageableModel,
  ProcessStatus,
  SearchResultPayloadModel,
  WhatsAppActivityBasicModel,
  WhatsAppActivityCreateRequestModel,
  WhatsAppActivityModel,
  WhatsAppActivityRequestModel,
  WhatsAppFilesPayloadModel,
  WhatsappTemplateModel,
  WhatsAppTemplatesPayloadModel,
  WhatsAppTypeBasicModel,
  WhatsAppTypeModel,
  WhatsAppTypeRequestModel
} from "@grabbill/lib";
import { ApiHttpService } from "./api-http.service";
import { WhatsAppApi } from "../api/whatsapp.api";

@Injectable({
  providedIn: 'root',
})
export class WhatsAppService implements WhatsAppApi {
  readonly baseRoute = `whatsapp`;

  constructor(private http: ApiHttpService) {}

  updateAutoReplyMessage(message: string): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.put<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/auto-reply-message`, {
      message
    });
  }

  getTemplates(): Observable<ApiResponseModel<WhatsAppTemplatesPayloadModel>> {
    return this.http.get(`${this.baseRoute}/templates`);
  }

  createTemplate(formData: FormData): Observable<ApiResponseModel<WhatsappTemplateModel>> {
    return this.http.post<ApiResponseModel<WhatsappTemplateModel>>(`${this.baseRoute}/templates`, formData);
  }

  deleteTemplate(id: string): Observable<ApiResponseModel<WhatsAppTemplatesPayloadModel>> {
    return this.http.delete<ApiResponseModel<WhatsAppTemplatesPayloadModel>>(`${this.baseRoute}/templates/${id}`);
  }

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
  ): Observable<ApiResponseModel<SearchResultPayloadModel<WhatsAppTypeBasicModel>>> {
    return this.http.query(this.baseRoute, pageable, { name: name });
  }

  getType(typeId: number): Observable<ApiResponseModel<WhatsAppTypeModel>> {
    return this.http.get<ApiResponseModel<WhatsAppTypeModel>>(`${this.baseRoute}/${typeId}`);
  }

  newType(request: WhatsAppTypeRequestModel): Observable<ApiResponseModel<WhatsAppTypeModel>> {
    return this.http.post<ApiResponseModel<WhatsAppTypeModel>>(`${this.baseRoute}`, request);
  }

  duplicateType(
    request: WhatsAppTypeRequestModel
  ): Observable<ApiResponseModel<WhatsAppTypeModel>> {
    return this.http.post<ApiResponseModel<WhatsAppTypeModel>>(`${this.baseRoute}/duplicate`, request);
  }

  updateType(
    typeId: number,
    request: WhatsAppTypeRequestModel
  ): Observable<ApiResponseModel<WhatsAppTypeModel>> {
    return this.http.put<ApiResponseModel<WhatsAppTypeModel>>(`${this.baseRoute}/${typeId}`, request);
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

  getActivity(typeId: number, activityId: number): Observable<ApiResponseModel<WhatsAppActivityModel>> {
    return this.http.get<ApiResponseModel<WhatsAppActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`
    );
  }

  validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>> {
    return this.http.get(`${this.baseRoute}/${typeId}/activities/name/${encodeURIComponent(name)}`);
  }

  newActivity(
    typeId: number,
    request: WhatsAppActivityCreateRequestModel
  ): Observable<ApiResponseModel<WhatsAppActivityModel>> {
    return this.http.post<ApiResponseModel<WhatsAppActivityModel>>(
      `${this.baseRoute}/${typeId}/activities`,
      request
    );
  }

  updateActivity(
    typeId: number,
    activityId: number,
    request: WhatsAppActivityRequestModel
  ): Observable<ApiResponseModel<WhatsAppActivityModel>> {
    return this.http.put<ApiResponseModel<WhatsAppActivityModel>>(
      `${this.baseRoute}/${typeId}/activities/${activityId}`,
      request
    );
  }

  deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${typeId}/activities/${activityId}`);
  }

  purgeFiles(typeId: number, activityId: number): Observable<ApiResponseModel<WhatsAppActivityModel>> {
    return this.http.delete<ApiResponseModel<WhatsAppActivityModel>>(
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
