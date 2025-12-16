import {
  BaseActivitySftpValidationSummaryModel,
  BaseFileModel,
  BaseIndexRowModel,
  DownloadFile,
  MultiTemplateTransactionalEmailActivityModel,
  MultiTemplateTransactionalEmailTypeModel,
  PageableModel,
  SearchResultPayloadModel,
  TransactionalEmailActivityBasicModel,
  TransactionalEmailTypeBasicModel
} from "@grabbill/lib";

export interface MultiTemplateTransactionalEmailStateModel {
  typeName?: string;

  transactionalEmailType?: MultiTemplateTransactionalEmailTypeModel;
  transactionalEmailTypePageable: PageableModel;
  transactionalEmailTypeSearchResult: SearchResultPayloadModel<TransactionalEmailTypeBasicModel>;

  transactionalEmailActivity?: MultiTemplateTransactionalEmailActivityModel;
  transactionalEmailActivityPageable: PageableModel;
  transactionalEmailActivitySearchResult: SearchResultPayloadModel<TransactionalEmailActivityBasicModel>;
  transactionalEmailActivityError?: BaseActivitySftpValidationSummaryModel;

  activityName?: string;

  fileName?: string;
  fileFilters: { [index: string]: any };
  transactionalEmailFilePageable: PageableModel;
  transactionalEmailFileSearchResult: SearchResultPayloadModel<BaseIndexRowModel>;

  transactionalEmailActivityFiles: BaseFileModel[];

  file?: DownloadFile;
}
