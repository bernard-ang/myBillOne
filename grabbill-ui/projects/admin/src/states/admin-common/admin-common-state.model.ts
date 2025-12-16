import { Breadcrumb } from '@grabbill/lib';

export interface AdminCommonStateModel {
  message?: {
    messageType?: string;
    message?: string;
  };
  error?: string;
  sectionTitle: string;
  breadcrumbs: Breadcrumb[];
  pageLoading: boolean;
}
