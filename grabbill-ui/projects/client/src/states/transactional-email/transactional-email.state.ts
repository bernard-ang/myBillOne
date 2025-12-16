import produce from 'immer';
import { Injectable } from '@angular/core';
import { of, switchMap, tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  BaseIndexRowModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
  TransactionalEmailActivityBasicModel,
  TransactionalEmailActivityModel,
  TransactionalEmailFilesPayloadModel,
  TransactionalEmailTypeBasicModel,
  TransactionalEmailTypeModel,
} from '@grabbill/lib';
import {
  DeleteTransactionalEmailActivity,
  DeleteTransactionalEmailFile,
  DeleteTransactionalEmailFiles,
  DeleteTransactionalEmailType,
  DownloadTransactionalEmailFile,
  DuplicateTransactionalEmailType,
  ExportTransactionalEmailActivity,
  GetTransactionalEmailActivity,
  GetTransactionalEmailType,
  LoadMoreTransactionalEmailActivities,
  LoadMoreTransactionalEmailTypes,
  NewTransactionalEmailActivity,
  NewTransactionalEmailType,
  PurgeTransactionalEmailActivity,
  QueryTransactionalEmailActivities,
  QueryTransactionalEmailTypeFiles,
  QueryTransactionalEmailTypes,
  ResetTransactionalEmailActivity,
  ResetTransactionalEmailType,
  ResetTransactionalEmailTypes,
  UpdateTransactionalEmailActivity,
  UpdateTransactionalEmailType,
  UploadTransactionalEmailFile,
} from './transactional-email.state-actions';
import { TransactionalEmailStateModel } from './transactional-email.state-model';
import { TransactionalEmailApi } from '../../api/transactional-email.api';

@State<TransactionalEmailStateModel>({
  name: 'transactional_email',
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
export class TransactionalEmailState {
  constructor(private transactionalEmailApi: TransactionalEmailApi) {}

  @Selector()
  static transactionalEmailTypeSearchResult(state: TransactionalEmailStateModel) {
    return state.transactionalEmailTypeSearchResult;
  }

  @Selector()
  static transactionalEmailTypePageable(state: TransactionalEmailStateModel) {
    return state.transactionalEmailTypePageable;
  }

  @Selector()
  static transactionalEmailType(state: TransactionalEmailStateModel) {
    return state.transactionalEmailType;
  }

  @Selector()
  static typeName(state: TransactionalEmailStateModel) {
    return state.typeName;
  }

  @Selector()
  static activityName(state: TransactionalEmailStateModel) {
    return state.activityName;
  }

  @Selector()
  static file(state: TransactionalEmailStateModel) {
    return state.file;
  }

  @Selector()
  static transactionalEmailActivityPageable(state: TransactionalEmailStateModel) {
    return state.transactionalEmailActivityPageable;
  }

  @Selector()
  static transactionalEmailActivitySearchResult(state: TransactionalEmailStateModel) {
    return state.transactionalEmailActivitySearchResult;
  }

  @Selector()
  static transactionalEmailActivity(state: TransactionalEmailStateModel) {
    return state.transactionalEmailActivity;
  }

  @Selector()
  static transactionalEmailActivityFiles(state: TransactionalEmailStateModel) {
    return state.transactionalEmailActivityFiles;
  }

  @Selector()
  static fileName(state: TransactionalEmailStateModel) {
    return state.fileName;
  }

  @Selector()
  static fileFilters(state: TransactionalEmailStateModel) {
    return state.fileFilters;
  }

  @Selector()
  static transactionalEmailFilePageable(state: TransactionalEmailStateModel) {
    return state.transactionalEmailFilePageable;
  }

  @Selector()
  static transactionalEmailFileSearchResult(state: TransactionalEmailStateModel) {
    return state.transactionalEmailFileSearchResult;
  }

  @Action(ResetTransactionalEmailTypes)
  resetTransactionalEmailTypes(context: StateContext<TransactionalEmailStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.typeName = undefined;
        draft.transactionalEmailTypePageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.transactionalEmailTypeSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QueryTransactionalEmailTypes)
  queryTransactionalEmailTypes(
    context: StateContext<TransactionalEmailStateModel>,
    { pageable, name }: QueryTransactionalEmailTypes
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

  @Action(LoadMoreTransactionalEmailTypes)
  loadMoreTransactionalEmailTypes(context: StateContext<TransactionalEmailStateModel>) {
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

  @Action(GetTransactionalEmailType)
  getTransactionalEmailType(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId }: GetTransactionalEmailType
  ) {
    return this.transactionalEmailApi.getType(typeId).pipe(
      tap((response: ApiResponseModel<TransactionalEmailTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailType = response.data;
          })
        );
      })
    );
  }

  @Action(ResetTransactionalEmailType)
  resetTransactionalEmailType(context: StateContext<TransactionalEmailStateModel>) {
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

  @Action(NewTransactionalEmailType)
  newTransactionalEmailType(
    context: StateContext<TransactionalEmailStateModel>,
    { request }: NewTransactionalEmailType
  ) {
    return this.transactionalEmailApi.newType(request).pipe(
      tap((response: ApiResponseModel<TransactionalEmailTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailType = response.data;
          })
        );
      })
    );
  }

  @Action(DuplicateTransactionalEmailType)
  duplicateTransactionalEmailType(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId }: DuplicateTransactionalEmailType
  ) {
    return this.transactionalEmailApi.getType(typeId).pipe(
      switchMap((res) => {
        return this.transactionalEmailApi.duplicateType(res.data).pipe(
          tap((response: ApiResponseModel<TransactionalEmailTypeModel>) => {
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

  @Action(UpdateTransactionalEmailType)
  updateTransactionalEmailType(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, request }: UpdateTransactionalEmailType
  ) {
    return this.transactionalEmailApi.updateType(typeId, request);
  }

  @Action(DeleteTransactionalEmailType)
  deleteTransactionalEmailType(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId }: DeleteTransactionalEmailType
  ) {
    return this.transactionalEmailApi
      .deleteType(typeId)
      .pipe(tap(() => context.dispatch(new QueryTransactionalEmailTypes())));
  }

  @Action(QueryTransactionalEmailActivities)
  queryTransactionalEmailActivities(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, pageable, name }: QueryTransactionalEmailActivities
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

  @Action(LoadMoreTransactionalEmailActivities)
  loadMoreTransactionalEmailActivities(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId }: LoadMoreTransactionalEmailActivities
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

  @Action(GetTransactionalEmailActivity)
  getTransactionalEmailActivity(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, activityId }: GetTransactionalEmailActivity
  ) {
    return this.transactionalEmailApi.getActivity(typeId, activityId).pipe(
      tap((response: ApiResponseModel<TransactionalEmailActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivity = response.data;
            draft.transactionalEmailActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ResetTransactionalEmailActivity)
  resetTransactionalEmailActivity(context: StateContext<TransactionalEmailStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.transactionalEmailType = undefined;
        draft.transactionalEmailActivity = undefined;
        draft.transactionalEmailActivityFiles = [];
      })
    );
  }

  @Action(NewTransactionalEmailActivity)
  newTransactionalEmailActivity(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, request }: NewTransactionalEmailActivity
  ) {
    return this.transactionalEmailApi.newActivity(typeId, request).pipe(
      tap((response: ApiResponseModel<TransactionalEmailActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivity = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateTransactionalEmailActivity)
  updateTransactionalEmailActivity(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, activityId, request }: UpdateTransactionalEmailActivity
  ) {
    return this.transactionalEmailApi.updateActivity(typeId, activityId, request).pipe(
      tap((response: ApiResponseModel<TransactionalEmailActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivity = response.data;
          })
        );
      })
    );
  }

  @Action(DeleteTransactionalEmailActivity)
  deleteTransactionalEmailActivity(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, activityId }: DeleteTransactionalEmailActivity
  ) {
    return this.transactionalEmailApi
      .deleteActivity(typeId, activityId)
      .pipe(tap(() => context.dispatch(new QueryTransactionalEmailActivities(typeId))));
  }

  @Action(UploadTransactionalEmailFile)
  uploadTransactionalEmailFile(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, activityId, fileFormData }: UploadTransactionalEmailFile
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

  @Action(DownloadTransactionalEmailFile)
  downloadTransactionalEmailFile(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, activityId, fileId }: DownloadTransactionalEmailFile
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

  @Action(DeleteTransactionalEmailFile)
  deleteTransactionalEmailFile(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, activityId, fileId }: DeleteTransactionalEmailFile
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

  @Action(DeleteTransactionalEmailFiles)
  deleteTransactionalEmailFiles(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, activityId }: DeleteTransactionalEmailFiles
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

  @Action(PurgeTransactionalEmailActivity)
  purgeTransactionalEmailActivity(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, activityId }: PurgeTransactionalEmailActivity
  ) {
    return this.transactionalEmailApi.purgeFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<TransactionalEmailActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.transactionalEmailActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ExportTransactionalEmailActivity)
  exportTransactionalEmailActivity(
    context: StateContext<TransactionalEmailStateModel>,
    { typeId, activityId }: ExportTransactionalEmailActivity
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

  @Action(QueryTransactionalEmailTypeFiles)
  queryTransactionalEmailTypeFiles(
    context: StateContext<TransactionalEmailStateModel>,
    { pageable, name, fileFilters, typeId }: QueryTransactionalEmailTypeFiles
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
}
