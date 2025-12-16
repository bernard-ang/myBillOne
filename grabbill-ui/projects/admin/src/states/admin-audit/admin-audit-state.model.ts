import { AdminAuditLogModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface AdminAuditStateModel {
  query?: string;
  startDate?: Date;
  endDate?: Date;
  auditPageable: PageableModel;
  auditSearchResult: SearchResultPayloadModel<AdminAuditLogModel>;
}
