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
  WhatsAppActivityBasicModel,
  WhatsAppActivityCreateRequestModel,
  WhatsAppActivityModel,
  WhatsAppActivityRequestModel,
  WhatsappTemplateModel,
  WhatsAppTemplatesPayloadModel,
  WhatsAppTypeBasicModel,
  WhatsAppTypeModel,
  WhatsAppTypeRequestModel,
  WhatsAppFilesPayloadModel
} from "@grabbill/lib";

export abstract class WhatsAppApi {
  abstract getTemplates(): Observable<ApiResponseModel<WhatsAppTemplatesPayloadModel>>;

  abstract createTemplate(formData: FormData): Observable<ApiResponseModel<WhatsappTemplateModel>>;

  abstract deleteTemplate(id: string): Observable<ApiResponseModel<WhatsAppTemplatesPayloadModel>>;

  abstract updateAutoReplyMessage(message: string): Observable<ApiResponseModel<ApiMessage>>;


  abstract queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<WhatsAppTypeBasicModel>>>;

  abstract getType(typeId: number): Observable<ApiResponseModel<WhatsAppTypeModel>>;

  abstract validateTypeName(name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newType(
    request: WhatsAppTypeRequestModel
  ): Observable<ApiResponseModel<WhatsAppTypeModel>>;

  abstract duplicateType(
    request: WhatsAppTypeRequestModel
  ): Observable<ApiResponseModel<WhatsAppTypeModel>>;

  abstract updateType(
    typeId: number,
    request: WhatsAppTypeRequestModel
  ): Observable<ApiResponseModel<WhatsAppTypeModel>>;

  abstract downloadReport(typeId: number, activityIds: number[], reportTypes: string[], encryptPassword: boolean): Observable<DownloadFile>;

  abstract deleteType(typeId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract queryActivities(
    typeId: number,
    pageable: PageableModel,
    name?: string,
    status?: ProcessStatus
  ): Observable<ApiResponseModel<SearchResultPayloadModel<WhatsAppActivityBasicModel>>>;

  abstract getActivity(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<WhatsAppActivityModel>>;

  abstract validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newActivity(
    typeId: number,
    request: WhatsAppActivityCreateRequestModel
  ): Observable<ApiResponseModel<WhatsAppActivityModel>>;

  abstract updateActivity(
    typeId: number,
    activityId: number,
    request: WhatsAppActivityRequestModel
  ): Observable<ApiResponseModel<WhatsAppActivityModel>>;

  abstract deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract purgeFiles(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<WhatsAppActivityModel>>;

  abstract deleteFiles(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<WhatsAppFilesPayloadModel>>;

  abstract uploadFile(
    typeId: number,
    activityId: number,
    fileFormData: FormData
  ): Observable<ApiResponseModel<WhatsAppFilesPayloadModel>>;

  abstract downloadFile(typeId: number, activityId: number, fileId: number): Observable<DownloadFile>;

  abstract deleteFile(
    typeId: number,
    activityId: number,
    fileId: number
  ): Observable<ApiResponseModel<WhatsAppFilesPayloadModel>>;

  abstract exportFile(typeId: number, activityId: number): Observable<DownloadFile>;

  abstract queryFiles(
    pageable: PageableModel,
    typeId: number,
    fileName?: string,
    filterFilters?: { [index: string]: any }
  ): Observable<ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>>;

  abstract queryRecords(
    pageable: PageableModel,
    typeId: number,
    filterFilters?: { [index: string]: any }
  ): Observable<ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>>;
}
