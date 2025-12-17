/**
  * create schema
 */
CREATE DATABASE IF NOT EXISTS `grabbill`;

GRANT ALL ON `grabbill`.* TO 'grabbill.root'@'%';

/**
  create grabbill tables
 */
USE grabbill;

-- SEQUENCE removed for MySQL
-- SEQUENCE removed for MySQL
-- SEQUENCE removed for MySQL
-- SEQUENCE removed for MySQL
-- SEQUENCE removed for MySQL
-- SEQUENCE removed for MySQL
-- SEQUENCE removed for MySQL
-- SEQUENCE removed for MySQL
-- SEQUENCE removed for MySQL

create table role
(
    id      int             not null primary key,
    name    varchar(255)    null
);

create table privilege
(
    id      int             not null primary key,
    name    varchar(255)    null
);

create table roles_privileges
(
    role_id      int not null,
    privilege_id int not null,
    constraint FK_PRIVILEGE_ID  foreign key (privilege_id) references privilege (id),
    constraint FK_ROLE_ID       foreign key (role_id) references role (id)
);

create table plan
(
    id                  int          not null primary key,
    description         varchar(255) null,
    name                varchar(255) null,
    custom_smtp         bit          null,
    export_file         bit          null,
    grabbill_logo       bit          null,
    max_attachment_size bigint(20)   null,
    max_user            int          null,
    reporting           bit          null,
    schedule_email      bit          null,
    support_days        int          null
);

create table storage_plan_option (
     id      int        not null primary key,
     price   double     null,
     size    bigint(20) null,
     plan_id int        not null,
     CONSTRAINT FK_STORAGE_PLAN_ID FOREIGN KEY (plan_id) REFERENCES plan (id)
);

create table txe_plan_option (
     id      int        not null primary key,
     price   double     null,
     size    bigint(20) null,
     plan_id int        not null,
     CONSTRAINT FK_TXE_PLAN_ID FOREIGN KEY (plan_id) REFERENCES plan (id)
);

create table ec_plan_option (
    id      int        not null primary key,
    price   double     null,
    size    bigint(20) null,
    plan_id int        not null,
    CONSTRAINT FK_EC_PLAN_ID FOREIGN KEY (plan_id) REFERENCES plan (id)
);

create table credits_plan_option (
    id                int          not null primary key,
    description       varchar(255) null,
    name              varchar(255) not null,
    price             double       null,
    quantity          int          null,
    stripe_product_id varchar(255) not null,
    type              varchar(255) not null
);

create table account
(
    id                      int          not null   primary key,
    created_by              varchar(255) not null,
    created_date            datetime(6)  null,
    last_modified_by        varchar(255) not null,
    last_modified_date      datetime(6)  null,
    addr_line1              varchar(255) null,
    addr_line2              varchar(255) null,
    affiliate_master_code   varchar(255) null,
    affiliate_sub_code      varchar(255) null,
    city                    varchar(255) null,
    company_contact_no      varchar(255) null,
    company_name            varchar(255) null,
    country                 varchar(255) null,
    country_iso_code        varchar(255) null,
    payment_exempted        bit          not null,
    postcode                varchar(255) null,
    state                   varchar(255) null,
    stripe_customer_id      varchar(255) null,
    waba_auto_reply_message varchar(255) null,
    waba_email              varchar(255) null,
    waba_guid               varchar(255) null,
    waba_id                 varchar(255) null,
    waba_name               varchar(255) null,
    waba_password           varchar(255) null,
    waba_phone              varchar(255) null,
    waba_phone_id           varchar(255) null,
    waba_webhook_id         varchar(255) null,
    waba_webhook_url        varchar(255) null,
    logo                    longtext     null
);

create table user
(
    id                 int          not null primary key,
    email              varchar(255) null,
    name               varchar(255) null,
    password           varchar(255) null,
    active             bit          not null,
    verified           bit          not null,
    account_active     bit          not null,
    role_id            int          not null,
    account_id         int          not null,
    created_by         varchar(255) not null,
    created_date       datetime(6)  null,
    last_modified_by   varchar(255) not null,
    last_modified_date datetime(6)  null,
    last_logged_in     datetime(6)  null,
    constraint FK_USER_ACCOUNT_ID   foreign key (account_id) references account (id),
    constraint FK_USER_ROLE_ID      foreign key (role_id) references role (id)
);

INSERT INTO grabbill.role (id, name) VALUES (1, 'OWNER');
INSERT INTO grabbill.role (id, name) VALUES (2, 'ADMIN');
INSERT INTO grabbill.role (id, name) VALUES (3, 'MANAGER');
INSERT INTO grabbill.role (id, name) VALUES (4, 'AUTHOR');
INSERT INTO grabbill.role (id, name) VALUES (5, 'VIEWER');

INSERT INTO grabbill.privilege (id, name) VALUES (1, 'MANAGE_PROFILE');
INSERT INTO grabbill.privilege (id, name) VALUES (2, 'MANAGE_ACCOUNT');
INSERT INTO grabbill.privilege (id, name) VALUES (3, 'MANAGE_PLAN');
INSERT INTO grabbill.privilege (id, name) VALUES (4, 'MANAGE_USER');
INSERT INTO grabbill.privilege (id, name) VALUES (5, 'MANAGE_SMTP_IMAP');
INSERT INTO grabbill.privilege (id, name) VALUES (6, 'MANAGE_IMAGES');
INSERT INTO grabbill.privilege (id, name) VALUES (7, 'MANAGE_BOUNCED_EMAIL');
INSERT INTO grabbill.privilege (id, name) VALUES (8, 'MANAGE_UNSUBSCRIBED_EMAIL');
INSERT INTO grabbill.privilege (id, name) VALUES (9, 'CONTACT_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (10, 'CONTACT_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (11, 'CONTACT_FIELD_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (12, 'CONTACT_FIELD_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (13, 'CONTACT_GROUP_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (14, 'CONTACT_GROUP_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (15, 'DGTL_FILING_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (16, 'DGTL_FILING_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (17, 'DGTL_FILING_ACTIVITY_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (18, 'DGTL_FILING_ACTIVITY_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (19, 'TRX_EMAIL_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (20, 'TRX_EMAIL_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (21, 'TRX_EMAIL_ACTIVITY_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (22, 'TRX_EMAIL_ACTIVITY_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (23, 'EMAIL_CAMPAIGN_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (24, 'EMAIL_CAMPAIGN_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (25, 'EMAIL_CAMPAIGN_ACTIVITY_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (26, 'EMAIL_CAMPAIGN_ACTIVITY_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (27, 'SEARCH_RECORD');
INSERT INTO grabbill.privilege (id, name) VALUES (28, 'AUDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (29, 'DASHBOARD');
INSERT INTO grabbill.privilege (id, name) VALUES (30, 'SMS_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (31, 'SMS_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (32, 'SMS_ACTIVITY_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (33, 'SMS_ACTIVITY_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (34, 'WA_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (35, 'WA_VIEW');
INSERT INTO grabbill.privilege (id, name) VALUES (36, 'WA_ACTIVITY_EDIT');
INSERT INTO grabbill.privilege (id, name) VALUES (37, 'WA_ACTIVITY_VIEW');

INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 1);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 2);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 3);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 4);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 5);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 6);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 7);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 8);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 9);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 10);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 11);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 12);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 13);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 14);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 15);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 16);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 17);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 18);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 19);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 20);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 21);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 22);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 23);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 24);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 25);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 26);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 27);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 28);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 29);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 30);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 31);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 32);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 33);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 34);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 35);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 36);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (1, 37);

INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 1);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 5);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 6);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 7);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 8);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 9);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 10);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 11);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 12);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 13);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 14);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 15);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 16);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 17);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 18);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 19);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 20);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 21);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 22);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 23);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 24);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 25);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 26);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 27);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 28);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 29);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 30);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 31);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 32);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 33);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 34);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 35);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 36);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (2, 37);

INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 1);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 9);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 10);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 11);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 12);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 13);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 14);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 15);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 16);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 17);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 18);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 19);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 20);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 21);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 22);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 23);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 24);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 25);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 26);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 27);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 29);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 30);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 31);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 32);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 33);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 34);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 35);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 36);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (3, 37);

INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 1);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 10);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 12);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 14);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 16);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 17);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 18);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 20);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 21);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 22);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 24);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 25);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 26);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 27);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 29);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 31);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 32);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 33);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 35);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 36);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (4, 37);

INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 1);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 10);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 12);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 14);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 16);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 18);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 20);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 22);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 24);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 26);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 27);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 29);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 31);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 33);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 35);
INSERT INTO grabbill.roles_privileges (role_id, privilege_id) VALUES (5, 37);

INSERT INTO plan (id, description, name, custom_smtp, export_file, grabbill_logo, max_attachment_size, max_user, reporting, schedule_email, support_days) VALUES (1, 'Basic equipment for business that just getting started.', 'Free', false, false, true, 2000000, 2, false, false, 7);
INSERT INTO plan (id, description, name, custom_smtp, export_file, grabbill_logo, max_attachment_size, max_user, reporting, schedule_email, support_days) VALUES (2, 'Basic equipment for a small-scale business.', 'Basic', true, true, false, 5000000, 5, true, true, 30);
INSERT INTO plan (id, description, name, custom_smtp, export_file, grabbill_logo, max_attachment_size, max_user, reporting, schedule_email, support_days) VALUES (3, 'Standard equipment for a small to medium scale business', 'Standard', true, true, false, 8000000, 8, true, true, 60);
INSERT INTO plan (id, description, name, custom_smtp, export_file, grabbill_logo, max_attachment_size, max_user, reporting, schedule_email, support_days) VALUES (4, 'Professional equipment for large scale business.', 'Professional', true, true, false, 10000000, 10, true, true, 90);

/* Digital Filing Plan */
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (1, 0, 500000000, 1);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (2, 19, 20000000000, 2);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (3, 38, 40000000000, 2);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (4, 58, 60000000000, 2);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (5, 77, 80000000000, 2);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (6, 72, 100000000000, 3);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (7, 108, 150000000000, 3);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (8, 144, 200000000000, 3);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (9, 180, 250000000000, 3);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (10, 216, 300000000000, 4);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (11, 288, 400000000000, 4);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (12, 360, 500000000000, 4);
INSERT INTO storage_plan_option (id, price, size, plan_id) VALUES (13, 432, 600000000000, 4);

/* Transactional Email Plan */
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (1, 0, 10, 1);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (2, 240, 1000, 2);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (3, 360, 1500, 2);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (4, 480, 2000, 2);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (5, 600, 2500, 2);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (6, 800, 5000, 3);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (7, 960, 6000, 3);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (8, 1120, 7000, 3);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (9, 1280, 8000, 3);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (10, 1600, 10000, 4);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (11, 1920, 12000, 4);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (12, 2240, 14000, 4);
INSERT INTO txe_plan_option (id, price, size, plan_id) VALUES (13, 2560, 16000, 4);


/* Email Campaign Plan */
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (1, 0, 100, 1);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (2, 80, 5000, 2);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (3, 160, 10000, 2);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (4, 240, 15000, 2);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (5, 320, 20000, 2);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (6, 560, 50000, 3);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (7, 840, 75000, 3);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (8, 1120, 100000, 3);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (9, 1400, 125000, 3);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (10, 2240, 200000, 4);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (11, 2800, 250000, 4);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (12, 3360, 300000, 4);
INSERT INTO ec_plan_option (id, price, size, plan_id) VALUES (13, 3920, 350000, 4);

INSERT INTO credits_plan_option (id, description, name, price, quantity, stripe_product_id, type) VALUES ('1', 'SMS credits at RM 0.10 per SMS', 'SMS - 1000 Credits', '100', '1000', 'prod_ODhfSrTzZ67FVl', 'SMS');
INSERT INTO credits_plan_option (id, description, name, price, quantity, stripe_product_id, type) VALUES ('2', 'SMS credits at RM 0.095 per SMS', 'SMS - 5000 Credits', '475', '5000', 'prod_ODhh78w9zDYNbh', 'SMS');
INSERT INTO credits_plan_option (id, description, name, price, quantity, stripe_product_id, type) VALUES ('3', 'SMS credits at RM 0.093 per SMS', 'SMS - 10000 Credits', '930', '10000', 'prod_ODhjUymwyDBzNT', 'SMS');

INSERT INTO account
(
    id,
    addr_line1,
    addr_line2,
    city,
    company_contact_no,
    company_name,
    country,
    country_iso_code,
    postcode,
    state,
    payment_exempted,
    created_by,
    created_date,
    last_modified_by,
    last_modified_date
) VALUES (
             1,
             'Address line 1',
             'Address line 2',
             'City',
             '60123456789',
             'Company Name',
             'Malaysia',
             'MY',
             '43300',
             'Selangor',
             false,
             'system',
             '2024-03-17 22:53:13.692952',
             'system',
             '2024-03-17 22:53:13.692952'
         );


INSERT INTO grabbill.user (id, email, name, password, role_id, active, verified, account_active, account_id, created_by, created_date, last_modified_by, last_modified_date) VALUES (1, 'owner@demo.com', 'owner', '$2a$10$p1UGdCBeQHyHDx7wS5WJtOrkG8JczBxN6/wsMTZ23XVcG.58jf0N.', 1, true, true, true, 1, 'system', '2022-04-09 20:26:32.226366', 'system', '2022-04-09 20:26:32.226366');
INSERT INTO grabbill.user (id, email, name, password, role_id, active, verified, account_active, account_id, created_by, created_date, last_modified_by, last_modified_date) VALUES (2, 'admin@demo.com', 'admin', '$2a$10$p1UGdCBeQHyHDx7wS5WJtOrkG8JczBxN6/wsMTZ23XVcG.58jf0N.', 2, true, true, true, 1, 'system', '2022-04-09 20:26:32.226366', 'system', '2022-04-09 20:26:32.226366');
INSERT INTO grabbill.user (id, email, name, password, role_id, active, verified, account_active, account_id, created_by, created_date, last_modified_by, last_modified_date) VALUES (3, 'manager@demo.com', 'manager', '$2a$10$p1UGdCBeQHyHDx7wS5WJtOrkG8JczBxN6/wsMTZ23XVcG.58jf0N.', 3, true, true, true, 1, 'system', '2022-04-09 20:26:32.226366', 'system', '2022-04-09 20:26:32.226366');
INSERT INTO grabbill.user (id, email, name, password, role_id, active, verified, account_active, account_id, created_by, created_date, last_modified_by, last_modified_date) VALUES (4, 'author@demo.com', 'author', '$2a$10$p1UGdCBeQHyHDx7wS5WJtOrkG8JczBxN6/wsMTZ23XVcG.58jf0N.', 4, true, true, true, 1, 'system', '2022-04-09 20:26:32.226366', 'system', '2022-04-09 20:26:32.226366');
INSERT INTO grabbill.user (id, email, name, password, role_id, active, verified, account_active, account_id, created_by, created_date, last_modified_by, last_modified_date) VALUES (5, 'viewer@demo.com', 'viewer', '$2a$10$p1UGdCBeQHyHDx7wS5WJtOrkG8JczBxN6/wsMTZ23XVcG.58jf0N.', 5, true, true, true, 1, 'system', '2022-04-09 20:26:32.226366', 'system', '2022-04-09 20:26:32.226366');


CREATE TABLE admin_user (
    id int(11) NOT NULL PRIMARY KEY,
    created_by varchar(255) NOT NULL,
    created_date datetime(6) DEFAULT NULL,
    last_modified_by varchar(255) NOT NULL,
    last_modified_date datetime(6) DEFAULT NULL,
    active bit(1) NOT NULL,
    email varchar(255) DEFAULT NULL,
    last_logged_in datetime(6) DEFAULT NULL,
    name varchar(255) DEFAULT NULL,
    password varchar(255) DEFAULT NULL
);

INSERT INTO grabbill.admin_user (id, created_by, created_date, last_modified_by, last_modified_date, active, email, name, password) VALUES (0, 'system', '2022-04-09 20:26:32.226366', 'system', '2022-04-09 20:26:32.226366', b'1', 'admin@grabbill.com', 'Admin', '$2a$10$p1UGdCBeQHyHDx7wS5WJtOrkG8JczBxN6/wsMTZ23XVcG.58jf0N.');

create table account_usage_stats
(
    id                 int          not null    primary key,
    created_by         varchar(255) not null,
    created_date       datetime(6)  null,
    last_modified_by   varchar(255) not null,
    last_modified_date datetime(6)  null,
    max_ec_sent        bigint       null,
    max_storage_size   bigint       null,
    max_txe_sent       bigint       null,
    total_ec_sent      bigint       null,
    total_storage_used bigint       null,
    total_txe_sent     bigint       null,
    account_id         int          not null,
    constraint UK_ACCT_USG_STATS_ACCOUNT_ID     unique (account_id),
    constraint FK_ACCT_USG_STATS_ACCOUNT_ID     foreign key (account_id) references account (id)
);

INSERT INTO grabbill.account_usage_stats (id, created_by, created_date, last_modified_by, last_modified_date, max_ec_sent, max_storage_size, max_txe_sent, total_ec_sent, total_storage_used, total_txe_sent, account_id) VALUES (1, 'system', '2022-08-13 23:54:50.0', 'system', '2022-08-13 23:54:58.0', null, null, null, 0, 0, 0, 1);
