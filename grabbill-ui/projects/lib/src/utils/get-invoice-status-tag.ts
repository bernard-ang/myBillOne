import { InvoiceStatus } from '@grabbill/lib';

export const getInvoiceStatusTag = (status: InvoiceStatus): string => {
  switch (status) {
    case InvoiceStatus.PAID:
    case InvoiceStatus.FREE:
    case InvoiceStatus.PAYMENT_WAIVED:
      return 'success';
    case InvoiceStatus.NEW:
    case InvoiceStatus.VOID:
      return 'default';
    case InvoiceStatus.PAYMENT_FAILED:
      return 'error';
    case InvoiceStatus.PROCESSING:
      return 'processing';
    case InvoiceStatus.CARRY_FORWARD:
      return 'warning';
  }
};
