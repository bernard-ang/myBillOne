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

export abstract class MultiTemplateTransactionalEmailApi {
  abstract queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<TransactionalEmailTypeBasicModel>>>;

  abstract getType(typeId: number): Observable<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>>;

  abstract validateTypeName(name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newType(
    request: MultiTemplateTransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>>;

  abstract duplicateType(
    request: MultiTemplateTransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>>;

  abstract updateType(
    typeId: number,
    request: MultiTemplateTransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>>;

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
  ): Observable<ApiResponseModel<SearchResultPayloadModel<TransactionalEmailActivityBasicModel>>>;

  abstract getActivity(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>>;

  abstract validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newActivity(
    typeId: number,
    request: MultiTemplateTransactionalEmailActivityCreateRequestModel
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>>;

  abstract updateActivity(
    typeId: number,
    activityId: number,
    request: MultiTemplateTransactionalEmailActivityRequestModel
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>>;

  abstract deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract purgeFiles(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>>;

  abstract deleteFiles(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<TransactionalEmailFilesPayloadModel>>;

  abstract uploadFile(
    typeId: number,
    activityId: number,
    fileFormData: FormData
  ): Observable<ApiResponseModel<TransactionalEmailFilesPayloadModel>>;

  abstract downloadFile(typeId: number, activityId: number, fileId: number): Observable<DownloadFile>;

  abstract deleteFile(
    typeId: number,
    activityId: number,
    fileId: number
  ): Observable<ApiResponseModel<TransactionalEmailFilesPayloadModel>>;

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

  abstract validateSftp(
    typeId: number,
    activityId: number,
    request: MultiTemplateTransactionalEmailActivityRequestModel
  ): Observable<ApiResponseModel<BaseActivitySftpValidationSummaryModel>>;
}
