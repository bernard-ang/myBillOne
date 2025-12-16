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

export abstract class EmailCampaignApi {
  abstract queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<EmailCampaignTypeBasicModel>>>;

  abstract getType(typeId: number): Observable<ApiResponseModel<EmailCampaignTypeModel>>;

  abstract validateTypeName(name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newType(request: EmailCampaignTypeRequestModel): Observable<ApiResponseModel<EmailCampaignTypeModel>>;

  abstract duplicateType(request: EmailCampaignTypeRequestModel): Observable<ApiResponseModel<EmailCampaignTypeModel>>;

  abstract updateType(
    typeId: number,
    request: EmailCampaignTypeRequestModel
  ): Observable<ApiResponseModel<EmailCampaignTypeModel>>;

  abstract deleteType(typeId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract queryActivities(
    typeId: number,
    pageable: PageableModel,
    name?: string,
    status?: ProcessStatus
  ): Observable<ApiResponseModel<SearchResultPayloadModel<EmailCampaignActivityBasicModel>>>;

  abstract getActivity(typeId: number, activityId: number): Observable<ApiResponseModel<EmailCampaignActivityModel>>;

  abstract validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newActivity(
    typeId: number,
    request: EmailCampaignActivityCreateRequestModel
  ): Observable<ApiResponseModel<EmailCampaignActivityModel>>;

  abstract updateActivity(
    typeId: number,
    activityId: number,
    request: EmailCampaignActivityRequestModel
  ): Observable<ApiResponseModel<EmailCampaignActivityModel>>;

  abstract deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract queryRecords(
    pageable: PageableModel,
    typeId: number,
    filterFilters?: { [index: string]: any }
  ): Observable<ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>>;

  abstract purgeFiles(typeId: number, activityId: number): Observable<ApiResponseModel<EmailCampaignActivityModel>>;

  abstract deleteFiles(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<EmailCampaignFilesPayloadModel>>;

  abstract uploadFile(
    typeId: number,
    activityId: number,
    fileFormData: FormData
  ): Observable<ApiResponseModel<EmailCampaignFilesPayloadModel>>;

  abstract downloadFile(typeId: number, activityId: number, fileId: number): Observable<DownloadFile>;

  abstract downloadReport(typeId: number, activityIds: number[], reportTypes: string[]): Observable<DownloadFile>;

  abstract deleteFile(
    typeId: number,
    activityId: number,
    fileId: number
  ): Observable<ApiResponseModel<EmailCampaignFilesPayloadModel>>;

  abstract exportFile(typeId: number, activityId: number): Observable<DownloadFile>;

  abstract queryFiles(
    pageable: PageableModel,
    typeId: number,
    fileName?: string,
    filterFilters?: { [index: string]: any }
  ): Observable<ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>>;
}
