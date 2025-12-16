import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiResponseModel,
  PageableModel,
  SearchResultPayloadModel,
  StripeEventBasicModel,
  StripeEventModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { StripeEventManagementApi } from '../api/stripe-event-management.api';

@Injectable({
  providedIn: 'root',
})
export class StripeEventManagementService implements StripeEventManagementApi {
  readonly baseRoute = `mgmt/events`;

  constructor(private http: ApiHttpService) {}

  getEvent(id: number): Observable<ApiResponseModel<StripeEventModel>> {
    return this.http.get<ApiResponseModel<StripeEventModel>>(`${this.baseRoute}/${id}`);
  }

  getEvents(
    pageable: PageableModel,
    accountName?: string,
    type?: string,
    refId?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<StripeEventBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<StripeEventBasicModel>>>(
      `${this.baseRoute}`,
      pageable,
      {
        accountName,
        type,
        refId,
        startDate,
        endDate,
      }
    );
  }
}
