import { Observable } from 'rxjs';
import { AdminAuditLogModel, ApiResponseModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export abstract class AdminAuditLogApi {
  abstract getAudits(
    pageable: PageableModel,
    query?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AdminAuditLogModel>>>;
}
