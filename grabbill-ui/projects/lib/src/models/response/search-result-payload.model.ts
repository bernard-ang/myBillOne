export interface SearchResultPayloadModel<T> {
  items: T[];
  totalItems: number;
  totalPages: number;
}
