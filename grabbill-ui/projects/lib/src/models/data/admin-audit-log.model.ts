import { AdminDomainType } from './admin-domain-type';

export interface AdminAuditLogModel {
  id: number;
  adminDomainType: AdminDomainType;
  targetId: number;
  actionType: string;
  description: string;
  createdBy: string;
  createdDate: Date;
}
