import {
  BaseFileModel,
  BaseIndexRowModel,
  DownloadFile,
  PageableModel,
  SearchResultPayloadModel,
  TransactionalEmailActivityBasicModel,
  TransactionalEmailActivityModel,
  TransactionalEmailTypeBasicModel,
  TransactionalEmailTypeModel,
} from '@grabbill/lib';

export interface TransactionalEmailStateModel {
  typeName?: string;
  transactionalEmailType?: TransactionalEmailTypeModel;
  transactionalEmailTypePageable: PageableModel;
  transactionalEmailTypeSearchResult: SearchResultPayloadModel<TransactionalEmailTypeBasicModel>;

  transactionalEmailActivity?: TransactionalEmailActivityModel;
  transactionalEmailActivityPageable: PageableModel;
  transactionalEmailActivitySearchResult: SearchResultPayloadModel<TransactionalEmailActivityBasicModel>;

  activityName?: string;

  fileName?: string;
  fileFilters: { [index: string]: any };
  transactionalEmailFilePageable: PageableModel;
  transactionalEmailFileSearchResult: SearchResultPayloadModel<BaseIndexRowModel>;

  transactionalEmailActivityFiles: BaseFileModel[];

  file?: DownloadFile;
}
