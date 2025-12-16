import { ApiErrorMessageModel } from './api.error.model';

export interface ApiResponseModel<T> {
  apiVersion: string;
  data: T;
  error?: ApiErrorMessageModel;
}
