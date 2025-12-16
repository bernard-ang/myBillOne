import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminAuditLogModel, ApiResponseModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { AdminAuditLogApi } from '../api/admin-audit-log.api';

@Injectable({
  providedIn: 'root',
})
export class AdminAuditLogService implements AdminAuditLogApi {
  readonly baseRoute = `mgmt/admin-audit-logs`;

  constructor(private http: ApiHttpService) {}

  getAudits(
    pageable: PageableModel,
    query?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AdminAuditLogModel>>> {
    startDate?.setHours(0, 0, 0, 0);
    endDate?.setHours(23, 59, 59, 9999);

    return this.http.query<ApiResponseModel<SearchResultPayloadModel<AdminAuditLogModel>>>(
      `${this.baseRoute}`,
      pageable,
      {
        query,
        startDate,
        endDate,
      }
    );
  }
}
