import { AuditableModel } from "../common";

export interface ContactGroupBasicModel extends AuditableModel {
  id: number;
  name: string;
  description: string;
}

