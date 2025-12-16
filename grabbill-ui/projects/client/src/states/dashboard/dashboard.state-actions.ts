import { PageableModel } from "@grabbill/lib";

export class GetCurrentStatistics {
  static readonly type = '[Dashboard] Get Current Statistics';
}

export class ResetRecentActivities {
  static readonly type = '[Dashboard] ResetRecentActivities';
}

export class QueryRecentActivities {
  static readonly type = '[Dashboard] QueryRecentActivities';

  constructor(public pageable?: PageableModel) {}
}

export class LoadMoreRecentActivities {
  static readonly type = '[Dashboard] LoadMoreRecentActivities';
}
