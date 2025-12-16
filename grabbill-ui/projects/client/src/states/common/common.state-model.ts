import { Breadcrumb } from '@grabbill/lib';

export interface CommonStateModel {
  message?: {
    messageType?: string;
    message?: string;
  };
  error?: string;
  sectionTitle: string;
  breadcrumbs: Breadcrumb[];
  pageLoading: boolean;
}
