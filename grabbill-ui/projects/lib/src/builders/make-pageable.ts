import { PageableModel } from '../models';

export const makePageable = (size = 5, page = 1, sort?: string, direction?: string): PageableModel => ({
  size,
  page,
  sort,
  direction,
});
