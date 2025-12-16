import { AuditLogModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface AuditStateModel {
  query?: string;
  startDate?: Date;
  endDate?: Date;
  auditPageable: PageableModel;
  auditSearchResult: SearchResultPayloadModel<AuditLogModel>;
}
