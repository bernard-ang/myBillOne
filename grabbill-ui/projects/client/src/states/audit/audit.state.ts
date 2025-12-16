import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  AuditLogModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { AuditStateModel } from './audit.state-model';
import { AuditLogApi } from '../../api/audit-log.api';
import { QueryAudits, ResetAudits } from './audit.state-actions';

@State<AuditStateModel>({
  name: 'audit',
  defaults: {
    auditPageable: makePageable(10),
    auditSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class AuditState {
  constructor(private auditApi: AuditLogApi) {}

  @Selector()
  static auditSearchResult(state: AuditStateModel) {
    return state.auditSearchResult;
  }

  @Selector()
  static auditPageable(state: AuditStateModel) {
    return state.auditPageable;
  }

  @Selector()
  static query(state: AuditStateModel) {
    return state.query;
  }

  @Selector()
  static startDate(state: AuditStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: AuditStateModel) {
    return state.endDate;
  }

  @Action(ResetAudits)
  resetAudits(context: StateContext<AuditStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.auditPageable = makePageable(10);
        draft.auditSearchResult = makeSearchResultPayload();
        draft.query = undefined;
        draft.startDate = undefined;
        draft.endDate = undefined;
      })
    );
  }

  @Action(QueryAudits)
  queryAudits(context: StateContext<AuditStateModel>, { pageable, query, startDate, endDate }: QueryAudits) {
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
        tap((response: ApiResponseModel<SearchResultPayloadModel<AuditLogModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.auditSearchResult = response.data;
            })
          );
        })
      );
  }
}
