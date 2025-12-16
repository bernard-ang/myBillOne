import { BaseActivityBasicModel } from '../base-activity-basic.model';

export interface TransactionalEmailActivityBasicModel extends BaseActivityBasicModel {
  total: number;
  sent: number;
  opened: number;
  bounced: number;
  bouncedSkip: number;
  unsubscribedSkip: number;
}
