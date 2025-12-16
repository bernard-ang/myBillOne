import { PageableModel, SearchResultPayloadModel, UserModel } from '@grabbill/lib';

export interface UserStateModel {
  name?: string;
  userPageable: PageableModel;
  userSearchResult: SearchResultPayloadModel<UserModel>;
}
