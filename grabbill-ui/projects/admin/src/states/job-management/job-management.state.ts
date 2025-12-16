import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  JobBasicModel,
  JobModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { JobManagementStateModel } from './job-management.state-model';
import { GetJob, QueryJobs, ResetJob, ResetJobs, RetryJob } from './job-management.state-actions';
import { JobManagementApi } from '../../api/job-management.api';

@State<JobManagementStateModel>({
  name: 'job_management',
  defaults: {
    accountName: '',
    filters: {},
    jobPageable: makePageable(10),
    jobSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class JobManagementState {
  constructor(private jobManagementApi: JobManagementApi) {}

  @Selector()
  static jobSearchResult(state: JobManagementStateModel) {
    return state.jobSearchResult;
  }

  @Selector()
  static jobPageable(state: JobManagementStateModel) {
    return state.jobPageable;
  }

  @Selector()
  static filters(state: JobManagementStateModel) {
    return state.filters;
  }

  @Selector()
  static accountName(state: JobManagementStateModel) {
    return state.accountName;
  }

  @Selector()
  static startDate(state: JobManagementStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: JobManagementStateModel) {
    return state.endDate;
  }

  @Selector()
  static job(state: JobManagementStateModel) {
    return state.job;
  }

  @Action(ResetJobs)
  resetJobs(context: StateContext<JobManagementStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.jobSearchResult = makeSearchResultPayload();
        draft.jobPageable = makePageable(10);
        draft.startDate = undefined;
        draft.endDate = undefined;
        draft.accountName = '';
        draft.filters = {};
      })
    );
  }

  @Action(QueryJobs)
  queryJobs(
    context: StateContext<JobManagementStateModel>,
    { pageable, accountName, filters, startDate, endDate }: QueryJobs
  ) {
    context.setState(
      produce(context.getState(), (draft: JobManagementStateModel) => {
        draft.jobPageable = pageable ? pageable : draft.jobPageable;
        draft.accountName = accountName;
        draft.filters = filters;
        draft.startDate = startDate;
        draft.endDate = endDate;
      })
    );

    return this.jobManagementApi
      .getJobs(
        context.getState().jobPageable,
        context.getState().accountName,
        context.getState().filters,
        context.getState().startDate,
        context.getState().endDate
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<JobBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft: JobManagementStateModel) => {
              draft.jobSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(ResetJob)
  resetJob(context: StateContext<JobManagementStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.job = undefined;
      })
    );
  }

  @Action(GetJob)
  getAccount(context: StateContext<JobManagementStateModel>, { id }: GetJob) {
    return this.jobManagementApi.getJob(id).pipe(
      tap((response: ApiResponseModel<JobModel>) => {
        context.setState(
          produce(context.getState(), (draft: JobManagementStateModel) => {
            draft.job = response.data;
          })
        );
      })
    );
  }

  @Action(RetryJob)
  retryJob(context: StateContext<JobManagementStateModel>, { id }: RetryJob) {
    return this.jobManagementApi.retryJob(id).pipe(
      tap((response: ApiResponseModel<JobModel>) => {
        context.setState(
          produce(context.getState(), (draft: JobManagementStateModel) => {
            draft.job = response.data;
          })
        );
      })
    );
  }
}
