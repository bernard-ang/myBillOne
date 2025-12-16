import { DomainType } from '@grabbill/lib';

export interface RouteInfo {
  domainType: DomainType;
  icon: string;
  path: string[];
  menuTitle: string;
}

export const AllRoutes: RouteInfo[] = [
  {
    domainType: DomainType.ACCOUNT,
    icon: 'bank',
    path: ['/', 'account', 'list'],
    menuTitle: 'Accounts',
  },
  {
    domainType: DomainType.AFFILIATE_CODE,
    icon: 'container',
    path: ['/', 'affiliate-codes'],
    menuTitle: 'Affiliate Codes',
  },
  {
    domainType: DomainType.PROMO_CODE,
    icon: 'container',
    path: ['/', 'promo-codes', 'list'],
    menuTitle: 'Promo Codes',
  },
  {
    domainType: DomainType.ACCOUNT,
    icon: 'container',
    path: ['/', 'job', 'list'],
    menuTitle: 'Jobs',
  },
  {
    domainType: DomainType.INVOICE,
    icon: 'file-text',
    path: ['/', 'invoice', 'list'],
    menuTitle: 'Invoices',
  },
  {
    domainType: DomainType.STRIPE_EVENT,
    icon: 'dollar',
    path: ['/', 'stripe-event', 'list'],
    menuTitle: 'Stripe Events',
  },
  {
    domainType: DomainType.ACCOUNT,
    icon: 'team',
    path: ['/', 'admin-users'],
    menuTitle: 'Admin Users',
  },
  {
    domainType: DomainType.ACCOUNT,
    icon: 'history',
    path: ['/', 'audits'],
    menuTitle: 'Audits',
  },
];
