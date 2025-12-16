import { JobStatus } from '@grabbill/lib';

export const getJobStatusTag = (status: JobStatus): string => {
  switch (status) {
    case JobStatus.SUCCESS:
      return 'success';
    case JobStatus.NEW:
    case JobStatus.SUBMITTED:
      return 'default';
    case JobStatus.FAILED:
      return 'error';
    case JobStatus.PROCESSING:
      return 'processing';
    case JobStatus.QUEUED:
      return 'warning';
  }
};
