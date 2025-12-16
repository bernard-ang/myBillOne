import {
  AccountStatementModel,
  PageableModel,
  SearchResultPayloadModel,
  SmsUsageSummaryPayloadModel
} from "@grabbill/lib";

export interface UsageStateModel {
  startDate?: Date;
  endDate?: Date;
  accountStatementPageable: PageableModel;
  accountStatementSearchResult: SearchResultPayloadModel<AccountStatementModel>;

  smsUsageSummary?: SmsUsageSummaryPayloadModel;
}
