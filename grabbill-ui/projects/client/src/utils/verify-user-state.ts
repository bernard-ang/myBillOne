import { UserAuthorityModel } from '@grabbill/lib';
import { Params } from '@angular/router';

export const verifyUserState = (
  user: UserAuthorityModel,
  redirectToHome = false,
  targetUrl = '',
  params: Params = {}
): { paths: string[]; queryParams?: Params } => {
  if (!user.verified) {
    return { paths: ['/', 'register', 'verify-email'], queryParams: { email: user.email } };
  }

  if (user.account.companyName == null) {
    return { paths: ['/', 'register', 'account'] };
  }

  if (user.subscription == null) {
    if (targetUrl.startsWith('/register/payment-method-summary')) {
      return { paths: [] };
    } else if (targetUrl === '/register/plan-selection-summary') {
      return { paths: [] };
    } else if (targetUrl === '/register/switch-plan') {
      return { paths: [] };
    } else {
      return { paths: ['/', 'register', 'plan-selection'] };
    }
  }

  if (user.paymentMethodRequired) {
    if (targetUrl === '/register/plan-selection') {
      return { paths: [] };
    } else if (targetUrl.startsWith('/register/payment-method-summary')) {
      return { paths: [] };
    }
  }

  if (redirectToHome) {
    return { paths: ['/'] };
  }

  return { paths: [] };
};
