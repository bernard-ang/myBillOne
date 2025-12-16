import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  DigitalFilingActivityBasicModel,
  DigitalFilingTypeBasicModel,
  DomainType,
  EmailCampaignActivityBasicModel,
  EmailCampaignTypeBasicModel,
  makeSearchResultPayload,
  ProcessStatus,
  SearchResultPayloadModel,
  SmsTypeBasicModel,
  TransactionalEmailActivityBasicModel,
  TransactionalEmailTypeBasicModel,
  WhatsAppActivityBasicModel,
  WhatsAppTypeBasicModel,
} from '@grabbill/lib';
import { DigitalFilingApi } from '../../api/digital-filing.api';
import { ReportStateModel } from './report.state-model';
import { GenerateReport, QueryActivities, QueryTypes, ResetReport } from './report.state-actions';
import { TransactionalEmailApi } from '../../api/transactional-email.api';
import { EmailCampaignApi } from '../../api/email-campaign.api';
import { SmsApi } from '../../api/sms.api';
import { WhatsAppApi } from '../../api/whatsapp.api';
import { MultiTemplateTransactionalEmailApi } from "../../api/multi-template-transactional-email.api";
import { MultiTemplateWhatsappApi } from "../../api/multi-template-whatsapp.api";

@State<ReportStateModel>({
  name: 'report',
  defaults: {
    typeSearchResult: makeSearchResultPayload(),
    activitySearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class ReportState {
  constructor(
    private digitalFilingApi: DigitalFilingApi,
    private transactionalEmailApi: TransactionalEmailApi,
    private multiTemplateTransactionalEmailApi: MultiTemplateTransactionalEmailApi,
    private emailCampaignApi: EmailCampaignApi,
    private whatsAppApi: WhatsAppApi,
    private multiTemplateWhatsAppApi: MultiTemplateWhatsappApi,
    private smsApi: SmsApi
  ) {}

  @Selector()
  static typeSearchResult(state: ReportStateModel) {
    return state.typeSearchResult;
  }

  @Selector()
  static activitySearchResult(state: ReportStateModel) {
    return state.activitySearchResult;
  }

  @Selector()
  static type(state: ReportStateModel) {
    return state.type;
  }

  @Selector()
  static file(state: ReportStateModel) {
    return state.file;
  }

  @Action(ResetReport)
  resetSearch(context: StateContext<ReportStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.type = undefined;
        draft.typeName = undefined;
        draft.typeSearchResult = makeSearchResultPayload();

        draft.activityName = undefined;
        draft.activitySearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QueryActivities)
  queryActivities(
    context: StateContext<ReportStateModel>,
    { domain, pageable, typeId, activityName }: QueryActivities
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.activityName = activityName ? activityName : draft.activityName;
      })
    );

    if (domain === DomainType.DIGITAL_FILING) {
      return this.digitalFilingApi
        .queryActivities(typeId, pageable, context.getState().activityName, ProcessStatus.COMPLETED)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<DigitalFilingActivityBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.activitySearchResult = response.data;
              })
            );
          })
        );
    } else if (domain === DomainType.TRANSACTIONAL_EMAIL) {
      return this.transactionalEmailApi
        .queryActivities(typeId, pageable, context.getState().activityName, ProcessStatus.COMPLETED)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<TransactionalEmailActivityBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.activitySearchResult = response.data;
              })
            );
          })
        );
    } else if (domain === DomainType.MT_TRANSACTIONAL_EMAIL) {
      return this.multiTemplateTransactionalEmailApi
        .queryActivities(typeId, pageable, context.getState().activityName, ProcessStatus.COMPLETED)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<TransactionalEmailActivityBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.activitySearchResult = response.data;
              })
            );
          })
        );
    } else if (domain === DomainType.EMAIL_CAMPAIGN) {
      return this.emailCampaignApi
        .queryActivities(typeId, pageable, context.getState().activityName, ProcessStatus.COMPLETED)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<EmailCampaignActivityBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.activitySearchResult = response.data;
              })
            );
          })
        );
    } else if (domain === DomainType.WHATSAPP) {
      return this.whatsAppApi
        .queryActivities(typeId, pageable, context.getState().activityName, ProcessStatus.COMPLETED)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppActivityBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.activitySearchResult = response.data;
              })
            );
          })
        );
    } else if (domain === DomainType.MT_WHATSAPP) {
      return this.multiTemplateWhatsAppApi
        .queryActivities(typeId, pageable, context.getState().activityName, ProcessStatus.COMPLETED)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppActivityBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.activitySearchResult = response.data;
              })
            );
          })
        );
    } else {
      return this.smsApi
        .queryActivities(typeId, pageable, context.getState().activityName, ProcessStatus.COMPLETED)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<EmailCampaignActivityBasicModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.activitySearchResult = response.data;
              })
            );
          })
        );
    }
  }

  @Action(QueryTypes)
  queryTypes(context: StateContext<ReportStateModel>, { domain, pageable, typeName }: QueryTypes) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.typeName = typeName ? typeName : draft.typeName;
      })
    );

    if (domain === DomainType.DIGITAL_FILING) {
      return this.digitalFilingApi.queryTypes(pageable, context.getState().typeName).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<DigitalFilingTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.typeSearchResult = response.data;
            })
          );
        })
      );
    } else if (domain === DomainType.TRANSACTIONAL_EMAIL) {
      return this.transactionalEmailApi.queryTypes(pageable, context.getState().typeName).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<TransactionalEmailTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.typeSearchResult = response.data;
            })
          );
        })
      );
    } else if (domain === DomainType.MT_TRANSACTIONAL_EMAIL) {
      return this.multiTemplateTransactionalEmailApi.queryTypes(pageable, context.getState().typeName).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<TransactionalEmailTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.typeSearchResult = response.data;
            })
          );
        })
      );
    } else if (domain === DomainType.EMAIL_CAMPAIGN) {
      return this.emailCampaignApi.queryTypes(pageable, context.getState().typeName).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<EmailCampaignTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.typeSearchResult = response.data;
            })
          );
        })
      );
    } else if (domain === DomainType.WHATSAPP) {
      return this.whatsAppApi.queryTypes(pageable, context.getState().typeName).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.typeSearchResult = response.data;
            })
          );
        })
      );
    } else if (domain === DomainType.MT_WHATSAPP) {
      return this.multiTemplateWhatsAppApi.queryTypes(pageable, context.getState().typeName).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<WhatsAppTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.typeSearchResult = response.data;
            })
          );
        })
      );
    } else {
      return this.smsApi.queryTypes(pageable, context.getState().typeName).pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<SmsTypeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.typeSearchResult = response.data;
            })
          );
        })
      );
    }
  }

  @Action(GenerateReport)
  generateReport(
    context: StateContext<ReportStateModel>,
    { domain, typeId, activityIds, reportTypes, encrypts }: GenerateReport
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    if (domain === DomainType.DIGITAL_FILING) {
      return this.digitalFilingApi.downloadReport(typeId, activityIds, reportTypes).pipe(
        tap((file) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.file = file;
            })
          );
        })
      );
    } else if (domain === DomainType.TRANSACTIONAL_EMAIL) {
      return this.transactionalEmailApi
        .downloadReport(typeId, activityIds, reportTypes, encrypts.includes('ENCRYPT_ATTACHMENT_PASSWORD'))
        .pipe(
          tap((file) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.file = file;
              })
            );
          })
        );
    } else if (domain === DomainType.MT_TRANSACTIONAL_EMAIL) {
      return this.multiTemplateTransactionalEmailApi
        .downloadReport(typeId, activityIds, reportTypes, encrypts.includes('ENCRYPT_ATTACHMENT_PASSWORD'))
        .pipe(
          tap((file) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.file = file;
              })
            );
          })
        );
    } else if (domain === DomainType.EMAIL_CAMPAIGN) {
      return this.emailCampaignApi.downloadReport(typeId, activityIds, reportTypes).pipe(
        tap((file) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.file = file;
            })
          );
        })
      );
    } else if (domain === DomainType.WHATSAPP) {
      return this.whatsAppApi
        .downloadReport(typeId, activityIds, reportTypes, encrypts.includes('ENCRYPT_ATTACHMENT_PASSWORD'))
        .pipe(
          tap((file) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.file = file;
              })
            );
          })
        );
    } else if (domain === DomainType.MT_WHATSAPP) {
      return this.multiTemplateWhatsAppApi
        .downloadReport(typeId, activityIds, reportTypes, encrypts.includes('ENCRYPT_ATTACHMENT_PASSWORD'))
        .pipe(
          tap((file) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.file = file;
              })
            );
          })
        );
    } else {
      return this.smsApi.downloadReport(typeId, activityIds, reportTypes).pipe(
        tap((file) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.file = file;
            })
          );
        })
      );
    }
  }
}
