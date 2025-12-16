import { AffiliateSubCodeDetailsModel } from './affiliate-sub-code-details.model';

export interface AffiliateCodeDetailsModel {
  id: number;
  name: string;
  code: string;
  subCodes: AffiliateSubCodeDetailsModel[];
  createdTime: Date;
  lastModifiedTime: Date;
}
