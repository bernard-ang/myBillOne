import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  AdminAuditLogModel,
  ApiResponseModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { AdminAuditStateModel } from './admin-audit-state.model';
import { AdminAuditLogApi } from '../../api/admin-audit-log.api';
import { QueryAudits, ResetAudits } from './admin-audit.state-actions';

@State<AdminAuditStateModel>({
  name: 'audit',
  defaults: {
    auditPageable: makePageable(10),
    auditSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class AdminAuditState {
  constructor(private auditApi: AdminAuditLogApi) {}

  @Selector()
  static auditSearchResult(state: AdminAuditStateModel) {
    return state.auditSearchResult;
  }

  @Selector()
  static auditPageable(state: AdminAuditStateModel) {
    return state.auditPageable;
  }

  @Selector()
  static query(state: AdminAuditStateModel) {
    return state.query;
  }

  @Selector()
  static startDate(state: AdminAuditStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: AdminAuditStateModel) {
    return state.endDate;
  }

  @Action(ResetAudits)
  resetUsers(context: StateContext<AdminAuditStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.auditSearchResult = makeSearchResultPayload();
        draft.auditPageable = makePageable(10);
        draft.query = undefined;
        draft.startDate = undefined;
        draft.endDate = undefined;
      })
    );
  }

  @Action(QueryAudits)
  queryUsers(context: StateContext<AdminAuditStateModel>, { pageable, query, startDate, endDate }: QueryAudits) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.auditPageable = pageable ? pageable : draft.auditPageable;
        draft.query = query !== undefined ? query : draft.query;
        draft.startDate = startDate;
        draft.endDate = endDate;
      })
    );

    return this.auditApi
      .getAudits(
        context.getState().auditPageable,
        context.getState().query,
        context.getState().startDate,
        context.getState().endDate
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<AdminAuditLogModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.auditSearchResult = response.data;
            })
          );
        })
      );
  }
}
