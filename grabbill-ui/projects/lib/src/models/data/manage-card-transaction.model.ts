import { ManageCardResponseModel } from './manage-card-response.model';
import { ManageCardTransactionBasicModel } from './manage-card-transaction-basic.model';

export interface ManageCardTransactionModel extends ManageCardTransactionBasicModel {
  manageCardResponses: ManageCardResponseModel[];
}
