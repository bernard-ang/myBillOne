import { PageableModel } from '@grabbill/lib';

export const getPageableQueryString = (pageable: PageableModel): string => {
  let queryString = `size=${pageable.size}&page=${pageable.page - 1}`;
  if (pageable.sort) {
    queryString += `&sort=${pageable.sort},${pageable.direction}`;
  }
  return queryString;
};
