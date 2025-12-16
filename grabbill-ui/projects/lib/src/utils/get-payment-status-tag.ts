import { PaymentStatus } from '@grabbill/lib';

export const getPaymentStatusTag = (status: PaymentStatus): string => {
  switch (status) {
    case PaymentStatus.SUCCESS:
      return 'success';
    case PaymentStatus.FAILED:
      return 'error';
  }
};
