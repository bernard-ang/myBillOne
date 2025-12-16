import { Observable } from 'rxjs';
import { ApiResponseModel, AuditLogModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export abstract class AuditLogApi {
  abstract getAudits(
    pageable: PageableModel,
    query?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AuditLogModel>>>;
}
