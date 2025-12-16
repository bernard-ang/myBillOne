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
  WhatsAppTypeBasicModel,
} from '@grabbill/lib';

export abstract class MultiTemplateWhatsappApi {
  abstract queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<WhatsAppTypeBasicModel>>>;

  abstract getType(typeId: number): Observable<ApiResponseModel<MultiTemplateWhatsappTypeModel>>;

  abstract validateTypeName(name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newType(
    request: MultiTemplateWhatsappTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateWhatsappTypeModel>>;

  abstract duplicateType(
    request: MultiTemplateWhatsappTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateWhatsappTypeModel>>;

  abstract updateType(
    typeId: number,
    request: MultiTemplateWhatsappTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateWhatsappTypeModel>>;

  abstract downloadReport(
    typeId: number,
    activityIds: number[],
    reportTypes: string[],
    encryptPassword: boolean
  ): Observable<DownloadFile>;

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
  ): Observable<ApiResponseModel<MultiTemplateWhatsappActivityModel>>;

  abstract validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newActivity(
    typeId: number,
    request: WhatsAppActivityCreateRequestModel
  ): Observable<ApiResponseModel<MultiTemplateWhatsappActivityModel>>;

  abstract updateActivity(
    typeId: number,
    activityId: number,
    request: WhatsAppActivityRequestModel
  ): Observable<ApiResponseModel<MultiTemplateWhatsappActivityModel>>;

  abstract validateSftp(
    typeId: number,
    activityId: number,
    request: WhatsAppActivityRequestModel
  ): Observable<ApiResponseModel<BaseActivitySftpValidationSummaryModel>>;

  abstract deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract purgeFiles(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<MultiTemplateWhatsappActivityModel>>;

  abstract deleteFiles(typeId: number, activityId: number): Observable<ApiResponseModel<WhatsAppFilesPayloadModel>>;

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
