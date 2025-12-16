import { AuditableModel } from '../common';
import { BaseTypeDataModel } from '../base-type-data.model';
import { ContactGroupModel } from './contact-group.model';

export interface ContactModel extends AuditableModel, BaseTypeDataModel {
  id: number;
  email: string;
  mobileNo: string;

  groups: ContactGroupModel[];
}
