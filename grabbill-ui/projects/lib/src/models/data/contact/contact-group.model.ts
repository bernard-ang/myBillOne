import { AuditableModel } from '../common';

export interface ContactGroupModel extends AuditableModel {
  id: number;
  name: string;
  description: string;
}
