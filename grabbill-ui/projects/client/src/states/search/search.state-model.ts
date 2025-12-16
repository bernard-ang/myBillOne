import {
  BaseIndexRowModel,
  BaseTypeBasicModel,
  BaseTypeModel,
  DownloadFile,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';

export interface SearchStateModel {
  typeName?: string;
  type?: BaseTypeModel;
  typeSearchResult: SearchResultPayloadModel<BaseTypeBasicModel>;

  indexRowFilters: { [index: string]: any };
  indexRowPageable: PageableModel;
  indexRowSearchResult: SearchResultPayloadModel<BaseIndexRowModel>;

  file?: DownloadFile;
}
