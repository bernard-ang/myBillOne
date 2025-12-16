import { WhatsappTemplateStatus } from '../models/data/whatsapp';

export const getWabaStatusTag = (status: WhatsappTemplateStatus): string => {
  switch (status) {
    case WhatsappTemplateStatus.APPROVED:
      return 'success';
    case WhatsappTemplateStatus.REJECTED:
      return 'error';
    default:
      return 'default';
  }
};
