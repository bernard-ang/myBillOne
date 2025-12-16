import { JobBasicModel, JobModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface JobStateModel {
  filters: { [index: string]: any };
  startDate?: Date;
  endDate?: Date;

  jobPageable: PageableModel;
  jobSearchResult: SearchResultPayloadModel<JobBasicModel>;
  job?: JobModel;
}
