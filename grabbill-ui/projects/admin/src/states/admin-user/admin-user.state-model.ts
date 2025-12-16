import { AdminUserModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface AdminUserStateModel {
  name?: string;
  userPageable: PageableModel;
  userSearchResult: SearchResultPayloadModel<AdminUserModel>;
}
