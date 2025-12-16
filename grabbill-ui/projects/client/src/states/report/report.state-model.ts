import {
  BaseActivityBasicModel,
  BaseTypeBasicModel,
  BaseTypeModel,
  DownloadFile,
  SearchResultPayloadModel,
} from '@grabbill/lib';

export interface ReportStateModel {
  typeName?: string;
  typeSearchResult: SearchResultPayloadModel<BaseTypeBasicModel>;
  type?: BaseTypeModel;

  activityName?: string;
  activitySearchResult: SearchResultPayloadModel<BaseActivityBasicModel>;

  file?: DownloadFile;
}
