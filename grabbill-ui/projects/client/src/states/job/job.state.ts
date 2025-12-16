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
import { JobStateModel } from './job-state.model';
import { JobApi } from "../../api/job.api";
import { GetJob, QueryJobs, ResetJob, ResetJobs, RetryJob } from "./job.state-actions";

@State<JobStateModel>({
  name: 'job',
  defaults: {
    filters: {},
    jobPageable: makePageable(10),
    jobSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class JobState {
  constructor(private jobApi: JobApi) {}

  @Selector()
  static jobSearchResult(state: JobStateModel) {
    return state.jobSearchResult;
  }

  @Selector()
  static jobPageable(state: JobStateModel) {
    return state.jobPageable;
  }

  @Selector()
  static filters(state: JobStateModel) {
    return state.filters;
  }

  @Selector()
  static startDate(state: JobStateModel) {
    return state.startDate;
  }

  @Selector()
  static endDate(state: JobStateModel) {
    return state.endDate;
  }

  @Selector()
  static job(state: JobStateModel) {
    return state.job;
  }

  @Action(ResetJobs)
  resetJobs(context: StateContext<JobStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.jobSearchResult = makeSearchResultPayload();
        draft.jobPageable = makePageable(10);
        draft.startDate = undefined;
        draft.endDate = undefined;
        draft.filters = {};
      })
    );
  }

  @Action(QueryJobs)
  queryJobs(
    context: StateContext<JobStateModel>,
    { pageable, filters, startDate, endDate }: QueryJobs
  ) {
    context.setState(
      produce(context.getState(), (draft: JobStateModel) => {
        draft.jobPageable = pageable ? pageable : draft.jobPageable;
        draft.filters = filters;
        draft.startDate = startDate;
        draft.endDate = endDate;
      })
    );

    return this.jobApi
      .getJobs(
        context.getState().jobPageable,
        '',
        context.getState().filters,
        context.getState().startDate,
        context.getState().endDate
      )
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<JobBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft: JobStateModel) => {
              draft.jobSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(ResetJob)
  resetJob(context: StateContext<JobStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.job = undefined;
      })
    );
  }

  @Action(GetJob)
  getAccount(context: StateContext<JobStateModel>, { id }: GetJob) {
    return this.jobApi.getJob(id).pipe(
      tap((response: ApiResponseModel<JobModel>) => {
        context.setState(
          produce(context.getState(), (draft: JobStateModel) => {
            draft.job = response.data;
          })
        );
      })
    );
  }

  @Action(RetryJob)
  retryJob(context: StateContext<JobStateModel>, { id }: RetryJob) {
    return this.jobApi.retryJob(id).pipe(
      tap((response: ApiResponseModel<JobModel>) => {
        context.setState(
          produce(context.getState(), (draft: JobStateModel) => {
            draft.job = response.data;
          })
        );
      })
    );
  }
}
