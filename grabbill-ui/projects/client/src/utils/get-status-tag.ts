import { InvoiceStatus, ProcessStatus } from '@grabbill/lib';

export const getStatusTag = (status: ProcessStatus): string => {
  switch (status) {
    case ProcessStatus.COMPLETED:
      return 'success';
    case ProcessStatus.DRAFT:
      return 'default';
    case ProcessStatus.ERROR:
      return 'error';
    case ProcessStatus.PROCESSING:
      return 'processing';
    case ProcessStatus.SUBMITTED:
      return 'warning';
  }
};

export const getInvoiceStatusTag = (status: InvoiceStatus): string => {
  switch (status) {
    case InvoiceStatus.PAID:
    case InvoiceStatus.FREE:
    case InvoiceStatus.PAYMENT_WAIVED:
      return 'success';
    case InvoiceStatus.NEW:
    case InvoiceStatus.PROCESSING:
      return 'processing';
    case InvoiceStatus.CARRY_FORWARD:
      return 'warning';
    case InvoiceStatus.PAYMENT_FAILED:
      return 'error';
    default:
      return 'default';
  }
};
