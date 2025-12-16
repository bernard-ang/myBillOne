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

export abstract class TransactionalEmailApi {
  abstract queryTypes(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<TransactionalEmailTypeBasicModel>>>;

  abstract getType(typeId: number): Observable<ApiResponseModel<TransactionalEmailTypeModel>>;

  abstract validateTypeName(name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newType(
    request: TransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<TransactionalEmailTypeModel>>;

  abstract duplicateType(
    request: TransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<TransactionalEmailTypeModel>>;

  abstract updateType(
    typeId: number,
    request: TransactionalEmailTypeRequestModel
  ): Observable<ApiResponseModel<TransactionalEmailTypeModel>>;

  abstract downloadReport(typeId: number, activityIds: number[], reportTypes: string[], encryptPassword: boolean): Observable<DownloadFile>;

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
  ): Observable<ApiResponseModel<TransactionalEmailActivityModel>>;

  abstract validateActivityName(typeId: number, name: string): Observable<ApiResponseModel<NameCheckPayloadModel>>;

  abstract newActivity(
    typeId: number,
    request: TransactionalEmailActivityCreateRequestModel
  ): Observable<ApiResponseModel<TransactionalEmailActivityModel>>;

  abstract updateActivity(
    typeId: number,
    activityId: number,
    request: TransactionalEmailActivityRequestModel
  ): Observable<ApiResponseModel<TransactionalEmailActivityModel>>;

  abstract deleteActivity(typeId: number, activityId: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract purgeFiles(
    typeId: number,
    activityId: number
  ): Observable<ApiResponseModel<TransactionalEmailActivityModel>>;

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
}
