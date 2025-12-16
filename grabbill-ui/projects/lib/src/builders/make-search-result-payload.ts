import { SearchResultPayloadModel } from '../models';

export const makeSearchResultPayload = (): SearchResultPayloadModel<any> => ({
  items: [],
  totalItems: 0,
  totalPages: 0,
});
