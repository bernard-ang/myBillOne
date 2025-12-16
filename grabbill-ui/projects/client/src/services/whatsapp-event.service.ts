import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { WhatsAppEventApi } from '../api/whatsapp-event.api';
import { WhatsappEventBasicModel } from 'projects/lib/src/models/data/whatsapp/whatsapp-event-basic.model';

@Injectable({
  providedIn: 'root',
})
export class WhatsappEventService implements WhatsAppEventApi {
  readonly baseRoute = `whatsapp/events`;

  constructor(private http: ApiHttpService) {}

  getEvents(
    pageable: PageableModel,
    messageType?: string,
    mobileNo?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<WhatsappEventBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<WhatsappEventBasicModel>>>(
      `${this.baseRoute}`,
      pageable,
      {
        messageType,
        mobileNo,
        startDate,
        endDate,
      }
    );
  }
}
