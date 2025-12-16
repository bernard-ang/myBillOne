import {
  BaseActivitySftpValidationSummaryModel,
  BaseFileModel,
  BaseIndexRowModel,
  DownloadFile,
  MultiTemplateWhatsappActivityModel,
  MultiTemplateWhatsappTypeModel,
  PageableModel,
  SearchResultPayloadModel,
  WhatsAppActivityBasicModel,
  WhatsAppActivityModel,
  WhatsappEventBasicModel,
  WhatsappTemplateModel,
  WhatsAppTypeBasicModel,
  WhatsAppTypeModel
} from "@grabbill/lib";

export interface WhatsAppStateModel {
  templates: WhatsappTemplateModel[];

  mobileNo?: string;
  startDate?: Date;
  endDate?: Date;
  eventPageable: PageableModel;
  eventSearchResult: SearchResultPayloadModel<WhatsappEventBasicModel>;

  typeName?: string;
  whatsAppType?: WhatsAppTypeModel;
  whatsAppTypePageable: PageableModel;
  whatsAppTypeSearchResult: SearchResultPayloadModel<WhatsAppTypeBasicModel>;

  whatsAppActivity?: WhatsAppActivityModel;
  whatsAppActivityPageable: PageableModel;
  whatsAppActivitySearchResult: SearchResultPayloadModel<WhatsAppActivityBasicModel>;

  multiTemplateWhatsAppType?: MultiTemplateWhatsappTypeModel;
  multiTemplateWhatsAppTypePageable: PageableModel;
  multiTemplateWhatsAppTypeSearchResult: SearchResultPayloadModel<WhatsAppTypeBasicModel>;

  multiTemplateWhatsAppActivityError?: BaseActivitySftpValidationSummaryModel;
  multiTemplateWhatsAppActivity?: MultiTemplateWhatsappActivityModel;
  multiTemplateWhatsAppActivityPageable: PageableModel;
  multiTemplateWhatsAppActivitySearchResult: SearchResultPayloadModel<WhatsAppActivityBasicModel>;

  activityName?: string;

  fileName?: string;
  fileFilters: { [index: string]: any };
  whatsAppFilePageable: PageableModel;
  whatsAppFileSearchResult: SearchResultPayloadModel<BaseIndexRowModel>;

  whatsAppActivityFiles: BaseFileModel[];

  file?: DownloadFile;
}
