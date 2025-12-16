import {
  SmsActivityBasicModel,
  SmsActivityModel,
  SmsTypeBasicModel,
  SmsTypeModel,
  PageableModel,
  SearchResultPayloadModel, SmsActivityCreditUsagePayloadModel
} from "@grabbill/lib";

export interface SmsStateModel {
  typeName?: string;
  smsType?: SmsTypeModel;
  smsTypePageable: PageableModel;
  smsTypeSearchResult: SearchResultPayloadModel<SmsTypeBasicModel>;

  smsActivity?: SmsActivityModel;
  smsActivityPageable: PageableModel;
  smsActivitySearchResult: SearchResultPayloadModel<SmsActivityBasicModel>;

  smsActivityCreditUsage?: SmsActivityCreditUsagePayloadModel;

  activityName?: string;
}
