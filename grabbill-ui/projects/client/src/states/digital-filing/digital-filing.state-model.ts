import {
  BaseActivitySftpValidationSummaryModel,
  BaseFileModel,
  BaseIndexRowModel,
  DigitalFilingActivityBasicModel,
  DigitalFilingActivityModel,
  DigitalFilingTypeBasicModel,
  DigitalFilingTypeModel,
  DownloadFile,
  PageableModel,
  SearchResultPayloadModel
} from "@grabbill/lib";

export interface DigitalFilingStateModel {
  typeName?: string;
  digitalFilingType?: DigitalFilingTypeModel;
  digitalFilingTypePageable: PageableModel;
  digitalFilingTypeSearchResult: SearchResultPayloadModel<DigitalFilingTypeBasicModel>;

  activityName?: string;
  digitalFilingActivity?: DigitalFilingActivityModel;
  digitalFilingActivityPageable: PageableModel;
  digitalFilingActivitySearchResult: SearchResultPayloadModel<DigitalFilingActivityBasicModel>;
  digitalFilingActivityError?: BaseActivitySftpValidationSummaryModel;

  fileName?: string;
  fileFilters: { [index: string]: any };
  digitalFilingFilePageable: PageableModel;
  digitalFilingFileSearchResult: SearchResultPayloadModel<BaseIndexRowModel>;

  digitalFilingActivityFiles: BaseFileModel[];

  file?: DownloadFile;
}
