import produce from 'immer';
import { Injectable } from '@angular/core';
import { of, switchMap, tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel, BaseActivitySftpValidationSummaryModel,
  BaseIndexRowModel,
  DigitalFilingActivityBasicModel,
  DigitalFilingActivityModel,
  DigitalFilingFilesPayloadModel,
  DigitalFilingTypeBasicModel,
  DigitalFilingTypeModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel
} from "@grabbill/lib";
import { DigitalFilingStateModel } from './digital-filing.state-model';
import { DigitalFilingApi } from '../../api/digital-filing.api';
import {
  DeleteDigitalFilingActivity,
  DeleteDigitalFilingFile,
  DeleteDigitalFilingFiles,
  DeleteDigitalFilingType,
  DownloadDigitalFilingFile,
  DownloadDigitalFilingFiles,
  DuplicateDigitalFilingType,
  ExportDigitalFilingActivity,
  GetDigitalFilingActivity,
  GetDigitalFilingType,
  LoadMoreDigitalFilingActivities,
  LoadMoreDigitalFilingTypes,
  NewDigitalFilingActivity,
  NewDigitalFilingType,
  PurgeDigitalFilingActivity,
  QueryDigitalFilingActivities,
  QueryDigitalFilingTypeFiles,
  QueryDigitalFilingTypes,
  ResetDigitalFilingActivity,
  ResetDigitalFilingType,
  ResetDigitalFilingTypes,
  UpdateDigitalFilingActivity,
  UpdateDigitalFilingType,
  UploadDigitalFilingFile,
  ValidateDigitalFilingActivitySftp
} from "./digital-filing.state-actions";

@State<DigitalFilingStateModel>({
  name: 'digital_filing',
  defaults: {
    digitalFilingTypePageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    digitalFilingTypeSearchResult: makeSearchResultPayload(),
    digitalFilingActivityPageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    digitalFilingActivitySearchResult: makeSearchResultPayload(),
    digitalFilingActivityFiles: [],
    digitalFilingFilePageable: makePageable(10),
    digitalFilingFileSearchResult: makeSearchResultPayload(),
    fileFilters: {},
  },
})
@Injectable()
export class DigitalFilingState {
  constructor(private digitalFilingApi: DigitalFilingApi) {}

  @Selector()
  static digitalFilingTypeSearchResult(state: DigitalFilingStateModel) {
    return state.digitalFilingTypeSearchResult;
  }

  @Selector()
  static digitalFilingTypePageable(state: DigitalFilingStateModel) {
    return state.digitalFilingTypePageable;
  }

  @Selector()
  static digitalFilingType(state: DigitalFilingStateModel) {
    return state.digitalFilingType;
  }

  @Selector()
  static typeName(state: DigitalFilingStateModel) {
    return state.typeName;
  }

  @Selector()
  static activityName(state: DigitalFilingStateModel) {
    return state.activityName;
  }

  @Selector()
  static file(state: DigitalFilingStateModel) {
    return state.file;
  }

  @Selector()
  static digitalFilingActivityPageable(state: DigitalFilingStateModel) {
    return state.digitalFilingActivityPageable;
  }

  @Selector()
  static digitalFilingActivitySearchResult(state: DigitalFilingStateModel) {
    return state.digitalFilingActivitySearchResult;
  }

  @Selector()
  static digitalFilingActivity(state: DigitalFilingStateModel) {
    return state.digitalFilingActivity;
  }

  @Selector()
  static digitalFilingActivityFiles(state: DigitalFilingStateModel) {
    return state.digitalFilingActivityFiles;
  }

  @Selector()
  static fileName(state: DigitalFilingStateModel) {
    return state.fileName;
  }

  @Selector()
  static fileFilters(state: DigitalFilingStateModel) {
    return state.fileFilters;
  }

  @Selector()
  static digitalFilingFilePageable(state: DigitalFilingStateModel) {
    return state.digitalFilingFilePageable;
  }

  @Selector()
  static digitalFilingFileSearchResult(state: DigitalFilingStateModel) {
    return state.digitalFilingFileSearchResult;
  }

  @Selector()
  static digitalFilingActivityError(state: DigitalFilingStateModel) {
    return state.digitalFilingActivityError;
  }

  @Action(ResetDigitalFilingTypes)
  resetDigitalFilingTypes(context: StateContext<DigitalFilingStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.typeName = undefined;
        draft.digitalFilingTypePageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.digitalFilingTypeSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QueryDigitalFilingTypes)
  queryDigitalFilingTypes(context: StateContext<DigitalFilingStateModel>, { pageable, name }: QueryDigitalFilingTypes) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.digitalFilingTypePageable = pageable ? pageable : draft.digitalFilingTypePageable;
        draft.typeName = name !== undefined ? name : draft.typeName;
      })
    );

    return this.digitalFilingApi
      .queryTypes(context.getState().digitalFilingTypePageable, context.getState().typeName)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<DigitalFilingTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.digitalFilingTypeSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(LoadMoreDigitalFilingTypes)
  loadMoreDigitalFilingTypes(context: StateContext<DigitalFilingStateModel>) {
    const pageable = context.getState().digitalFilingTypePageable;
    const page = pageable.page;
    const totalPages = context.getState().digitalFilingTypeSearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.digitalFilingTypePageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.digitalFilingApi
        .queryTypes(context.getState().digitalFilingTypePageable, context.getState().typeName)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<DigitalFilingTypeBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.digitalFilingTypeSearchResult = {
                  items: [...draft.digitalFilingTypeSearchResult.items, ...response.data.items],
                  totalItems: response.data.totalItems,
                  totalPages: response.data.totalPages,
                };
              })
            );
          })
        );
    } else {
      return of(null);
    }
  }

  @Action(GetDigitalFilingType)
  getDigitalFilingType(context: StateContext<DigitalFilingStateModel>, { typeId }: GetDigitalFilingType) {
    return this.digitalFilingApi.getType(typeId).pipe(
      tap((response: ApiResponseModel<DigitalFilingTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingType = response.data;
          })
        );
      })
    );
  }

  @Action(ResetDigitalFilingType)
  resetDigitalFilingType(context: StateContext<DigitalFilingStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.fileFilters = {};
        draft.fileName = undefined;
        draft.activityName = undefined;
        draft.digitalFilingType = undefined;
        draft.digitalFilingActivityPageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.digitalFilingActivitySearchResult = makeSearchResultPayload();
        draft.digitalFilingFilePageable = makePageable(10);
        draft.digitalFilingFileSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(NewDigitalFilingType)
  newDigitalFilingType(context: StateContext<DigitalFilingStateModel>, { request }: NewDigitalFilingType) {
    return this.digitalFilingApi.newType(request).pipe(
      tap((response: ApiResponseModel<DigitalFilingTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingType = response.data;
          })
        );
      })
    );
  }

  @Action(DuplicateDigitalFilingType)
  duplicateDigitalFilingType(context: StateContext<DigitalFilingStateModel>, { typeId }: DuplicateDigitalFilingType) {
    return this.digitalFilingApi.getType(typeId).pipe(
      switchMap((res) => {
        return this.digitalFilingApi.duplicateType(res.data).pipe(
          tap((response: ApiResponseModel<DigitalFilingTypeModel>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.digitalFilingType = response.data;
              })
            );
          })
        );
      })
    );
  }

  @Action(UpdateDigitalFilingType)
  updateDigitalFilingType(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, request }: UpdateDigitalFilingType
  ) {
    return this.digitalFilingApi.updateType(typeId, request);
  }

  @Action(DeleteDigitalFilingType)
  deleteDigitalFilingType(context: StateContext<DigitalFilingStateModel>, { typeId }: DeleteDigitalFilingType) {
    return this.digitalFilingApi.deleteType(typeId).pipe(tap(() => context.dispatch(new QueryDigitalFilingTypes())));
  }

  @Action(QueryDigitalFilingActivities)
  queryDigitalFilingActivities(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, pageable, name }: QueryDigitalFilingActivities
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.digitalFilingActivityPageable = pageable ? pageable : draft.digitalFilingActivityPageable;
        draft.activityName = name !== undefined ? name : draft.activityName;
      })
    );

    return this.digitalFilingApi.queryActivities(typeId, context.getState().digitalFilingActivityPageable).pipe(
      tap((response: ApiResponseModel<SearchResultPayloadModel<DigitalFilingActivityBasicModel>>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingActivitySearchResult = response.data;
          })
        );
      })
    );
  }

  @Action(LoadMoreDigitalFilingActivities)
  loadMoreDigitalFilingActivities(
    context: StateContext<DigitalFilingStateModel>,
    { typeId }: LoadMoreDigitalFilingActivities
  ) {
    const pageable = context.getState().digitalFilingActivityPageable;
    const page = pageable.page;
    const totalPages = context.getState().digitalFilingActivitySearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.digitalFilingActivityPageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.digitalFilingApi.queryActivities(typeId, context.getState().digitalFilingActivityPageable).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<DigitalFilingActivityBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.digitalFilingActivitySearchResult = {
                items: [...draft.digitalFilingActivitySearchResult.items, ...response.data.items],
                totalItems: response.data.totalItems,
                totalPages: response.data.totalPages,
              };
            })
          );
        })
      );
    } else {
      return of(null);
    }
  }

  @Action(GetDigitalFilingActivity)
  getDigitalFilingActivity(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, activityId }: GetDigitalFilingActivity
  ) {
    return this.digitalFilingApi.getActivity(typeId, activityId).pipe(
      tap((response: ApiResponseModel<DigitalFilingActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingActivity = response.data;
            draft.digitalFilingActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ResetDigitalFilingActivity)
  resetDigitalFilingActivity(context: StateContext<DigitalFilingStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.digitalFilingType = undefined;
        draft.digitalFilingActivity = undefined;
        draft.digitalFilingActivityError = undefined;
        draft.digitalFilingActivityFiles = [];
      })
    );
  }

  @Action(NewDigitalFilingActivity)
  newDigitalFilingActivity(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, request }: NewDigitalFilingActivity
  ) {
    return this.digitalFilingApi.newActivity(typeId, request).pipe(
      tap((response: ApiResponseModel<DigitalFilingActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingActivity = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateDigitalFilingActivity)
  updateDigitalFilingActivity(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, activityId, request }: UpdateDigitalFilingActivity
  ) {
    return this.digitalFilingApi.updateActivity(typeId, activityId, request).pipe(
      tap((response: ApiResponseModel<DigitalFilingActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingActivity = response.data;
          })
        );
      })
    );
  }

  @Action(ValidateDigitalFilingActivitySftp)
  validateDigitalFilingActivitySftp(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, activityId, request }: ValidateDigitalFilingActivitySftp
  ) {
    return this.digitalFilingApi.validateSftp(typeId, activityId, request).pipe(
      tap((response: ApiResponseModel<BaseActivitySftpValidationSummaryModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingActivityError = response.data;
          })
        );
      })
    );
  }

  @Action(DeleteDigitalFilingActivity)
  deleteDigitalFilingActivity(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, activityId }: DeleteDigitalFilingActivity
  ) {
    return this.digitalFilingApi
      .deleteActivity(typeId, activityId)
      .pipe(tap(() => context.dispatch(new QueryDigitalFilingActivities(typeId))));
  }

  @Action(UploadDigitalFilingFile)
  uploadDigitalFilingFile(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, activityId, fileFormData }: UploadDigitalFilingFile
  ) {
    return this.digitalFilingApi.uploadFile(typeId, activityId, fileFormData).pipe(
      tap((response: ApiResponseModel<DigitalFilingFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(DownloadDigitalFilingFile)
  downloadDigitalFilingFile(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, activityId, fileId }: DownloadDigitalFilingFile
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.digitalFilingApi.downloadFile(typeId, activityId, fileId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(DownloadDigitalFilingFiles)
  downloadDigitalFilingFiles(context: StateContext<DigitalFilingStateModel>, { typeId }: DownloadDigitalFilingFiles) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.digitalFilingApi.downloadFiles(typeId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(DeleteDigitalFilingFile)
  deleteDigitalFilingFile(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, activityId, fileId }: DeleteDigitalFilingFile
  ) {
    return this.digitalFilingApi.deleteFile(typeId, activityId, fileId).pipe(
      tap((response: ApiResponseModel<DigitalFilingFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(DeleteDigitalFilingFiles)
  deleteDigitalFilingFiles(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, activityId }: DeleteDigitalFilingFiles
  ) {
    return this.digitalFilingApi.deleteFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<DigitalFilingFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(PurgeDigitalFilingActivity)
  purgeDigitalFilingActivity(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, activityId }: PurgeDigitalFilingActivity
  ) {
    return this.digitalFilingApi.purgeFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<DigitalFilingActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.digitalFilingActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ExportDigitalFilingActivity)
  exportDigitalFilingActivity(
    context: StateContext<DigitalFilingStateModel>,
    { typeId, activityId }: ExportDigitalFilingActivity
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.digitalFilingApi.exportFile(typeId, activityId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(QueryDigitalFilingTypeFiles)
  queryDigitalFilingTypeFiles(
    context: StateContext<DigitalFilingStateModel>,
    { pageable, name, fileFilters, typeId }: QueryDigitalFilingTypeFiles
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.digitalFilingFilePageable = pageable ? pageable : draft.digitalFilingFilePageable;
        draft.fileName = name !== undefined ? name : draft.fileName;
        draft.fileFilters = fileFilters ? fileFilters : draft.fileFilters;
      })
    );

    return this.digitalFilingApi
      .queryFiles(
        context.getState().digitalFilingFilePageable,
        typeId,
        context.getState().fileName,
        context.getState().fileFilters
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.digitalFilingFileSearchResult = response.data;
            })
          );
        })
      );
  }
}
