import { DomainType, Privilege } from '@grabbill/lib';
import { Params } from '@angular/router';
import { isSaasMode } from "./utils/deployment-mode";

export interface RouteInfo {
  domainType: DomainType;
  icon: string;
  path: string[];
  privilege: Privilege;
  menuTitle: string;
  queryParams?: Params;
  isExperimental: boolean;
  isWabaRequired?: boolean;
  mode?: 'saas' | 'on-premise';
  tooltip?: string;
}

export const TypeRoutes: RouteInfo[] = [
  {
    domainType: DomainType.TRANSACTIONAL_EMAIL,
    icon: 'file-pdf',
    path: ['/', 'transactional-email', 'list'],
    privilege: Privilege.TRX_EMAIL_VIEW,
    menuTitle: 'Transactional Emails',
    isExperimental: false,
    mode: 'saas',
    tooltip: 'Send personalised email with unique PDF attachments.'
  },
  {
    domainType: DomainType.MT_TRANSACTIONAL_EMAIL,
    icon: 'file-pdf',
    path: ['/', 'mt-transactional-email', 'list'],
    privilege: Privilege.TRX_EMAIL_VIEW,
    menuTitle: 'Transactional Emails',
    isExperimental: false,
    mode: 'on-premise',
    tooltip: 'Send personalised email with unique PDF attachments.'
  },
  {
    domainType: DomainType.EMAIL_CAMPAIGN,
    icon: 'mail',
    path: ['/', 'email-campaign', 'list'],
    privilege: Privilege.EMAIL_CAMPAIGN_VIEW,
    menuTitle: 'Email Campaigns',
    isExperimental: false,
    tooltip: 'Promote by sending marketing messages to your audience.'
  },
  {
    domainType: DomainType.WHATSAPP,
    icon: 'whats-app',
    path: ['/', 'whatsapp', 'list'],
    privilege: Privilege.WA_VIEW,
    menuTitle: 'WhatsApp',
    isExperimental: false,
    isWabaRequired: true,
    mode: 'saas',
    tooltip: 'Send WhatsApp message with unique PDF attachments.'
  },
  {
    domainType: DomainType.MT_WHATSAPP,
    icon: 'whats-app',
    path: ['/', 'mt-whatsapp', 'list'],
    privilege: Privilege.WA_VIEW,
    menuTitle: 'WhatsApp',
    isExperimental: false,
    isWabaRequired: true,
    mode: 'on-premise',
    tooltip: 'Send WhatsApp message with unique PDF attachments.'
  },
  {
    domainType: DomainType.SMS,
    icon: 'message',
    path: ['/', 'sms', 'list'],
    privilege: Privilege.SMS_VIEW,
    menuTitle: 'SMS',
    isExperimental: false,
    tooltip: 'Send message.'
  },
  {
    domainType: DomainType.CONTACT,
    icon: 'contacts',
    path: ['/', 'contact', 'list'],
    privilege: Privilege.CONTACT_VIEW,
    menuTitle: 'Contacts',
    queryParams: { tab: 'contacts' },
    isExperimental: false,
    tooltip: 'View, organize and group your database.'
  },
  {
    domainType: DomainType.DIGITAL_FILING,
    icon: 'folder',
    path: ['/', 'digital-filing', 'list'],
    privilege: Privilege.DGTL_FILING_VIEW,
    menuTitle: 'Digital Filing',
    isExperimental: false,
    tooltip: 'Securely store, organize and manage your documents.'
  },
];

export const ContactRoutes: RouteInfo[] = [
  {
    domainType: DomainType.CONTACT_GROUP,
    icon: 'contacts',
    path: ['/', 'contact', 'list'],
    privilege: Privilege.CONTACT_GROUP_VIEW,
    menuTitle: 'Contact Groups',
    queryParams: { tab: 'groups' },
    isExperimental: false,
  },
  {
    domainType: DomainType.CONTACT_FIELD,
    icon: 'contacts',
    path: ['/', 'contact', 'list'],
    privilege: Privilege.CONTACT_FIELD_VIEW,
    menuTitle: 'Contact Fields',
    queryParams: { tab: 'fields' },
    isExperimental: false,
  },
];

export const AccountRoutes: RouteInfo[] = [
  {
    domainType: DomainType.USER_PROFILE,
    icon: 'profile',
    path: ['/', 'account', 'profile'],
    privilege: Privilege.MANAGE_PROFILE,
    menuTitle: 'Profile',
    isExperimental: false,
  },
  {
    domainType: DomainType.ACCOUNT,
    icon: 'bank',
    path: ['/', 'account', 'account'],
    privilege: Privilege.MANAGE_ACCOUNT,
    menuTitle: 'Account',
    isExperimental: false,
  },
  {
    domainType: DomainType.ACCOUNT,
    icon: 'database',
    path: ['/', 'account', 'usage'],
    privilege: Privilege.MANAGE_ACCOUNT,
    menuTitle: 'Account Usages',
    queryParams: { tab: 'statement' },
    isExperimental: false,
  },
  {
    domainType: DomainType.USER,
    icon: 'safety',
    path: ['/', 'account', 'roles'],
    privilege: Privilege.MANAGE_USER,
    menuTitle: 'Manage Roles',
    isExperimental: false,
    mode: 'on-premise'
  },
  {
    domainType: DomainType.USER,
    icon: 'team',
    path: ['/', 'account', 'users'],
    privilege: Privilege.MANAGE_USER,
    menuTitle: 'Manage Users',
    isExperimental: false,
  },
  {
    domainType: DomainType.PLAN,
    icon: 'solution',
    path: ['/', 'account', 'plan'],
    privilege: Privilege.MANAGE_PLAN,
    menuTitle: 'Manage Plan & Credits',
    queryParams: { tab: 'overview' },
    isExperimental: false,
    mode: 'saas'
  },
];

export const PlanRoutes: RouteInfo[] = [
  {
    domainType: DomainType.BILLING_INFO,
    icon: 'audit',
    path: ['/', 'account', 'plan'],
    privilege: Privilege.MANAGE_PLAN,
    menuTitle: 'Manage Billing Info',
    queryParams: { tab: 'billing-info' },
    isExperimental: false,
    mode: 'saas'
  },
  {
    domainType: DomainType.INVOICE,
    icon: 'file-text',
    path: ['/', 'account', 'plan'],
    privilege: Privilege.MANAGE_PLAN,
    menuTitle: 'Manage Invoice',
    queryParams: { tab: 'billing-history' },
    isExperimental: false,
    mode: 'saas'
  },
];

export const ImageRoutes: RouteInfo[] = [
  {
    domainType: DomainType.IMAGE_FOLDER,
    icon: 'folder',
    path: ['/', 'account', 'images'],
    privilege: Privilege.MANAGE_IMAGES,
    menuTitle: 'Manage Image Folder',
    isExperimental: false,
  },
];

export const MiscRoutes: RouteInfo[] = [
  {
    domainType: DomainType.MAIL_SERVER,
    icon: 'mail',
    path: ['/', 'account', 'mail-server'],
    privilege: Privilege.MANAGE_SMTP_IMAP,
    menuTitle: 'Mail Server Settings',
    isExperimental: false,
  },
  {
    domainType: DomainType.ACCOUNT,
    icon: 'mail',
    path: ['/', 'account', 'sftp-settings'],
    privilege: Privilege.MANAGE_ACCOUNT,
    menuTitle: 'SFTP Settings',
    isExperimental: false,
  },
  {
    domainType: DomainType.IMAGE,
    icon: 'picture',
    path: ['/', 'account', 'images'],
    privilege: Privilege.MANAGE_IMAGES,
    menuTitle: 'Images',
    isExperimental: false,
  },
  {
    domainType: DomainType.BOUNCED_EMAIL,
    icon: 'exception',
    path: ['/', 'account', 'bounced-emails'],
    privilege: Privilege.MANAGE_BOUNCED_EMAIL,
    menuTitle: 'Bounced Emails',
    isExperimental: false,
  },
  {
    domainType: DomainType.UNSUBSCRIBED_EMAIL,
    icon: 'inbox',
    path: ['/', 'account', 'unsubscribed-emails'],
    privilege: Privilege.MANAGE_UNSUBSCRIBED_EMAIL,
    menuTitle: 'Unsubscribed Emails',
    isExperimental: false,
  },
  {
    domainType: DomainType.WHATSAPP_TEMPLATE,
    icon: 'whats-app',
    path: ['/', 'account', 'whatsapp-settings'],
    privilege: Privilege.MANAGE_ACCOUNT,
    queryParams: { tab: isSaasMode() ? 'template': 'waba' },
    menuTitle: 'WhatsApp Settings',
    isExperimental: false,
    isWabaRequired: true
  },
  {
    domainType: DomainType.WHATSAPP_MESSAGE,
    icon: 'message',
    path: ['/', 'account', 'whatsapp-received-message'],
    privilege: Privilege.MANAGE_ACCOUNT,
    menuTitle: 'WhatsApp Received Message',
    isExperimental: false,
    isWabaRequired: true
  },
  {
    domainType: DomainType.JOB,
    icon: 'container',
    path: ['/', 'account', 'jobs'],
    privilege: Privilege.AUDIT,
    menuTitle: 'Jobs',
    isExperimental: false,
    mode: 'on-premise'
  },
  {
    domainType: DomainType.PLAN,
    icon: 'history',
    path: ['/', 'account', 'audits'],
    privilege: Privilege.AUDIT,
    menuTitle: 'Audits',
    isExperimental: false,
  },
];

export const AllRoutes: RouteInfo[] = [
  ...TypeRoutes,
  ...ContactRoutes,
  ...AccountRoutes,
  ...MiscRoutes,
  ...PlanRoutes,
  ...ImageRoutes,
];
