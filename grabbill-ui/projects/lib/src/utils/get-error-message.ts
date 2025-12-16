import { ApiErrorMessageModel } from '@grabbill/lib';

export const getErrorMessage = (error: unknown): string => {
  if ((error as any).errorDetails) {
    return getApiErrorMessage(error).errorDetails
  }
  return error as string;
};

export const getApiErrorMessage = (error: unknown): ApiErrorMessageModel => {
  return error as ApiErrorMessageModel;
};
