import { DomainType } from './domain-type';

export interface AuditLogModel {
  id: number;
  domainType: DomainType;
  actionType: string;
  targetId: number;
  parentTypeId?: number;
  description: string;
  createdBy: string;
  createdDate: Date;
}
