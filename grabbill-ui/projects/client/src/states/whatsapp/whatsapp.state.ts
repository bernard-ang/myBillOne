import { Injectable } from '@angular/core';
import produce from 'immer';
import { of, switchMap, tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiMessage,
  ApiResponseModel,
  BaseActivitySftpValidationSummaryModel,
  BaseIndexRowModel,
  makePageable,
  makeSearchResultPayload,
  MultiTemplateWhatsappActivityModel,
  MultiTemplateWhatsappTypeModel,
  SearchResultPayloadModel,
  WhatsAppActivityBasicModel,
  WhatsAppActivityModel,
  WhatsappEventBasicModel,
  WhatsAppFilesPayloadModel,
  WhatsAppTemplatesPayloadModel,
  WhatsAppTypeBasicModel,
  WhatsAppTypeModel
} from "@grabbill/lib";
import { WhatsAppStateModel } from './whatsapp-state.model';
import { WhatsAppApi } from '../../api/whatsapp.api';
import {
  CreateWhatsAppTemplate,
  DeleteMultiTemplateWhatsAppActivity,
  DeleteMultiTemplateWhatsAppFile,
  DeleteMultiTemplateWhatsAppFiles,
  DeleteMultiTemplateWhatsAppType,
  DeleteWhatsAppActivity,
  DeleteWhatsAppFile,
  DeleteWhatsAppFiles,
  DeleteWhatsAppTemplate,
  DeleteWhatsAppType,
  DownloadMultiTemplateWhatsAppFile,
  DownloadWhatsAppFile,
  DuplicateMultiTemplateWhatsAppType,
  DuplicateWhatsAppType,
  ExportMultiTemplateWhatsAppActivity,
  ExportWhatsAppActivity,
  GetMultiTemplateWhatsAppActivity,
  GetMultiTemplateWhatsAppType,
  GetWhatsAppActivity,
  GetWhatsAppTemplates,
  GetWhatsAppType,
  LoadMoreMultiTemplateWhatsAppActivities,
  LoadMoreMultiTemplateWhatsAppTypes,
  LoadMoreWhatsAppActivities,
  LoadMoreWhatsAppTypes,
  NewMultiTemplateWhatsAppActivity,
  NewMultiTemplateWhatsAppType,
  NewWhatsAppActivity,
  NewWhatsAppType,
  PurgeMultiTemplateWhatsAppActivity,
  PurgeWhatsAppActivity,
  QueryMultiTemplateWhatsAppActivities,
  QueryMultiTemplateWhatsAppTypeFiles,
  QueryMultiTemplateWhatsAppTypes,
  QueryWhatsAppActivities,
  QueryWhatsAppEvents,
  QueryWhatsAppTypeFiles,
  QueryWhatsAppTypes,
  ResetMultiTemplateWhatsAppActivity,
  ResetMultiTemplateWhatsAppType,
  ResetMultiTemplateWhatsAppTypes,
  ResetWhatsAppActivity,
  ResetWhatsAppEvents,
  ResetWhatsAppType,
  ResetWhatsAppTypes,
  UpdateAutoReplyMessage,
  UpdateMultiTemplateWhatsAppActivity,
  UpdateMultiTemplateWhatsAppType,
  UpdateWhatsAppActivity,
  UpdateWhatsAppType,
  UploadMultiTemplateWhatsAppFile,
  UploadWhatsAppFile, ValidateMultiTemplateWhatsAppActivitySftp
} from "./whatsapp.state-actions";
import { Me } from '../auth/auth.state-actions';
import { WhatsAppEventApi } from '../../api/whatsapp-event.api';
import { MultiTemplateWhatsappApi } from '../../api/multi-template-whatsapp.api';

@State<WhatsAppStateModel>({
  name: 'whatsapp_template',
  defaults: {
    eventPageable: makePageable(10),
    eventSearchResult: makeSearchResultPayload(),

    templates: [],

    whatsAppTypePageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    whatsAppTypeSearchResult: makeSearchResultPayload(),
    whatsAppActivityPageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    whatsAppActivitySearchResult: makeSearchResultPayload(),

    multiTemplateWhatsAppTypePageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    multiTemplateWhatsAppTypeSearchResult: makeSearchResultPayload(),
    multiTemplateWhatsAppActivityPageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    multiTemplateWhatsAppActivitySearchResult: makeSearchResultPayload(),

    whatsAppActivityFiles: [],
    whatsAppFilePageable: makePageable(10),
    whatsAppFileSearchResult: makeSearchResultPayload(),

    fileFilters: {},
  },
})
@Injectable()
export class WhatsAppState {
  constructor(
    private whatsAppApi: WhatsAppApi,
    private multiTemplateWhatsappApi: MultiTemplateWhatsappApi,
    private whatsAppEventApi: WhatsAppEventApi
  ) {}

  @Selector()
  static templates(state: WhatsAppStateModel) {
    return state.templates;
  }

  @Selector()
  static startDate(state: WhatsAppStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: WhatsAppStateModel) {
    return state.endDate;
  }

  @Selector()
  static eventPageable(state: WhatsAppStateModel) {
    return state.eventPageable;
  }

  @Selector()
  static mobileNo(state: WhatsAppStateModel) {
    return state.mobileNo;
  }

  @Selector()
  static eventSearchResult(state: WhatsAppStateModel) {
    return state.eventSearchResult;
  }

  @Selector()
  static whatsAppTypeSearchResult(state: WhatsAppStateModel) {
    return state.whatsAppTypeSearchResult;
  }

  @Selector()
  static whatsAppTypePageable(state: WhatsAppStateModel) {
    return state.whatsAppTypePageable;
  }

  @Selector()
  static whatsAppType(state: WhatsAppStateModel) {
    return state.whatsAppType;
  }

  @Selector()
  static multiTemplateWhatsAppTypeSearchResult(state: WhatsAppStateModel) {
    return state.multiTemplateWhatsAppTypeSearchResult;
  }

  @Selector()
  static multiTemplateWhatsAppTypePageable(state: WhatsAppStateModel) {
    return state.multiTemplateWhatsAppTypePageable;
  }

  @Selector()
  static multiTemplateWhatsAppType(state: WhatsAppStateModel) {
    return state.multiTemplateWhatsAppType;
  }

  @Selector()
  static typeName(state: WhatsAppStateModel) {
    return state.typeName;
  }

  @Selector()
  static activityName(state: WhatsAppStateModel) {
    return state.activityName;
  }

  @Selector()
  static file(state: WhatsAppStateModel) {
    return state.file;
  }

  @Selector()
  static whatsAppActivityPageable(state: WhatsAppStateModel) {
    return state.whatsAppActivityPageable;
  }

  @Selector()
  static whatsAppActivitySearchResult(state: WhatsAppStateModel) {
    return state.whatsAppActivitySearchResult;
  }

  @Selector()
  static whatsAppActivity(state: WhatsAppStateModel) {
    return state.whatsAppActivity;
  }

  @Selector()
  static multiTemplateWhatsAppActivityPageable(state: WhatsAppStateModel) {
    return state.multiTemplateWhatsAppActivityPageable;
  }

  @Selector()
  static multiTemplateWhatsAppActivitySearchResult(state: WhatsAppStateModel) {
    return state.multiTemplateWhatsAppActivitySearchResult;
  }

  @Selector()
  static multiTemplateWhatsAppActivity(state: WhatsAppStateModel) {
    return state.multiTemplateWhatsAppActivity;
  }

  @Selector()
  static whatsAppActivityFiles(state: WhatsAppStateModel) {
    return state.whatsAppActivityFiles;
  }

  @Selector()
  static fileName(state: WhatsAppStateModel) {
    return state.fileName;
  }

  @Selector()
  static fileFilters(state: WhatsAppStateModel) {
    return state.fileFilters;
  }

  @Selector()
  static whatsAppFilePageable(state: WhatsAppStateModel) {
    return state.whatsAppFilePageable;
  }

  @Selector()
  static whatsAppFileSearchResult(state: WhatsAppStateModel) {
    return state.whatsAppFileSearchResult;
  }

  @Selector()
  static multiTemplateWhatsAppActivityError(state: WhatsAppStateModel) {
    return state.multiTemplateWhatsAppActivityError;
  }

  @Action(GetWhatsAppTemplates)
  getTemplates(context: StateContext<WhatsAppStateModel>) {
    return this.whatsAppApi.getTemplates().pipe(
      tap((response: ApiResponseModel<WhatsAppTemplatesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.templates = response.data.templates;
          })
        );
      })
    );
  }

  @Action(CreateWhatsAppTemplate)
  uploadImage(context: StateContext<WhatsAppStateModel>, { formData }: CreateWhatsAppTemplate) {
    return this.whatsAppApi.createTemplate(formData).pipe(
      tap(() => {
        context.dispatch(new GetWhatsAppTemplates());
      })
    );
  }

  @Action(DeleteWhatsAppTemplate)
  deleteTemplate(context: StateContext<WhatsAppStateModel>, { id }: DeleteWhatsAppTemplate) {
    return this.whatsAppApi.deleteTemplate(id).pipe(
      tap((response: ApiResponseModel<WhatsAppTemplatesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.templates = response.data.templates;
          })
        );
      })
    );
  }

  @Action(UpdateAutoReplyMessage)
  updateAutoReplyMessage(context: StateContext<WhatsAppStateModel>, { message }: UpdateAutoReplyMessage) {
    return this.whatsAppApi.updateAutoReplyMessage(message).pipe(
      tap((response: ApiResponseModel<ApiMessage>) => {
        context.dispatch(new Me());
      })
    );
  }

  @Action(ResetWhatsAppEvents)
  resetWhatsAppEvents(context: StateContext<WhatsAppStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.eventSearchResult = makeSearchResultPayload();
        draft.eventPageable = makePageable(10);
        draft.mobileNo = undefined;
        draft.startDate = undefined;
        draft.endDate = undefined;
      })
    );
  }

  @Action(QueryWhatsAppEvents)
  queryWhatsAppEvents(
    context: StateContext<WhatsAppStateModel>,
    { pageable, mobileNo, startDate, endDate }: QueryWhatsAppEvents
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.eventPageable = pageable ? pageable : draft.eventPageable;
        draft.mobileNo = mobileNo !== undefined ? mobileNo : draft.mobileNo;
        draft.startDate = startDate;
        draft.endDate = endDate;
      })
    );

    return this.whatsAppEventApi
      .getEvents(
        context.getState().eventPageable,
        undefined,
        context.getState().mobileNo,
        context.getState().startDate,
        context.getState().endDate
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsappEventBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.eventSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(ResetWhatsAppTypes)
  resetWhatsAppTypes(context: StateContext<WhatsAppStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.typeName = undefined;
        draft.whatsAppTypePageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.whatsAppTypeSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QueryWhatsAppTypes)
  queryWhatsAppTypes(context: StateContext<WhatsAppStateModel>, { pageable, name }: QueryWhatsAppTypes) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.whatsAppTypePageable = pageable ? pageable : draft.whatsAppTypePageable;
        draft.typeName = name !== undefined ? name : draft.typeName;
      })
    );

    return this.whatsAppApi.queryTypes(context.getState().whatsAppTypePageable, context.getState().typeName).pipe(
      tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppTypeBasicModel>>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppTypeSearchResult = response.data;
          })
        );
      })
    );
  }

  @Action(LoadMoreWhatsAppTypes)
  loadMoreWhatsAppTypes(context: StateContext<WhatsAppStateModel>) {
    const pageable = context.getState().whatsAppTypePageable;
    const page = pageable.page;
    const totalPages = context.getState().whatsAppTypeSearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.whatsAppTypePageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.whatsAppApi.queryTypes(context.getState().whatsAppTypePageable, context.getState().typeName).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.whatsAppTypeSearchResult = {
                items: [...draft.whatsAppTypeSearchResult.items, ...response.data.items],
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

  @Action(GetWhatsAppType)
  getWhatsAppType(context: StateContext<WhatsAppStateModel>, { typeId }: GetWhatsAppType) {
    return this.whatsAppApi.getType(typeId).pipe(
      tap((response: ApiResponseModel<WhatsAppTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppType = response.data;
          })
        );
      })
    );
  }

  @Action(ResetWhatsAppType)
  resetWhatsAppType(context: StateContext<WhatsAppStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.whatsAppType = undefined;
        draft.activityName = undefined;
        draft.whatsAppActivitySearchResult = makeSearchResultPayload();
        draft.whatsAppActivityPageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.fileName = undefined;
        draft.fileFilters = {};
        draft.whatsAppFileSearchResult = makeSearchResultPayload();
        draft.whatsAppFilePageable = makePageable(10);
      })
    );
  }

  @Action(NewWhatsAppType)
  newWhatsAppType(context: StateContext<WhatsAppStateModel>, { request }: NewWhatsAppType) {
    return this.whatsAppApi.newType(request).pipe(
      tap((response: ApiResponseModel<WhatsAppTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppType = response.data;
          })
        );
      })
    );
  }

  @Action(DuplicateWhatsAppType)
  duplicateWhatsAppType(context: StateContext<WhatsAppStateModel>, { typeId }: DuplicateWhatsAppType) {
    return this.whatsAppApi.getType(typeId).pipe(
      switchMap((res) => {
        return this.whatsAppApi.duplicateType(res.data).pipe(
          tap((response: ApiResponseModel<WhatsAppTypeModel>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.whatsAppType = response.data;
              })
            );
          })
        );
      })
    );
  }

  @Action(UpdateWhatsAppType)
  updateWhatsAppType(context: StateContext<WhatsAppStateModel>, { typeId, request }: UpdateWhatsAppType) {
    return this.whatsAppApi.updateType(typeId, request);
  }

  @Action(DeleteWhatsAppType)
  deleteWhatsAppType(context: StateContext<WhatsAppStateModel>, { typeId }: DeleteWhatsAppType) {
    return this.whatsAppApi.deleteType(typeId).pipe(tap(() => context.dispatch(new QueryWhatsAppTypes())));
  }

  @Action(QueryWhatsAppActivities)
  queryWhatsAppActivities(
    context: StateContext<WhatsAppStateModel>,
    { typeId, pageable, name }: QueryWhatsAppActivities
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.whatsAppActivityPageable = pageable ? pageable : draft.whatsAppTypePageable;
        draft.activityName = name !== undefined ? name : draft.activityName;
      })
    );

    return this.whatsAppApi
      .queryActivities(typeId, context.getState().whatsAppActivityPageable, context.getState().activityName)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppActivityBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.whatsAppActivitySearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(LoadMoreWhatsAppActivities)
  loadMoreWhatsAppActivities(context: StateContext<WhatsAppStateModel>, { typeId }: LoadMoreWhatsAppActivities) {
    const pageable = context.getState().whatsAppActivityPageable;
    const page = pageable.page;
    const totalPages = context.getState().whatsAppActivitySearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.whatsAppActivityPageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.whatsAppApi.queryActivities(typeId, context.getState().whatsAppActivityPageable).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppActivityBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.whatsAppActivitySearchResult = {
                items: [...draft.whatsAppActivitySearchResult.items, ...response.data.items],
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

  @Action(GetWhatsAppActivity)
  getWhatsAppActivity(context: StateContext<WhatsAppStateModel>, { typeId, activityId }: GetWhatsAppActivity) {
    return this.whatsAppApi.getActivity(typeId, activityId).pipe(
      tap((response: ApiResponseModel<WhatsAppActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivity = response.data;
            draft.whatsAppActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ResetWhatsAppActivity)
  resetWhatsAppActivity(context: StateContext<WhatsAppStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.whatsAppType = undefined;
        draft.whatsAppActivity = undefined;
        draft.whatsAppActivityFiles = [];
      })
    );
  }

  @Action(NewWhatsAppActivity)
  newWhatsAppActivity(context: StateContext<WhatsAppStateModel>, { typeId, request }: NewWhatsAppActivity) {
    return this.whatsAppApi.newActivity(typeId, request).pipe(
      tap((response: ApiResponseModel<WhatsAppActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivity = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateWhatsAppActivity)
  updateWhatsAppActivity(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId, request }: UpdateWhatsAppActivity
  ) {
    return this.whatsAppApi.updateActivity(typeId, activityId, request).pipe(
      tap((response: ApiResponseModel<WhatsAppActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivity = response.data;
          })
        );
      })
    );
  }

  @Action(DeleteWhatsAppActivity)
  deleteWhatsAppActivity(context: StateContext<WhatsAppStateModel>, { typeId, activityId }: DeleteWhatsAppActivity) {
    return this.whatsAppApi
      .deleteActivity(typeId, activityId)
      .pipe(tap(() => context.dispatch(new QueryWhatsAppActivities(typeId))));
  }

  @Action(UploadWhatsAppFile)
  uploadWhatsAppFile(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId, fileFormData }: UploadWhatsAppFile
  ) {
    return this.whatsAppApi.uploadFile(typeId, activityId, fileFormData).pipe(
      tap((response: ApiResponseModel<WhatsAppFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(DownloadWhatsAppFile)
  downloadWhatsAppFile(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId, fileId }: DownloadWhatsAppFile
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.whatsAppApi.downloadFile(typeId, activityId, fileId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(DeleteWhatsAppFile)
  deleteWhatsAppFile(context: StateContext<WhatsAppStateModel>, { typeId, activityId, fileId }: DeleteWhatsAppFile) {
    return this.whatsAppApi.deleteFile(typeId, activityId, fileId).pipe(
      tap((response: ApiResponseModel<WhatsAppFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(DeleteWhatsAppFiles)
  deleteWhatsAppFiles(context: StateContext<WhatsAppStateModel>, { typeId, activityId }: DeleteWhatsAppFiles) {
    return this.whatsAppApi.deleteFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<WhatsAppFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(PurgeWhatsAppActivity)
  purgeWhatsAppActivity(context: StateContext<WhatsAppStateModel>, { typeId, activityId }: PurgeWhatsAppActivity) {
    return this.whatsAppApi.purgeFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<WhatsAppActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ExportWhatsAppActivity)
  exportWhatsAppActivity(context: StateContext<WhatsAppStateModel>, { typeId, activityId }: ExportWhatsAppActivity) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.whatsAppApi.exportFile(typeId, activityId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(QueryWhatsAppTypeFiles)
  queryWhatsAppTypeFiles(
    context: StateContext<WhatsAppStateModel>,
    { pageable, name, fileFilters, typeId }: QueryWhatsAppTypeFiles
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.whatsAppFilePageable = pageable ? pageable : draft.whatsAppFilePageable;
        draft.fileName = name !== undefined ? name : draft.fileName;
        draft.fileFilters = fileFilters ? fileFilters : draft.fileFilters;
      })
    );

    return this.whatsAppApi
      .queryFiles(
        context.getState().whatsAppFilePageable,
        typeId,
        context.getState().fileName,
        context.getState().fileFilters
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.whatsAppFileSearchResult = response.data;
            })
          );
        })
      );
  }

  // --- multi-template ---

  @Action(ResetMultiTemplateWhatsAppTypes)
  resetMultiTemplateWhatsAppTypes(context: StateContext<WhatsAppStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.typeName = undefined;
        draft.multiTemplateWhatsAppTypePageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.multiTemplateWhatsAppTypeSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QueryMultiTemplateWhatsAppTypes)
  queryMultiTemplateWhatsAppTypes(context: StateContext<WhatsAppStateModel>, { pageable, name }: QueryWhatsAppTypes) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.multiTemplateWhatsAppTypePageable = pageable ? pageable : draft.multiTemplateWhatsAppTypePageable;
        draft.typeName = name !== undefined ? name : draft.typeName;
      })
    );

    return this.multiTemplateWhatsappApi
      .queryTypes(context.getState().whatsAppTypePageable, context.getState().typeName)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.multiTemplateWhatsAppTypeSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(LoadMoreMultiTemplateWhatsAppTypes)
  loadMoreMultiTemplateWhatsAppTypes(context: StateContext<WhatsAppStateModel>) {
    const pageable = context.getState().multiTemplateWhatsAppTypePageable;
    const page = pageable.page;
    const totalPages = context.getState().multiTemplateWhatsAppTypeSearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.multiTemplateWhatsAppTypePageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.multiTemplateWhatsappApi
        .queryTypes(context.getState().multiTemplateWhatsAppTypePageable, context.getState().typeName)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppTypeBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.multiTemplateWhatsAppTypeSearchResult = {
                  items: [...draft.multiTemplateWhatsAppTypeSearchResult.items, ...response.data.items],
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

  @Action(GetMultiTemplateWhatsAppType)
  getMultiTemplateWhatsAppType(context: StateContext<WhatsAppStateModel>, { typeId }: GetMultiTemplateWhatsAppType) {
    return this.multiTemplateWhatsappApi.getType(typeId).pipe(
      tap((response: ApiResponseModel<MultiTemplateWhatsappTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.multiTemplateWhatsAppType = response.data;
          })
        );
      })
    );
  }

  @Action(ResetMultiTemplateWhatsAppType)
  resetMultiTemplateWhatsAppType(context: StateContext<WhatsAppStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.multiTemplateWhatsAppType = undefined;
        draft.activityName = undefined;
        draft.multiTemplateWhatsAppActivitySearchResult = makeSearchResultPayload();
        draft.multiTemplateWhatsAppActivityPageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.fileName = undefined;
        draft.fileFilters = {};
        draft.whatsAppFileSearchResult = makeSearchResultPayload();
        draft.whatsAppFilePageable = makePageable(10);
      })
    );
  }

  @Action(NewMultiTemplateWhatsAppType)
  newMultiTemplateWhatsAppType(context: StateContext<WhatsAppStateModel>, { request }: NewMultiTemplateWhatsAppType) {
    return this.multiTemplateWhatsappApi.newType(request).pipe(
      tap((response: ApiResponseModel<MultiTemplateWhatsappTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.multiTemplateWhatsAppType = response.data;
          })
        );
      })
    );
  }

  @Action(DuplicateMultiTemplateWhatsAppType)
  duplicateMultiTemplateWhatsAppType(
    context: StateContext<WhatsAppStateModel>,
    { typeId }: DuplicateMultiTemplateWhatsAppType
  ) {
    return this.multiTemplateWhatsappApi.getType(typeId).pipe(
      switchMap((res) => {
        return this.multiTemplateWhatsappApi.duplicateType(res.data).pipe(
          tap((response: ApiResponseModel<MultiTemplateWhatsappTypeModel>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.multiTemplateWhatsAppType = response.data;
              })
            );
          })
        );
      })
    );
  }

  @Action(UpdateMultiTemplateWhatsAppType)
  updateMultiTemplateWhatsAppType(
    context: StateContext<WhatsAppStateModel>,
    { typeId, request }: UpdateMultiTemplateWhatsAppType
  ) {
    return this.multiTemplateWhatsappApi.updateType(typeId, request);
  }

  @Action(DeleteMultiTemplateWhatsAppType)
  deleteMultiTemplateWhatsAppType(
    context: StateContext<WhatsAppStateModel>,
    { typeId }: DeleteMultiTemplateWhatsAppType
  ) {
    return this.multiTemplateWhatsappApi
      .deleteType(typeId)
      .pipe(tap(() => context.dispatch(new QueryMultiTemplateWhatsAppTypes())));
  }

  @Action(QueryMultiTemplateWhatsAppActivities)
  queryMultiTemplateWhatsAppActivities(
    context: StateContext<WhatsAppStateModel>,
    { typeId, pageable, name }: QueryMultiTemplateWhatsAppActivities
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.multiTemplateWhatsAppActivityPageable = pageable ? pageable : draft.multiTemplateWhatsAppTypePageable;
        draft.activityName = name !== undefined ? name : draft.activityName;
      })
    );

    return this.multiTemplateWhatsappApi
      .queryActivities(
        typeId,
        context.getState().multiTemplateWhatsAppActivityPageable,
        context.getState().activityName
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppActivityBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.multiTemplateWhatsAppActivitySearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(LoadMoreMultiTemplateWhatsAppActivities)
  loadMoreMultiTemplateWhatsAppActivities(
    context: StateContext<WhatsAppStateModel>,
    { typeId }: LoadMoreMultiTemplateWhatsAppActivities
  ) {
    const pageable = context.getState().multiTemplateWhatsAppActivityPageable;
    const page = pageable.page;
    const totalPages = context.getState().multiTemplateWhatsAppActivitySearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.multiTemplateWhatsAppActivityPageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.multiTemplateWhatsappApi
        .queryActivities(typeId, context.getState().multiTemplateWhatsAppActivityPageable)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppActivityBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.multiTemplateWhatsAppActivitySearchResult = {
                  items: [...draft.multiTemplateWhatsAppActivitySearchResult.items, ...response.data.items],
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

  @Action(GetMultiTemplateWhatsAppActivity)
  getMultiTemplateWhatsAppActivity(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId }: GetMultiTemplateWhatsAppActivity
  ) {
    return this.multiTemplateWhatsappApi.getActivity(typeId, activityId).pipe(
      tap((response: ApiResponseModel<MultiTemplateWhatsappActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.multiTemplateWhatsAppActivity = response.data;
            draft.whatsAppActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ResetMultiTemplateWhatsAppActivity)
  resetMultiTemplateWhatsAppActivity(context: StateContext<WhatsAppStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.multiTemplateWhatsAppType = undefined;
        draft.multiTemplateWhatsAppActivity = undefined;
        draft.whatsAppActivityFiles = [];
      })
    );
  }

  @Action(NewMultiTemplateWhatsAppActivity)
  newMultiTemplateWhatsAppActivity(
    context: StateContext<WhatsAppStateModel>,
    { typeId, request }: NewMultiTemplateWhatsAppActivity
  ) {
    return this.multiTemplateWhatsappApi.newActivity(typeId, request).pipe(
      tap((response: ApiResponseModel<MultiTemplateWhatsappActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.multiTemplateWhatsAppActivity = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateMultiTemplateWhatsAppActivity)
  updateMultiTemplateWhatsAppActivity(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId, request }: UpdateMultiTemplateWhatsAppActivity
  ) {
    return this.multiTemplateWhatsappApi.updateActivity(typeId, activityId, request).pipe(
      tap((response: ApiResponseModel<MultiTemplateWhatsappActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.multiTemplateWhatsAppActivity = response.data;
          })
        );
      })
    );
  }

  @Action(ValidateMultiTemplateWhatsAppActivitySftp)
  validateMultiTemplateWhatsAppActivitySftp(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId, request }: ValidateMultiTemplateWhatsAppActivitySftp
  ) {
    return this.multiTemplateWhatsappApi.validateSftp(typeId, activityId, request).pipe(
      tap((response: ApiResponseModel<BaseActivitySftpValidationSummaryModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.multiTemplateWhatsAppActivityError = response.data;
          })
        );
      })
    );
  }

  @Action(DeleteMultiTemplateWhatsAppActivity)
  deleteMultiTemplateWhatsAppActivity(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId }: DeleteMultiTemplateWhatsAppActivity
  ) {
    return this.multiTemplateWhatsappApi
      .deleteActivity(typeId, activityId)
      .pipe(tap(() => context.dispatch(new QueryMultiTemplateWhatsAppActivities(typeId))));
  }

  @Action(UploadMultiTemplateWhatsAppFile)
  uploadMultiTemplateWhatsAppFile(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId, fileFormData }: UploadMultiTemplateWhatsAppFile
  ) {
    return this.multiTemplateWhatsappApi.uploadFile(typeId, activityId, fileFormData).pipe(
      tap((response: ApiResponseModel<WhatsAppFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(DownloadMultiTemplateWhatsAppFile)
  downloadMultiTemplateWhatsAppFile(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId, fileId }: DownloadMultiTemplateWhatsAppFile
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.multiTemplateWhatsappApi.downloadFile(typeId, activityId, fileId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(DeleteMultiTemplateWhatsAppFile)
  deleteMultiTemplateWhatsAppFile(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId, fileId }: DeleteMultiTemplateWhatsAppFile
  ) {
    return this.multiTemplateWhatsappApi.deleteFile(typeId, activityId, fileId).pipe(
      tap((response: ApiResponseModel<WhatsAppFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(DeleteMultiTemplateWhatsAppFiles)
  deleteMultiTemplateWhatsAppFiles(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId }: DeleteMultiTemplateWhatsAppFiles
  ) {
    return this.multiTemplateWhatsappApi.deleteFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<WhatsAppFilesPayloadModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(PurgeMultiTemplateWhatsAppActivity)
  purgeMultiTemplateWhatsAppActivity(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId }: PurgeMultiTemplateWhatsAppActivity
  ) {
    return this.multiTemplateWhatsappApi.purgeFiles(typeId, activityId).pipe(
      tap((response: ApiResponseModel<MultiTemplateWhatsappActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.whatsAppActivityFiles = response.data.files;
          })
        );
      })
    );
  }

  @Action(ExportMultiTemplateWhatsAppActivity)
  exportMultiTemplateWhatsAppActivity(
    context: StateContext<WhatsAppStateModel>,
    { typeId, activityId }: ExportMultiTemplateWhatsAppActivity
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    return this.multiTemplateWhatsappApi.exportFile(typeId, activityId).pipe(
      tap((file) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.file = file;
          })
        );
      })
    );
  }

  @Action(QueryMultiTemplateWhatsAppTypeFiles)
  queryMultiTemplateWhatsAppTypeFiles(
    context: StateContext<WhatsAppStateModel>,
    { pageable, name, fileFilters, typeId }: QueryMultiTemplateWhatsAppTypeFiles
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.whatsAppFilePageable = pageable ? pageable : draft.whatsAppFilePageable;
        draft.fileName = name !== undefined ? name : draft.fileName;
        draft.fileFilters = fileFilters ? fileFilters : draft.fileFilters;
      })
    );

    return this.multiTemplateWhatsappApi
      .queryFiles(
        context.getState().whatsAppFilePageable,
        typeId,
        context.getState().fileName,
        context.getState().fileFilters
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.whatsAppFileSearchResult = response.data;
            })
          );
        })
      );
  }
}
