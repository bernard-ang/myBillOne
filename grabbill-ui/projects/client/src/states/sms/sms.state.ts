import produce from 'immer';
import { Injectable } from '@angular/core';
import { of, switchMap, tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  SmsActivityBasicModel,
  SmsActivityModel,
  SmsTypeBasicModel,
  SmsTypeModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
  SmsActivityCreditUsagePayloadModel,
} from '@grabbill/lib';
import {
  CountSmsCreditUsage,
  DeleteSmsActivity,
  DeleteSmsType,
  DuplicateSmsType,
  GetSmsActivity,
  GetSmsType,
  LoadMoreSmsActivities,
  LoadMoreSmsTypes,
  NewSmsActivity,
  NewSmsType,
  QuerySmsActivities,
  QuerySmsTypes,
  ResetSmsActivity,
  ResetSmsType,
  ResetSmsTypes,
  UpdateSmsActivity,
  UpdateSmsType,
} from './sms.state-actions';
import { SmsStateModel } from './sms.state-model';
import { SmsApi } from '../../api/sms.api';

@State<SmsStateModel>({
  name: 'sms',
  defaults: {
    smsTypePageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    smsTypeSearchResult: makeSearchResultPayload(),
    smsActivityPageable: makePageable(50, 1, 'lastModifiedDate', 'DESC'),
    smsActivitySearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class SmsState {
  constructor(private smsApi: SmsApi) {}

  @Selector()
  static smsTypeSearchResult(state: SmsStateModel) {
    return state.smsTypeSearchResult;
  }

  @Selector()
  static smsTypePageable(state: SmsStateModel) {
    return state.smsTypePageable;
  }

  @Selector()
  static smsType(state: SmsStateModel) {
    return state.smsType;
  }

  @Selector()
  static typeName(state: SmsStateModel) {
    return state.typeName;
  }

  @Selector()
  static activityName(state: SmsStateModel) {
    return state.activityName;
  }

  @Selector()
  static smsActivityPageable(state: SmsStateModel) {
    return state.smsActivityPageable;
  }

  @Selector()
  static smsActivitySearchResult(state: SmsStateModel) {
    return state.smsActivitySearchResult;
  }

  @Selector()
  static smsActivity(state: SmsStateModel) {
    return state.smsActivity;
  }

  @Selector()
  static smsActivityCreditUsage(state: SmsStateModel) {
    return state.smsActivityCreditUsage;
  }

  @Action(ResetSmsTypes)
  resetSmsTypes(context: StateContext<SmsStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.typeName = undefined;
        draft.smsTypePageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
        draft.smsTypeSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QuerySmsTypes)
  querySmsTypes(context: StateContext<SmsStateModel>, { pageable, name }: QuerySmsTypes) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.smsTypePageable = pageable ? pageable : draft.smsTypePageable;
        draft.typeName = name !== undefined ? name : draft.typeName;
      })
    );

    return this.smsApi.queryTypes(context.getState().smsTypePageable, context.getState().typeName).pipe(
      tap((response: ApiResponseModel<SearchResultPayloadModel<SmsTypeBasicModel>>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.smsTypeSearchResult = response.data;
          })
        );
      })
    );
  }

  @Action(LoadMoreSmsTypes)
  loadMoreSmsTypes(context: StateContext<SmsStateModel>) {
    const pageable = context.getState().smsTypePageable;
    const page = pageable.page;
    const totalPages = context.getState().smsTypeSearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.smsTypePageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.smsApi.queryTypes(context.getState().smsTypePageable, context.getState().typeName).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<SmsTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.smsTypeSearchResult = {
                items: [...draft.smsTypeSearchResult.items, ...response.data.items],
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

  @Action(GetSmsType)
  getSmsType(context: StateContext<SmsStateModel>, { typeId }: GetSmsType) {
    return this.smsApi.getType(typeId).pipe(
      tap((response: ApiResponseModel<SmsTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.smsType = response.data;
          })
        );
      })
    );
  }

  @Action(ResetSmsType)
  resetSmsType(context: StateContext<SmsStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.smsType = undefined;
        draft.activityName = undefined;
        draft.smsActivitySearchResult = makeSearchResultPayload();
        draft.smsActivityPageable = makePageable(50, 1, 'lastModifiedDate', 'DESC');
      })
    );
  }

  @Action(NewSmsType)
  newSmsType(context: StateContext<SmsStateModel>, { request }: NewSmsType) {
    return this.smsApi.newType(request).pipe(
      tap((response: ApiResponseModel<SmsTypeModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.smsType = response.data;
          })
        );
      })
    );
  }

  @Action(DuplicateSmsType)
  duplicateSmsType(context: StateContext<SmsStateModel>, { typeId }: DuplicateSmsType) {
    return this.smsApi.getType(typeId).pipe(
      switchMap((res) => {
        return this.smsApi.duplicateType(res.data).pipe(
          tap((response: ApiResponseModel<SmsTypeModel>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.smsType = response.data;
              })
            );
          })
        );
      })
    );
  }

  @Action(UpdateSmsType)
  updateSmsType(context: StateContext<SmsStateModel>, { typeId, request }: UpdateSmsType) {
    return this.smsApi.updateType(typeId, request);
  }

  @Action(DeleteSmsType)
  deleteSmsType(context: StateContext<SmsStateModel>, { typeId }: DeleteSmsType) {
    return this.smsApi.deleteType(typeId).pipe(tap(() => context.dispatch(new QuerySmsTypes())));
  }

  @Action(QuerySmsActivities)
  querySmsActivities(context: StateContext<SmsStateModel>, { typeId, pageable, name }: QuerySmsActivities) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.smsActivityPageable = pageable ? pageable : draft.smsActivityPageable;
        draft.activityName = name !== undefined ? name : draft.activityName;
      })
    );

    return this.smsApi
      .queryActivities(typeId, context.getState().smsActivityPageable, context.getState().activityName)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<SmsActivityBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.smsActivitySearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(LoadMoreSmsActivities)
  loadMoreSmsActivities(context: StateContext<SmsStateModel>, { typeId }: LoadMoreSmsActivities) {
    const pageable = context.getState().smsActivityPageable;
    const page = pageable.page;
    const totalPages = context.getState().smsActivitySearchResult.totalPages;

    if (page < totalPages) {
      context.setState(
        produce(context.getState(), (draft) => {
          draft.smsActivityPageable = {
            ...pageable,
            page: pageable.page + 1,
          };
        })
      );

      return this.smsApi.queryActivities(typeId, context.getState().smsActivityPageable).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<SmsActivityBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.smsActivitySearchResult = {
                items: [...draft.smsActivitySearchResult.items, ...response.data.items],
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

  @Action(GetSmsActivity)
  getSmsActivity(context: StateContext<SmsStateModel>, { typeId, activityId }: GetSmsActivity) {
    return this.smsApi.getActivity(typeId, activityId).pipe(
      tap((response: ApiResponseModel<SmsActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.smsActivity = response.data;
          })
        );
      })
    );
  }

  @Action(ResetSmsActivity)
  resetSmsActivity(context: StateContext<SmsStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.smsType = undefined;
        draft.smsActivity = undefined;
        draft.smsActivityCreditUsage = undefined;
      })
    );
  }

  @Action(NewSmsActivity)
  newSmsActivity(context: StateContext<SmsStateModel>, { typeId, request }: NewSmsActivity) {
    return this.smsApi.newActivity(typeId, request).pipe(
      tap((response: ApiResponseModel<SmsActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.smsActivity = response.data;
          })
        );
      })
    );
  }

  @Action(UpdateSmsActivity)
  updateSmsActivity(context: StateContext<SmsStateModel>, { typeId, activityId, request }: UpdateSmsActivity) {
    return this.smsApi.updateActivity(typeId, activityId, request).pipe(
      tap((response: ApiResponseModel<SmsActivityModel>) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.smsActivity = response.data;
          })
        );
      })
    );
  }

  @Action(DeleteSmsActivity)
  deleteSmsActivity(context: StateContext<SmsStateModel>, { typeId, activityId }: DeleteSmsActivity) {
    return this.smsApi
      .deleteActivity(typeId, activityId)
      .pipe(tap(() => context.dispatch(new QuerySmsActivities(typeId))));
  }

  @Action(CountSmsCreditUsage)
  countSmsCreditUsage(
    context: StateContext<SmsStateModel>,
    { typeId, activityId, contactGroupId, indexRows, smsContent }: CountSmsCreditUsage
  ) {
    return this.smsApi
      .countActivityCreditUsage(typeId, activityId, {
        smsContent,
        contactGroupId: contactGroupId === 'all' ? undefined : Number(contactGroupId),
        indexRows,
      })
      .pipe(
        tap((response: ApiResponseModel<SmsActivityCreditUsagePayloadModel>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.smsActivityCreditUsage = response.data;
            })
          );
        })
      );
  }
}
