import produce from 'immer';
import { Injectable } from '@angular/core';
import { of, switchMap, tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  BaseIndexRowModel,
  EmailCampaignActivityBasicModel,
  EmailCampaignActivityModel,
  EmailCampaignFilesPayloadModel,
  EmailCampaignTypeBasicModel,
  EmailCampaignTypeModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import {
  DeleteEmailCampaignActivity,
  DeleteEmailCampaignFile,
  DeleteEmailCampaignFiles,
  DeleteEmailCampaignType,
  DownloadEmailCampaignFile,
  DuplicateEmailCampaignType,
  ExportEmailCampaignActivity,
  GetEmailCampaignActivity,
  GetEmailCampaignType,
  LoadMoreEmailCampaignActivities,
  LoadMoreEmailCampaignTypes,
  NewEmailCampaignActivity,
  NewEmailCampaignType,
  PurgeEmailCampaignActivity,
  QueryEmailCampaignActivities,
  QueryEmailCampaignTypeFiles,
  QueryEmailCampaignTypes,
  ResetEmailCampaignActivity,
  ResetEmailCampaignType,
  ResetEmailCampaignTypes,
  UpdateEmailCampaignActivity,
  UpdateEmailCampaignType,
  UploadEmailCampaignFile,
} from './email-campaign.state-actions';
import { EmailCampaignStateModel } from './email-campaign.state-model';
import { EmailCampaignApi } from '../../api/email-campaign.api';

@State<EmailCampaignStateModel>({
  name: 'email_campaign',
  defaults: {
    emailCampaignTypePageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    emailCampaignTypeSearchResult: makeSearchResultPayload(),
    emailCampaignActivityPageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    emailCampaignActivitySearchResult: makeSearchResultPayload(),
    emailCampaignActivityFiles: [],
    emailCampaignFilePageable: makePageable(10),
    emailCampaignFileSearchResult: makeSearchResultPayload(),
    fileFilters: {},
  },
})
@Injectable()
export class EmailCampaignState {
  constructor(private emailCampaignApi: EmailCampaignApi) {}

  @Selector()
  static emailCampaignTypeSearchResult(state: EmailCampaignStateModel) {
    return state.emailCampaignTypeSearchResult;
  }

  @Selector()
  static emailCampaignTypePageable(state: EmailCampaignStateModel) {
    return state.emailCampaignTypePageable;
  }

  @Selector()
  static emailCampaignType(state: EmailCampaignStateModel) {
    return state.emailCampaignType;
  }

  @Selector()
  static typeName(state: EmailCampaignStateModel) {
    return state.typeName;
  }

  @Selector()
  static activityName(state: EmailCampaignStateModel) {
    return state.activityName;
  }

  @Selector()
  static file(state: EmailCampaignStateModel) {
    return state.file;
  }

  @Selector()
  static emailCampaignActivityPageable(state: EmailCampaignStateModel) {
    return state.emailCampaignActivityPageable;
  }

  @Selector()
  static emailCampaignActivitySearchResult(state: EmailCampaignStateModel) {
    return state.emailCampaignActivitySearchResult;
  }

  @Selector()
  static emailCampaignActivity(state: EmailCampaignStateModel) {
    return state.emailCampaignActivity;
  }

  @Selector()
  static emailCampaignActivityFiles(state: EmailCampaignStateModel) {
    return state.emailCampaignActivityFiles;
  }

  @Selector()
  static fileName(state: EmailCampaignStateModel) {
    return state.fileName;
  }

  @Selector()
  static fileFilters(state: EmailCampaignStateModel) {
    return state.fileFilters;
  }

  @Selector()
  static emailCampaignFilePageable(state: EmailCampaignStateModel) {
    return state.emailCampaignFilePageable;
  }

  @Selector()
  static emailCampaignFileSearchResult(state: EmailCampaignStateModel) {
    return state.emailCampaignFileSearchResult;
  }

  @Action(ResetEmailCampaignTypes)
  resetEmailCampaignTypes(context: StateContext<EmailCampaignStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.typeName = undefined;
        draft.emailCampaignTypePageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.emailCampaignTypeSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QueryEmailCampaignTypes)
  queryEmailCampaignTypes(context: StateContext<EmailCampaignStateModel>, { pageable, name }: QueryEmailCampaignTypes) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.emailCampaignTypePageable = pageable ? pageable : draft.emailCampaignTypePageable;
        draft.typeName = name !== undefined ? name : draft.typeName;
      })
    );

    return this.emailCampaignApi
      .queryTypes(context.getState().emailCampaignTypePageable, context.getState().typeName)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<EmailCampaignTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.emailCampaignTypeSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(LoadMoreEmailCampaignTypes)
  loadMoreEmailCampaignTypes(context: StateContext<EmailCampaignStateModel>) {
    const pageable = context.getState().emailCampaignTypePageable;
    const page = pageable.page;
    const totalPages = context.getState().emailCampaignTypeSearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.emailCampaignTypePageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.emailCampaignApi
        .queryTypes(context.getState().emailCampaignTypePageable, context.getState().typeName)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<EmailCampaignTypeBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.emailCampaignTypeSearchResult = {
                  items: [...draft.emailCampaignTypeSearchResult.items, ...response.data.items],
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

  @Action(GetEmailCampaignType)
  getEmailCampaignType(context: StateContext<EmailCampaignStateModel>, { typeId }: GetEmailCampaignType) {
    return this.emailCampaignApi.getType(typeId).pipe(
      tap((response: ApiResponseModel<EmailCampaignTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.emailCampaignType = response.data;
          })
        );
      })
    );
  }

  @Action(ResetEmailCampaignType)
  resetEmailCampaignType(context: StateContext<EmailCampaignStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.emailCampaignType = undefined;
        draft.activityName = undefined;
        draft.emailCampaignActivitySearchResult = makeSearchResultPayload();
        draft.emailCampaignActivityPageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.fileName = undefined;
        draft.fileFilters = {};
        draft.emailCampaignFileSearchResult = makeSearchResultPayload();
        draft.emailCampaignFilePageable = makePageable(10);
      })
    );
  }

  @Action(NewEmailCampaignType)
  newEmailCampaignType(context: StateContext<EmailCampaignStateModel>, { request }: NewEmailCampaignType) {
    return this.emailCampaignApi.newType(request).pipe(
      tap((response: ApiResponseModel<EmailCampaignTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.emailCampaignType = response.data;
          })
        );
      })
    );
  }

  @Action(DuplicateEmailCampaignType)
  duplicateEmailCampaignType(context: StateContext<EmailCampaignStateModel>, { typeId }: DuplicateEmailCampaignType) {
    return this.emailCampaignApi.getType(typeId).pipe(
      switchMap((res) => {
        return this.emailCampaignApi.duplicateType(res.data).pipe(
          tap((response: ApiResponseModel<EmailCampaignTypeModel>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.emailCampaignType = response.data;
              })
            );
          })
        );
      })
    );
  }

  @Action(UpdateEmailCampaignType)
  updateEmailCampaignType(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, request }: UpdateEmailCampaignType
  ) {
    return this.emailCampaignApi.updateType(typeId, request);
  }

  @Action(DeleteEmailCampaignType)
  deleteEmailCampaignType(context: StateContext<EmailCampaignStateModel>, { typeId }: DeleteEmailCampaignType) {
    return this.emailCampaignApi.deleteType(typeId).pipe(tap(() => context.dispatch(new QueryEmailCampaignTypes())));
  }

  @Action(QueryEmailCampaignActivities)
  queryEmailCampaignActivities(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, pageable, name }: QueryEmailCampaignActivities
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.emailCampaignActivityPageable = pageable ? pageable : draft.emailCampaignActivityPageable;
        draft.activityName = name !== undefined ? name : draft.activityName;
      })
    );

    return this.emailCampaignApi
      .queryActivities(typeId, context.getState().emailCampaignActivityPageable, context.getState().activityName)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<EmailCampaignActivityBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.emailCampaignActivitySearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(LoadMoreEmailCampaignActivities)
  loadMoreEmailCampaignActivities(
    context: StateContext<EmailCampaignStateModel>,
    { typeId }: LoadMoreEmailCampaignActivities
  ) {
    const pageable = context.getState().emailCampaignActivityPageable;
    const page = pageable.page;
    const totalPages = context.getState().emailCampaignActivitySearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.emailCampaignActivityPageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.emailCampaignApi.queryActivities(typeId, context.getState().emailCampaignActivityPageable).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<EmailCampaignActivityBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.emailCampaignActivitySearchResult = {
                items: [...draft.emailCampaignActivitySearchResult.items, ...response.data.items],
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

  @Action(GetEmailCampaignActivity)
  getEmailCampaignActivity(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, activityId }: GetEmailCampaignActivity
  ) {
    return this.emailCampaignApi.getActivity(typeId, activityId).pipe(
      tap((response: ApiResponseModel<EmailCampaignActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.emailCampaignActivity = response.data;
            draft.emailCampaignActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ResetEmailCampaignActivity)
  resetEmailCampaignActivity(context: StateContext<EmailCampaignStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.emailCampaignType = undefined;
        draft.emailCampaignActivity = undefined;
        draft.emailCampaignActivityFiles = [];
      })
    );
  }

  @Action(NewEmailCampaignActivity)
  newEmailCampaignActivity(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, request }: NewEmailCampaignActivity
  ) {
    return this.emailCampaignApi.newActivity(typeId, request).pipe(
      tap((response: ApiResponseModel<EmailCampaignActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.emailCampaignActivity = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateEmailCampaignActivity)
  updateEmailCampaignActivity(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, activityId, request }: UpdateEmailCampaignActivity
  ) {
    return this.emailCampaignApi.updateActivity(typeId, activityId, request).pipe(
      tap((response: ApiResponseModel<EmailCampaignActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.emailCampaignActivity = response.data;
          })
        );
      })
    );
  }

  @Action(DeleteEmailCampaignActivity)
  deleteEmailCampaignActivity(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, activityId }: DeleteEmailCampaignActivity
  ) {
    return this.emailCampaignApi
      .deleteActivity(typeId, activityId)
      .pipe(tap(() => context.dispatch(new QueryEmailCampaignActivities(typeId))));
  }

  @Action(UploadEmailCampaignFile)
  uploadEmailCampaignFile(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, activityId, fileFormData }: UploadEmailCampaignFile
  ) {
    return this.emailCampaignApi.uploadFile(typeId, activityId, fileFormData).pipe(
      tap((response: ApiResponseModel<EmailCampaignFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.emailCampaignActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(DownloadEmailCampaignFile)
  downloadEmailCampaignFile(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, activityId, fileId }: DownloadEmailCampaignFile
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.emailCampaignApi.downloadFile(typeId, activityId, fileId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(DeleteEmailCampaignFile)
  deleteEmailCampaignFile(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, activityId, fileId }: DeleteEmailCampaignFile
  ) {
    return this.emailCampaignApi.deleteFile(typeId, activityId, fileId).pipe(
      tap((response: ApiResponseModel<EmailCampaignFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.emailCampaignActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(DeleteEmailCampaignFiles)
  deleteEmailCampaignFiles(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, activityId }: DeleteEmailCampaignFiles
  ) {
    return this.emailCampaignApi.deleteFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<EmailCampaignFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.emailCampaignActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(PurgeEmailCampaignActivity)
  purgeEmailCampaignActivity(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, activityId }: PurgeEmailCampaignActivity
  ) {
    return this.emailCampaignApi.purgeFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<EmailCampaignActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.emailCampaignActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ExportEmailCampaignActivity)
  exportEmailCampaignActivity(
    context: StateContext<EmailCampaignStateModel>,
    { typeId, activityId }: ExportEmailCampaignActivity
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.emailCampaignApi.exportFile(typeId, activityId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(QueryEmailCampaignTypeFiles)
  queryEmailCampaignTypeFiles(
    context: StateContext<EmailCampaignStateModel>,
    { pageable, name, fileFilters, typeId }: QueryEmailCampaignTypeFiles
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.emailCampaignFilePageable = pageable ? pageable : draft.emailCampaignFilePageable;
        draft.fileName = name !== undefined ? name : draft.fileName;
        draft.fileFilters = fileFilters ? fileFilters : draft.fileFilters;
      })
    );

    return this.emailCampaignApi
      .queryFiles(
        context.getState().emailCampaignFilePageable,
        typeId,
        context.getState().fileName,
        context.getState().fileFilters
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.emailCampaignFileSearchResult = response.data;
            })
          );
        })
      );
  }
}
