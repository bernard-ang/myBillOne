import { Observable } from 'rxjs';
import { ApiResponseModel, JobBasicModel, JobModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export abstract class JobApi {
  abstract getJobs(
    pageable: PageableModel,
    accountName?: string,
    filters?: { [index: string]: any },
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<JobBasicModel>>>;

  abstract getJob(id: number): Observable<ApiResponseModel<JobModel>>;

  abstract retryJob(id: number): Observable<ApiResponseModel<JobModel>>;
}
