import {
  BaseIndexRowModel, MultiTemplateWhatsappTypeModel, MultiTemplateWhatsappTypeRequestModel,
  PageableModel,
  WhatsAppActivityCreateRequestModel,
  WhatsAppActivityRequestModel,
  WhatsAppTypeModel,
  WhatsAppTypeRequestModel
} from "@grabbill/lib";

export class GetWhatsAppTemplates {
  static readonly type = '[WhatsApp] GetTemplates';
}

export class CreateWhatsAppTemplate {
  static readonly type = '[WhatsApp] Create Template';

  constructor(public formData: FormData) {}
}

export class DeleteWhatsAppTemplate {
  static readonly type = '[WhatsApp] Delete Template';

  constructor(public id: string) {}
}

export class UpdateAutoReplyMessage {
  static readonly type = '[WhatsApp] Update Auto Reply Message';

  constructor(public message: string) {}
}

export class QueryWhatsAppEvents {
  static readonly type = '[WhatsApp] QueryWhatsAppEvents';

  constructor(
    public pageable?: PageableModel,
    public endDate?: Date,
    public startDate?: Date,
    public mobileNo?: string
  ) {}
}

export class ResetWhatsAppEvents {
  static readonly type = '[WhatsApp] ResetWhatsAppEvents';
}

export class ResetWhatsAppTypes {
  static readonly type = '[WhatsApp] ResetWhatsAppTypes';
}

export class QueryWhatsAppTypes {
  static readonly type = '[WhatsApp] QueryWhatsAppTypes';

  constructor(public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreWhatsAppTypes {
  static readonly type = '[WhatsApp] LoadMoreWhatsAppTypes';
}

export class GetWhatsAppType {
  static readonly type = '[WhatsApp] GetWhatsAppType';

  constructor(public typeId: number) {}
}

export class ResetWhatsAppType {
  static readonly type = '[WhatsApp] ResetWhatsAppType';
}

export class NewWhatsAppType {
  static readonly type = '[WhatsApp] NewWhatsAppType';

  constructor(public request: WhatsAppTypeRequestModel) {}
}

export class UpdateWhatsAppType {
  static readonly type = '[WhatsApp] UpdateWhatsAppType';

  constructor(public typeId: number, public request: WhatsAppTypeRequestModel) {}
}

export class DeleteWhatsAppType {
  static readonly type = '[WhatsApp] DeleteWhatsAppType';

  constructor(public typeId: number) {}
}

export class DuplicateWhatsAppType {
  static readonly type = '[WhatsApp] DuplicateWhatsAppType';

  constructor(public typeId: number) {}
}

export class QueryWhatsAppActivities {
  static readonly type = '[WhatsApp] QueryWhatsAppActivities';

  constructor(public typeId: number, public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreWhatsAppActivities {
  static readonly type = '[WhatsApp] LoadMoreWhatsAppActivities';

  constructor(public typeId: number) {}
}

export class GetWhatsAppActivity {
  static readonly type = '[WhatsApp] GetWhatsAppActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ResetWhatsAppActivity {
  static readonly type = '[WhatsApp] ResetWhatsAppActivity';
}

export class NewWhatsAppActivity {
  static readonly type = '[WhatsApp] NewWhatsAppActivity';

  constructor(public typeId: number, public request: WhatsAppActivityCreateRequestModel) {}
}

export class UpdateWhatsAppActivity {
  static readonly type = '[WhatsApp] UpdateWhatsAppActivity';

  constructor(public typeId: number, public activityId: number, public request: WhatsAppActivityRequestModel) {}
}

export class DeleteWhatsAppActivity {
  static readonly type = '[WhatsApp] DeleteWhatsAppActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ExportWhatsAppActivity {
  static readonly type = '[WhatsApp] ExportWhatsAppActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class PurgeWhatsAppActivity {
  static readonly type = '[WhatsApp] PurgeWhatsAppActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class UploadWhatsAppFile {
  static readonly type = '[WhatsApp] UploadWhatsAppFile';

  constructor(public typeId: number, public activityId: number, public fileFormData: FormData) {}
}

export class DeleteWhatsAppFiles {
  static readonly type = '[WhatsApp] DeleteWhatsAppFiles';

  constructor(public typeId: number, public activityId: number) {}
}

export class DeleteWhatsAppFile {
  static readonly type = '[WhatsApp] DeleteWhatsAppFile';

  constructor(public typeId: number, public activityId: number, public fileId: number) {}
}

export class DownloadWhatsAppFile {
  static readonly type = '[WhatsApp] DownloadWhatsAppFile';

  constructor(public typeId: number, public activityId: number, public fileId: number, public filename: string) {}
}

export class DownloadIndexData {
  static readonly type = '[WhatsApp] DownloadIndexData';

  constructor(
    public indexRows: BaseIndexRowModel[],
    public type: WhatsAppTypeModel,
    public generateSampleData: boolean = false
  ) {}
}

export class QueryWhatsAppTypeFiles {
  static readonly type = '[WhatsApp] QueryWhatsAppTypeFiles';

  constructor(
    public typeId: number,
    public pageable?: PageableModel,
    public name?: string,
    public fileFilters?: { [index: string]: any }
  ) {}
}


export class ResetMultiTemplateWhatsAppTypes {
  static readonly type = '[WhatsApp] ResetMultiTemplateWhatsAppTypes';
}

export class QueryMultiTemplateWhatsAppTypes {
  static readonly type = '[WhatsApp] QueryMultiTemplateWhatsAppTypes';

  constructor(public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreMultiTemplateWhatsAppTypes {
  static readonly type = '[WhatsApp] LoadMoreMultiTemplateWhatsAppTypes';
}

export class GetMultiTemplateWhatsAppType {
  static readonly type = '[WhatsApp] GetMultiTemplateWhatsAppType';

  constructor(public typeId: number) {}
}

export class ResetMultiTemplateWhatsAppType {
  static readonly type = '[WhatsApp] ResetMultiTemplateWhatsAppType';
}

export class NewMultiTemplateWhatsAppType {
  static readonly type = '[WhatsApp] NewMultiTemplateWhatsAppType';

  constructor(public request: MultiTemplateWhatsappTypeRequestModel) {}
}

export class UpdateMultiTemplateWhatsAppType {
  static readonly type = '[WhatsApp] UpdateMultiTemplateWhatsAppType';

  constructor(public typeId: number, public request: MultiTemplateWhatsappTypeRequestModel) {}
}

export class DeleteMultiTemplateWhatsAppType {
  static readonly type = '[WhatsApp] DeleteMultiTemplateWhatsAppType';

  constructor(public typeId: number) {}
}

export class DuplicateMultiTemplateWhatsAppType {
  static readonly type = '[WhatsApp] DuplicateMultiTemplateWhatsAppType';

  constructor(public typeId: number) {}
}

export class QueryMultiTemplateWhatsAppActivities {
  static readonly type = '[WhatsApp] QueryMultiTemplateWhatsAppActivities';

  constructor(public typeId: number, public pageable?: PageableModel, public name?: string) {}
}

export class LoadMoreMultiTemplateWhatsAppActivities {
  static readonly type = '[WhatsApp] LoadMoreMultiTemplateWhatsAppActivities';

  constructor(public typeId: number) {}
}

export class GetMultiTemplateWhatsAppActivity {
  static readonly type = '[WhatsApp] GetMultiTemplateWhatsAppActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ResetMultiTemplateWhatsAppActivity {
  static readonly type = '[WhatsApp] ResetMultiTemplateWhatsAppActivity';
}

export class NewMultiTemplateWhatsAppActivity {
  static readonly type = '[WhatsApp] NewMultiTemplateWhatsAppActivity';

  constructor(public typeId: number, public request: WhatsAppActivityCreateRequestModel) {}
}

export class UpdateMultiTemplateWhatsAppActivity {
  static readonly type = '[WhatsApp] UpdateMultiTemplateWhatsAppActivity';

  constructor(public typeId: number, public activityId: number, public request: WhatsAppActivityRequestModel) {}
}

export class ValidateMultiTemplateWhatsAppActivitySftp {
  static readonly type = '[WhatsApp] ValidateMultiTemplateWhatsAppActivitySftp';

  constructor(public typeId: number, public activityId: number, public request: WhatsAppActivityRequestModel) {}
}

export class DeleteMultiTemplateWhatsAppActivity {
  static readonly type = '[WhatsApp] DeleteMultiTemplateWhatsAppActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class ExportMultiTemplateWhatsAppActivity {
  static readonly type = '[WhatsApp] ExportMultiTemplateWhatsAppActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class PurgeMultiTemplateWhatsAppActivity {
  static readonly type = '[WhatsApp] PurgeMultiTemplateWhatsAppActivity';

  constructor(public typeId: number, public activityId: number) {}
}

export class UploadMultiTemplateWhatsAppFile {
  static readonly type = '[WhatsApp] UploadMultiTemplateWhatsAppFile';

  constructor(public typeId: number, public activityId: number, public fileFormData: FormData) {}
}

export class DeleteMultiTemplateWhatsAppFiles {
  static readonly type = '[WhatsApp] DeleteMultiTemplateWhatsAppFiles';

  constructor(public typeId: number, public activityId: number) {}
}

export class DeleteMultiTemplateWhatsAppFile {
  static readonly type = '[WhatsApp] DeleteMultiTemplateWhatsAppFile';

  constructor(public typeId: number, public activityId: number, public fileId: number) {}
}

export class DownloadMultiTemplateWhatsAppFile {
  static readonly type = '[WhatsApp] DownloadMultiTemplateWhatsAppFile';

  constructor(public typeId: number, public activityId: number, public fileId: number, public filename: string) {}
}

export class DownloadMultiTemplateIndexData {
  static readonly type = '[WhatsApp] DownloadMultiTemplateIndexData';

  constructor(
    public indexRows: BaseIndexRowModel[],
    public type: MultiTemplateWhatsappTypeModel,
    public generateSampleData: boolean = false
  ) {}
}

export class QueryMultiTemplateWhatsAppTypeFiles {
  static readonly type = '[WhatsApp] QueryMultiTemplateWhatsAppTypeFiles';

  constructor(
    public typeId: number,
    public pageable?: PageableModel,
    public name?: string,
    public fileFilters?: { [index: string]: any }
  ) {}
}
