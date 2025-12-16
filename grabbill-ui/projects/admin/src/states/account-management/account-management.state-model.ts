import {
  AccountBasicModel,
  AccountDetailsModel,
  DownloadFile,
  PageableModel,
  SearchResultPayloadModel,
  UserModel,
  WhatsappTemplateModel
} from '@grabbill/lib';

export interface AccountManagementStateModel {
  filters: { [index: string]: any };
  name?: string;
  startDate?: Date;
  endDate?: Date;

  accountPageable: PageableModel;
  accountSearchResult: SearchResultPayloadModel<AccountBasicModel>;
  account?: AccountDetailsModel;
  accountUsers: UserModel[];

  wabaTemplates: WhatsappTemplateModel[];

  file?: DownloadFile;
}
