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

export abstract class DigitalFilingApi {
  abstract queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<DigitalFilingTypeBasicModel>>>;

  abstract getType(typeId: number): Observable<ApiResponseModel<DigitalFilingTypeModel>>;

  abstract validateTypeName(name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newType(request: DigitalFilingTypeRequestModel): Observable<ApiResponseModel<DigitalFilingTypeModel>>;

  abstract duplicateType(request: DigitalFilingTypeRequestModel): Observable<ApiResponseModel<DigitalFilingTypeModel>>;

  abstract updateType(
    typeId: number,
    request: DigitalFilingTypeRequestModel
  ): Observable<ApiResponseModel<DigitalFilingTypeModel>>;

  abstract deleteType(typeId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract queryActivities(
    typeId: number,
    pageable: PageableModel,
    name?: string,
    status?: ProcessStatus
  ): Observable<ApiResponseModel<SearchResultPayloadModel<DigitalFilingActivityBasicModel>>>;

  abstract getActivity(typeId: number, activityId: number): Observable<ApiResponseModel<DigitalFilingActivityModel>>;

  abstract validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newActivity(
    typeId: number,
    request: DigitalFilingActivityCreateRequestModel
  ): Observable<ApiResponseModel<DigitalFilingActivityModel>>;

  abstract updateActivity(
    typeId: number,
    activityId: number,
    request: DigitalFilingActivityRequestModel
  ): Observable<ApiResponseModel<DigitalFilingActivityModel>>;

  abstract validateSftp(
    typeId: number,
    activityId: number,
    request: DigitalFilingActivityRequestModel
  ): Observable<ApiResponseModel<BaseActivitySftpValidationSummaryModel>>;

  abstract deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract purgeFiles(typeId: number, activityId: number): Observable<ApiResponseModel<DigitalFilingActivityModel>>;

  abstract deleteFiles(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<DigitalFilingFilesPayloadModel>>;

  abstract uploadFile(
    typeId: number,
    activityId: number,
    fileFormData: FormData
  ): Observable<ApiResponseModel<DigitalFilingFilesPayloadModel>>;

  abstract downloadReport(typeId: number, activityIds: number[], reportTypes: string[]): Observable<DownloadFile>;

  abstract downloadFile(typeId: number, activityId: number, fileId: number): Observable<DownloadFile>;

  abstract downloadFiles(typeId: number): Observable<DownloadFile>;

  abstract deleteFile(
    typeId: number,
    activityId: number,
    fileId: number
  ): Observable<ApiResponseModel<DigitalFilingFilesPayloadModel>>;

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
