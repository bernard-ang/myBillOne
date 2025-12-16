import produce from 'immer';
import { Injectable } from '@angular/core';
import { of, switchMap, tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel, BaseActivitySftpValidationSummaryModel,
  BaseIndexRowModel,
  makePageable,
  makeSearchResultPayload,
  MultiTemplateTransactionalEmailActivityModel,
  MultiTemplateTransactionalEmailTypeModel,
  SearchResultPayloadModel,
  TransactionalEmailActivityBasicModel,
  TransactionalEmailFilesPayloadModel,
  TransactionalEmailTypeBasicModel
} from "@grabbill/lib";
import { MultiTemplateTransactionalEmailStateModel } from './multi-template-transactional-email.state-model';
import { MultiTemplateTransactionalEmailApi } from '../../api/multi-template-transactional-email.api';
import {
  DeleteMultiTemplateTransactionalEmailActivity,
  DeleteMultiTemplateTransactionalEmailFile,
  DeleteMultiTemplateTransactionalEmailFiles,
  DeleteMultiTemplateTransactionalEmailType,
  DownloadMultiTemplateTransactionalEmailFile,
  DuplicateMultiTemplateTransactionalEmailType,
  ExportMultiTemplateTransactionalEmailActivity,
  GetMultiTemplateTransactionalEmailActivity,
  GetMultiTemplateTransactionalEmailType,
  LoadMoreMultiTemplateTransactionalEmailActivities,
  LoadMoreMultiTemplateTransactionalEmailTypes,
  NewMultiTemplateTransactionalEmailActivity,
  NewMultiTemplateTransactionalEmailType,
  PurgeMultiTemplateTransactionalEmailActivity,
  QueryMultiTemplateTransactionalEmailActivities,
  QueryMultiTemplateTransactionalEmailTypeFiles,
  QueryMultiTemplateTransactionalEmailTypes,
  ResetMultiTemplateTransactionalEmailActivity,
  ResetMultiTemplateTransactionalEmailType,
  ResetMultiTemplateTransactionalEmailTypes,
  UpdateMultiTemplateTransactionalEmailActivity,
  UpdateMultiTemplateTransactionalEmailType,
  UploadMultiTemplateTransactionalEmailFile, ValidateMultiTemplateTransactionalEmailActivitySftp
} from "./multi-template-transactional-email.state-actions";
import {
  UpdateDigitalFilingActivity,
  ValidateDigitalFilingActivitySftp
} from "../digital-filing/digital-filing.state-actions";
import { DigitalFilingStateModel } from "../digital-filing/digital-filing.state-model";

@State<MultiTemplateTransactionalEmailStateModel>({
  name: 'multi_template_transactional_email',
  defaults: {
    transactionalEmailTypePageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    transactionalEmailTypeSearchResult: makeSearchResultPayload(),
    transactionalEmailActivityPageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    transactionalEmailActivitySearchResult: makeSearchResultPayload(),
    transactionalEmailActivityFiles: [],
    transactionalEmailFilePageable: makePageable(10),
    transactionalEmailFileSearchResult: makeSearchResultPayload(),
    fileFilters: {},
  },
})
@Injectable()
export class MultiTemplateTransactionalEmailState {
  constructor(private transactionalEmailApi: MultiTemplateTransactionalEmailApi) {}

  @Selector()
  static transactionalEmailTypeSearchResult(state: MultiTemplateTransactionalEmailStateModel) {
    return state.transactionalEmailTypeSearchResult;
  }

  @Selector()
  static transactionalEmailTypePageable(state: MultiTemplateTransactionalEmailStateModel) {
    return state.transactionalEmailTypePageable;
  }

  @Selector()
  static transactionalEmailType(state: MultiTemplateTransactionalEmailStateModel) {
    return state.transactionalEmailType;
  }

  @Selector()
  static typeName(state: MultiTemplateTransactionalEmailStateModel) {
    return state.typeName;
  }

  @Selector()
  static activityName(state: MultiTemplateTransactionalEmailStateModel) {
    return state.activityName;
  }

  @Selector()
  static file(state: MultiTemplateTransactionalEmailStateModel) {
    return state.file;
  }

  @Selector()
  static transactionalEmailActivityPageable(state: MultiTemplateTransactionalEmailStateModel) {
    return state.transactionalEmailActivityPageable;
  }

  @Selector()
  static transactionalEmailActivitySearchResult(state: MultiTemplateTransactionalEmailStateModel) {
    return state.transactionalEmailActivitySearchResult;
  }

  @Selector()
  static transactionalEmailActivity(state: MultiTemplateTransactionalEmailStateModel) {
    return state.transactionalEmailActivity;
  }

  @Selector()
  static transactionalEmailActivityFiles(state: MultiTemplateTransactionalEmailStateModel) {
    return state.transactionalEmailActivityFiles;
  }

  @Selector()
  static fileName(state: MultiTemplateTransactionalEmailStateModel) {
    return state.fileName;
  }

  @Selector()
  static fileFilters(state: MultiTemplateTransactionalEmailStateModel) {
    return state.fileFilters;
  }

  @Selector()
  static transactionalEmailFilePageable(state: MultiTemplateTransactionalEmailStateModel) {
    return state.transactionalEmailFilePageable;
  }

  @Selector()
  static transactionalEmailFileSearchResult(state: MultiTemplateTransactionalEmailStateModel) {
    return state.transactionalEmailFileSearchResult;
  }

  @Selector()
  static transactionalEmailActivityError(state: MultiTemplateTransactionalEmailStateModel) {
    return state.transactionalEmailActivityError;
  }

  @Action(ResetMultiTemplateTransactionalEmailTypes)
  resetTransactionalEmailTypes(context: StateContext<MultiTemplateTransactionalEmailStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.typeName = undefined;
        draft.transactionalEmailTypePageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.transactionalEmailTypeSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QueryMultiTemplateTransactionalEmailTypes)
  queryTransactionalEmailTypes(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { pageable, name }: QueryMultiTemplateTransactionalEmailTypes
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.transactionalEmailTypePageable = pageable ? pageable : draft.transactionalEmailTypePageable;
        draft.typeName = name !== undefined ? name : draft.typeName;
      })
    );

    return this.transactionalEmailApi
      .queryTypes(context.getState().transactionalEmailTypePageable, context.getState().typeName)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<TransactionalEmailTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.transactionalEmailTypeSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(LoadMoreMultiTemplateTransactionalEmailTypes)
  loadMoreTransactionalEmailTypes(context: StateContext<MultiTemplateTransactionalEmailStateModel>) {
    const pageable = context.getState().transactionalEmailTypePageable;
    const page = pageable.page;
    const totalPages = context.getState().transactionalEmailTypeSearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.transactionalEmailTypePageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.transactionalEmailApi
        .queryTypes(context.getState().transactionalEmailTypePageable, context.getState().typeName)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<TransactionalEmailTypeBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.transactionalEmailTypeSearchResult = {
                  items: [...draft.transactionalEmailTypeSearchResult.items, ...response.data.items],
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

  @Action(GetMultiTemplateTransactionalEmailType)
  getTransactionalEmailType(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId }: GetMultiTemplateTransactionalEmailType
  ) {
    return this.transactionalEmailApi.getType(typeId).pipe(
      tap((response: ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailType = response.data;
          })
        );
      })
    );
  }

  @Action(ResetMultiTemplateTransactionalEmailType)
  resetTransactionalEmailType(context: StateContext<MultiTemplateTransactionalEmailStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.transactionalEmailType = undefined;
        draft.activityName = undefined;
        draft.transactionalEmailActivitySearchResult = makeSearchResultPayload();
        draft.transactionalEmailActivityPageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.fileName = undefined;
        draft.fileFilters = {};
        draft.transactionalEmailFileSearchResult = makeSearchResultPayload();
        draft.transactionalEmailFilePageable = makePageable(10);
      })
    );
  }

  @Action(NewMultiTemplateTransactionalEmailType)
  newTransactionalEmailType(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { request }: NewMultiTemplateTransactionalEmailType
  ) {
    return this.transactionalEmailApi.newType(request).pipe(
      tap((response: ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailType = response.data;
          })
        );
      })
    );
  }

  @Action(DuplicateMultiTemplateTransactionalEmailType)
  duplicateTransactionalEmailType(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId }: DuplicateMultiTemplateTransactionalEmailType
  ) {
    return this.transactionalEmailApi.getType(typeId).pipe(
      switchMap((res) => {
        return this.transactionalEmailApi.duplicateType(res.data).pipe(
          tap((response: ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.transactionalEmailType = response.data;
              })
            );
          })
        );
      })
    );
  }

  @Action(UpdateMultiTemplateTransactionalEmailType)
  updateTransactionalEmailType(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, request }: UpdateMultiTemplateTransactionalEmailType
  ) {
    return this.transactionalEmailApi.updateType(typeId, request);
  }

  @Action(DeleteMultiTemplateTransactionalEmailType)
  deleteTransactionalEmailType(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId }: DeleteMultiTemplateTransactionalEmailType
  ) {
    return this.transactionalEmailApi
      .deleteType(typeId)
      .pipe(tap(() => context.dispatch(new QueryMultiTemplateTransactionalEmailTypes())));
  }

  @Action(QueryMultiTemplateTransactionalEmailActivities)
  queryTransactionalEmailActivities(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, pageable, name }: QueryMultiTemplateTransactionalEmailActivities
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.transactionalEmailActivityPageable = pageable ? pageable : draft.transactionalEmailTypePageable;
        draft.activityName = name !== undefined ? name : draft.activityName;
      })
    );

    return this.transactionalEmailApi
      .queryActivities(typeId, context.getState().transactionalEmailActivityPageable, context.getState().activityName)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<TransactionalEmailActivityBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.transactionalEmailActivitySearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(LoadMoreMultiTemplateTransactionalEmailActivities)
  loadMoreTransactionalEmailActivities(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId }: LoadMoreMultiTemplateTransactionalEmailActivities
  ) {
    const pageable = context.getState().transactionalEmailActivityPageable;
    const page = pageable.page;
    const totalPages = context.getState().transactionalEmailActivitySearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.transactionalEmailActivityPageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.transactionalEmailApi
        .queryActivities(typeId, context.getState().transactionalEmailActivityPageable)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<TransactionalEmailActivityBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.transactionalEmailActivitySearchResult = {
                  items: [...draft.transactionalEmailActivitySearchResult.items, ...response.data.items],
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

  @Action(GetMultiTemplateTransactionalEmailActivity)
  getTransactionalEmailActivity(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, activityId }: GetMultiTemplateTransactionalEmailActivity
  ) {
    return this.transactionalEmailApi.getActivity(typeId, activityId).pipe(
      tap((response: ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivity = response.data;
            draft.transactionalEmailActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ResetMultiTemplateTransactionalEmailActivity)
  resetTransactionalEmailActivity(context: StateContext<MultiTemplateTransactionalEmailStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.transactionalEmailType = undefined;
        draft.transactionalEmailActivity = undefined;
        draft.transactionalEmailActivityFiles = [];
      })
    );
  }

  @Action(NewMultiTemplateTransactionalEmailActivity)
  newTransactionalEmailActivity(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, request }: NewMultiTemplateTransactionalEmailActivity
  ) {
    return this.transactionalEmailApi.newActivity(typeId, request).pipe(
      tap((response: ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivity = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateMultiTemplateTransactionalEmailActivity)
  updateTransactionalEmailActivity(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, activityId, request }: UpdateMultiTemplateTransactionalEmailActivity
  ) {
    return this.transactionalEmailApi.updateActivity(typeId, activityId, request).pipe(
      tap((response: ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivity = response.data;
          })
        );
      })
    );
  }

  @Action(DeleteMultiTemplateTransactionalEmailActivity)
  deleteTransactionalEmailActivity(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, activityId }: DeleteMultiTemplateTransactionalEmailActivity
  ) {
    return this.transactionalEmailApi
      .deleteActivity(typeId, activityId)
      .pipe(tap(() => context.dispatch(new QueryMultiTemplateTransactionalEmailActivities(typeId))));
  }

  @Action(UploadMultiTemplateTransactionalEmailFile)
  uploadTransactionalEmailFile(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, activityId, fileFormData }: UploadMultiTemplateTransactionalEmailFile
  ) {
    return this.transactionalEmailApi.uploadFile(typeId, activityId, fileFormData).pipe(
      tap((response: ApiResponseModel<TransactionalEmailFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(DownloadMultiTemplateTransactionalEmailFile)
  downloadTransactionalEmailFile(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, activityId, fileId }: DownloadMultiTemplateTransactionalEmailFile
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.transactionalEmailApi.downloadFile(typeId, activityId, fileId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(DeleteMultiTemplateTransactionalEmailFile)
  deleteTransactionalEmailFile(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, activityId, fileId }: DeleteMultiTemplateTransactionalEmailFile
  ) {
    return this.transactionalEmailApi.deleteFile(typeId, activityId, fileId).pipe(
      tap((response: ApiResponseModel<TransactionalEmailFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(DeleteMultiTemplateTransactionalEmailFiles)
  deleteTransactionalEmailFiles(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, activityId }: DeleteMultiTemplateTransactionalEmailFiles
  ) {
    return this.transactionalEmailApi.deleteFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<TransactionalEmailFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(PurgeMultiTemplateTransactionalEmailActivity)
  purgeTransactionalEmailActivity(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, activityId }: PurgeMultiTemplateTransactionalEmailActivity
  ) {
    return this.transactionalEmailApi.purgeFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<MultiTemplateTransactionalEmailActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ExportMultiTemplateTransactionalEmailActivity)
  exportTransactionalEmailActivity(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, activityId }: ExportMultiTemplateTransactionalEmailActivity
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.transactionalEmailApi.exportFile(typeId, activityId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(QueryMultiTemplateTransactionalEmailTypeFiles)
  queryTransactionalEmailTypeFiles(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { pageable, name, fileFilters, typeId }: QueryMultiTemplateTransactionalEmailTypeFiles
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.transactionalEmailFilePageable = pageable ? pageable : draft.transactionalEmailFilePageable;
        draft.fileName = name !== undefined ? name : draft.fileName;
        draft.fileFilters = fileFilters ? fileFilters : draft.fileFilters;
      })
    );

    return this.transactionalEmailApi
      .queryFiles(
        context.getState().transactionalEmailFilePageable,
        typeId,
        context.getState().fileName,
        context.getState().fileFilters
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.transactionalEmailFileSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(ValidateMultiTemplateTransactionalEmailActivitySftp)
  validateDigitalFilingActivitySftp(
    context: StateContext<MultiTemplateTransactionalEmailStateModel>,
    { typeId, activityId, request }: ValidateMultiTemplateTransactionalEmailActivitySftp
  ) {
    return this.transactionalEmailApi.validateSftp(typeId, activityId, request).pipe(
      tap((response: ApiResponseModel<BaseActivitySftpValidationSummaryModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivityError = response.data;
          })
        );
      })
    );
  }
}
