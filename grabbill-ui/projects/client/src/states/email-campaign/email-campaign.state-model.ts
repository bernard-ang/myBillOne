import {
  BaseFileModel,
  BaseIndexRowModel,
  DownloadFile,
  EmailCampaignActivityBasicModel,
  EmailCampaignActivityModel,
  EmailCampaignTypeBasicModel,
  EmailCampaignTypeModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';

export interface EmailCampaignStateModel {
  typeName?: string;
  emailCampaignType?: EmailCampaignTypeModel;
  emailCampaignTypePageable: PageableModel;
  emailCampaignTypeSearchResult: SearchResultPayloadModel<EmailCampaignTypeBasicModel>;

  emailCampaignActivity?: EmailCampaignActivityModel;
  emailCampaignActivityPageable: PageableModel;
  emailCampaignActivitySearchResult: SearchResultPayloadModel<EmailCampaignActivityBasicModel>;

  activityName?: string;

  fileName?: string;
  fileFilters: { [index: string]: any };
  emailCampaignFilePageable: PageableModel;
  emailCampaignFileSearchResult: SearchResultPayloadModel<BaseIndexRowModel>;

  emailCampaignActivityFiles: BaseFileModel[];

  file?: DownloadFile;
}
