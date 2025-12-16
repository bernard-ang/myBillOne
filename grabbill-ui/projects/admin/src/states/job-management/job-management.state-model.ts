import { JobBasicModel, JobModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface JobManagementStateModel {
  filters: { [index: string]: any };
  accountName: string;
  startDate?: Date;
  endDate?: Date;

  jobPageable: PageableModel;
  jobSearchResult: SearchResultPayloadModel<JobBasicModel>;
  job?: JobModel;
}
