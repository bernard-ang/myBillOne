import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseModel, JobBasicModel, JobModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { JobManagementApi } from '../api/job-management.api';

@Injectable({
  providedIn: 'root',
})
export class JobManagementService implements JobManagementApi {
  readonly baseRoute = `mgmt/jobs`;

  constructor(private http: ApiHttpService) {}

  getJobs(
    pageable: PageableModel,
    accountName?: string,
    filters?: { [index: string]: any },
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<JobBasicModel>>> {
    startDate?.setHours(0, 0, 0, 0);
    endDate?.setHours(23, 59, 59, 9999);

    return this.http.query<ApiResponseModel<SearchResultPayloadModel<JobBasicModel>>>(this.baseRoute, pageable, {
      ...filters,
      accountName,
      startDate,
      endDate,
    });
  }

  getJob(id: number): Observable<ApiResponseModel<JobModel>> {
    return this.http.get<ApiResponseModel<JobModel>>(`${this.baseRoute}/${id}`);
  }

  retryJob(id: number): Observable<ApiResponseModel<JobModel>> {
    return this.http.post<ApiResponseModel<JobModel>>(`${this.baseRoute}/${id}/retry`, {});
  }
}
