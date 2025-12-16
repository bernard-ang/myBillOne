import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseModel, AuditLogModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { AuditLogApi } from '../api/audit-log.api';

@Injectable({
  providedIn: 'root',
})
export class AuditLogService implements AuditLogApi {
  readonly baseRoute = `audit-logs`;

  constructor(private http: ApiHttpService) {}

  getAudits(
    pageable: PageableModel,
    query?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AuditLogModel>>> {
    startDate?.setHours(0, 0, 0, 0);
    endDate?.setHours(23, 59, 59, 9999);

    return this.http.query<ApiResponseModel<SearchResultPayloadModel<AuditLogModel>>>(`${this.baseRoute}`, pageable, {
      query,
      startDate,
      endDate,
    });
  }
}
