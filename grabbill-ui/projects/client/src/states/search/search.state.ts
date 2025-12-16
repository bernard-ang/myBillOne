import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  BaseIndexRowModel,
  DigitalFilingTypeBasicModel,
  DigitalFilingTypeModel,
  DomainType,
  EmailCampaignTypeBasicModel,
  EmailCampaignTypeModel,
  makePageable,
  makeSearchResultPayload,
  MultiTemplateTransactionalEmailTypeModel,
  MultiTemplateWhatsappTypeModel,
  SearchResultPayloadModel,
  SmsTypeBasicModel,
  SmsTypeModel,
  TransactionalEmailTypeBasicModel,
  TransactionalEmailTypeModel,
  WhatsAppTypeBasicModel,
  WhatsAppTypeModel,
} from '@grabbill/lib';
import { DigitalFilingApi } from '../../api/digital-filing.api';
import { SearchStateModel } from './search.state-model';
import {
  DeleteFile,
  DownloadFile,
  GetType,
  QueryTypeIndexRecords,
  QueryTypes,
  ResetIndexRecords,
  ResetSearch,
} from './search.state-actions';
import { TransactionalEmailApi } from '../../api/transactional-email.api';
import { EmailCampaignApi } from '../../api/email-campaign.api';
import { SmsApi } from '../../api/sms.api';
import { WhatsAppApi } from '../../api/whatsapp.api';
import { MultiTemplateTransactionalEmailApi } from '../../api/multi-template-transactional-email.api';
import { MultiTemplateWhatsappApi } from '../../api/multi-template-whatsapp.api';

@State<SearchStateModel>({
  name: 'search',
  defaults: {
    typeSearchResult: makeSearchResultPayload(),
    indexRowPageable: makePageable(10),
    indexRowSearchResult: makeSearchResultPayload(),
    indexRowFilters: {},
  },
})
@Injectable()
export class SearchState {
  constructor(
    private digitalFilingApi: DigitalFilingApi,
    private transactionalEmailApi: TransactionalEmailApi,
    private multiTemplateTransactionalEmailApi: MultiTemplateTransactionalEmailApi,
    private emailCampaignApi: EmailCampaignApi,
    private whatsAppApi: WhatsAppApi,
    private multiTemplateWhatsappApi: MultiTemplateWhatsappApi,
    private smsApi: SmsApi
  ) {}

  @Selector()
  static typeSearchResult(state: SearchStateModel) {
    return state.typeSearchResult;
  }

  @Selector()
  static type(state: SearchStateModel) {
    return state.type;
  }

  @Selector()
  static indexRowPageable(state: SearchStateModel) {
    return state.indexRowPageable;
  }

  @Selector()
  static indexRowSearchResult(state: SearchStateModel) {
    return state.indexRowSearchResult;
  }

  @Selector()
  static indexRowFilters(state: SearchStateModel) {
    return state.indexRowFilters;
  }

  @Selector()
  static file(state: SearchStateModel) {
    return state.file;
  }

  @Action(ResetSearch)
  resetSearch(context: StateContext<SearchStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.type = undefined;
        draft.typeName = undefined;
        draft.typeSearchResult = makeSearchResultPayload();

        draft.indexRowFilters = {};
        draft.indexRowPageable = makePageable(10);
        draft.indexRowSearchResult = makeSearchResultPayload();
      })
    );
  }

  @Action(QueryTypes)
  queryTypes(context: StateContext<SearchStateModel>, { domain, pageable, typeName }: QueryTypes) {
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
      return this.multiTemplateWhatsappApi.queryTypes(pageable, context.getState().typeName).pipe(
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

  @Action(GetType)
  getDigitalFilingType(context: StateContext<SearchStateModel>, { domain, typeId }: GetType) {
    if (domain === DomainType.DIGITAL_FILING) {
      return this.digitalFilingApi.getType(typeId).pipe(
        tap((response: ApiResponseModel<DigitalFilingTypeModel>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.type = response.data;
            })
          );
        })
      );
    } else if (domain === DomainType.TRANSACTIONAL_EMAIL) {
      return this.transactionalEmailApi.getType(typeId).pipe(
        tap((response: ApiResponseModel<TransactionalEmailTypeModel>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.type = response.data;
            })
          );
        })
      );
    } else if (domain === DomainType.MT_TRANSACTIONAL_EMAIL) {
      return this.multiTemplateTransactionalEmailApi.getType(typeId).pipe(
        tap((response: ApiResponseModel<MultiTemplateTransactionalEmailTypeModel>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.type = response.data;
            })
          );
        })
      );
    } else if (domain === DomainType.EMAIL_CAMPAIGN) {
      return this.emailCampaignApi.getType(typeId).pipe(
        tap((response: ApiResponseModel<EmailCampaignTypeModel>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.type = response.data;
            })
          );
        })
      );
    } else if (domain === DomainType.WHATSAPP) {
      return this.whatsAppApi.getType(typeId).pipe(
        tap((response: ApiResponseModel<WhatsAppTypeModel>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.type = response.data;
            })
          );
        })
      );
    } else if (domain === DomainType.MT_WHATSAPP) {
      return this.multiTemplateWhatsappApi.getType(typeId).pipe(
        tap((response: ApiResponseModel<MultiTemplateWhatsappTypeModel>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.type = response.data;
            })
          );
        })
      );
    } else {
      return this.smsApi.getType(typeId).pipe(
        tap((response: ApiResponseModel<SmsTypeModel>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.type = response.data;
            })
          );
        })
      );
    }
  }

  @Action(DownloadFile)
  downloadDigitalFilingFile(
    context: StateContext<SearchStateModel>,
    { domain, typeId, activityId, fileId }: DownloadFile
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.file = undefined;
      })
    );

    if (domain === DomainType.DIGITAL_FILING) {
      return this.digitalFilingApi.downloadFile(typeId, activityId, fileId).pipe(
        tap((file) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.file = file;
            })
          );
        })
      );
    } else if (domain === DomainType.TRANSACTIONAL_EMAIL) {
      return this.transactionalEmailApi.downloadFile(typeId, activityId, fileId).pipe(
        tap((file) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.file = file;
            })
          );
        })
      );
    } else if (domain === DomainType.MT_TRANSACTIONAL_EMAIL) {
      return this.multiTemplateTransactionalEmailApi.downloadFile(typeId, activityId, fileId).pipe(
        tap((file) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.file = file;
            })
          );
        })
      );
    } else if (domain === DomainType.MT_WHATSAPP) {
      return this.multiTemplateWhatsappApi.downloadFile(typeId, activityId, fileId).pipe(
        tap((file) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.file = file;
            })
          );
        })
      );
    } else {
      return this.whatsAppApi.downloadFile(typeId, activityId, fileId).pipe(
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

  @Action(DeleteFile)
  deleteDigitalFilingFile(context: StateContext<SearchStateModel>, { domain, typeId, activityId, fileId }: DeleteFile) {
    if (domain === DomainType.DIGITAL_FILING) {
      return this.digitalFilingApi.deleteFile(typeId, activityId, fileId);
    } else if (domain === DomainType.TRANSACTIONAL_EMAIL) {
      return this.transactionalEmailApi.deleteFile(typeId, activityId, fileId);
    } else {
      return this.whatsAppApi.deleteFile(typeId, activityId, fileId);
    }
  }

  @Action(QueryTypeIndexRecords)
  queryDigitalFilingTypeFiles(
    context: StateContext<SearchStateModel>,
    { domain, pageable, indexRowFilters, typeId }: QueryTypeIndexRecords
  ) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.indexRowPageable = pageable ? pageable : draft.indexRowPageable;
        draft.indexRowFilters = indexRowFilters ? indexRowFilters : draft.indexRowFilters;
      })
    );

    if (domain === DomainType.DIGITAL_FILING) {
      return this.digitalFilingApi
        .queryRecords(context.getState().indexRowPageable, typeId, context.getState().indexRowFilters)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.indexRowSearchResult = response.data;
              })
            );
          })
        );
    } else if (domain === DomainType.TRANSACTIONAL_EMAIL) {
      return this.transactionalEmailApi
        .queryRecords(context.getState().indexRowPageable, typeId, context.getState().indexRowFilters)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.indexRowSearchResult = response.data;
              })
            );
          })
        );
    } else if (domain === DomainType.MT_TRANSACTIONAL_EMAIL) {
      return this.multiTemplateTransactionalEmailApi
        .queryRecords(context.getState().indexRowPageable, typeId, context.getState().indexRowFilters)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.indexRowSearchResult = response.data;
              })
            );
          })
        );
    } else if (domain === DomainType.EMAIL_CAMPAIGN) {
      return this.emailCampaignApi
        .queryRecords(context.getState().indexRowPageable, typeId, context.getState().indexRowFilters)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.indexRowSearchResult = response.data;
              })
            );
          })
        );
    } else if (domain === DomainType.WHATSAPP) {
      return this.whatsAppApi
        .queryRecords(context.getState().indexRowPageable, typeId, context.getState().indexRowFilters)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.indexRowSearchResult = response.data;
              })
            );
          })
        );
    } else if (domain === DomainType.MT_WHATSAPP) {
      return this.multiTemplateWhatsappApi
        .queryRecords(context.getState().indexRowPageable, typeId, context.getState().indexRowFilters)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.indexRowSearchResult = response.data;
              })
            );
          })
        );
    } else {
      return this.smsApi
        .queryRecords(context.getState().indexRowPageable, typeId, context.getState().indexRowFilters)
        .pipe(
          tap((response: ApiResponseModel<SearchResultPayloadModel<BaseIndexRowModel>>) => {
            context.setState(
              produce(context.getState(), (draft) => {
                draft.indexRowSearchResult = response.data;
              })
            );
          })
        );
    }
  }

  @Action(ResetIndexRecords)
  resetIndexRecords(context: StateContext<SearchStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.indexRowPageable = makePageable(10);
        draft.indexRowFilters = {};
      })
    );
  }
}
