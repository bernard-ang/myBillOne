set names utf8mb4;
set foreign_key_checks = 0;

create table `role` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;

create table `user` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `role_id` varchar(255) null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `user` add index `user_role_id_index`(`role_id`);

create table `client` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `email` varchar(255) not null, `contact_no` varchar(255) not null, `country` varchar(255) not null, `logo` varchar(255) not null, `company_name` varchar(255) not null, `company_contact_no` varchar(255) not null, `address_street1` varchar(255) not null, `address_street2` varchar(255) not null, `address_postcode` varchar(255) not null, `address_city` varchar(255) not null, `address_state` varchar(255) not null, `address_country` varchar(255) not null, `billing_address_street1` varchar(255) not null, `billing_address_street2` varchar(255) not null, `billing_address_postcode` varchar(255) not null, `billing_address_city` varchar(255) not null, `billing_address_state` varchar(255) not null, `billing_address_country` varchar(255) not null, `active` tinyint(1) not null comment 'client active?', `credit_balance` int not null comment 'SMS credit balance', `active_plan_name` varchar(255) not null, `active_plan_storage_size` int not null, `active_plan_contact_count` int not null, `active_plan_email_per_month` int not null, `active_plan_job_per_month` int not null, `active_plan_price` int not null, `active_plan_usage_name` varchar(255) not null, `active_plan_usage_storage_size` int not null, `active_plan_usage_contact_count` int not null, `active_plan_usage_email_per_month` int not null, `active_plan_usage_job_per_month` int not null, `active_plan_usage_price` int not null, `smtp_config_from_email` varchar(255) not null, `smtp_config_host` varchar(255) not null, `smtp_config_port` int not null, `smtp_config_username` varchar(255) not null, `smtp_config_password` varchar(255) not null, `smtp_config_auth` tinyint(1) not null, `smtp_config_starttls` tinyint(1) not null, `imap_config_host` varchar(255) not null, `imap_config_port` int not null, `imap_config_username` varchar(255) not null, `imap_config_password` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB comment = 'main client table';
alter table `client` add index `client_email_index`(`email`);
alter table `client` add index `client_active_plan_price_index`(`active_plan_price`);
alter table `client` add index `client_active_plan_usage_price_index`(`active_plan_usage_price`);

create table `client_log` (`id` varchar(255) not null, `created` datetime not null, `created_by` varchar(255) not null default '', `created_by_id` varchar(255) not null default '', `type` varchar(255) not null default '', `message` varchar(255) not null default '', `client_id` varchar(255) not null, `level` varchar(255) not null, `detail` text not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `client_log` add index `client_log_created_by_index`(`created_by`);
alter table `client_log` add index `client_log_created_by_id_index`(`created_by_id`);
alter table `client_log` add index `client_log_type_index`(`type`);
alter table `client_log` add index `client_log_message_index`(`message`);
alter table `client_log` add index `client_log_client_id_index`(`client_id`);
alter table `client_log` add index `client_log_level_index`(`level`);

create table `plan_history` (`id` varchar(255) not null, `created` datetime not null, `created_by` varchar(255) not null default '', `created_by_id` varchar(255) not null default '', `type` varchar(255) not null default '', `message` varchar(255) not null default '', `client_id` varchar(255) not null, `plan_name` varchar(255) not null, `plan_storage_size` int not null, `plan_contact_count` int not null, `plan_email_per_month` int not null, `plan_job_per_month` int not null, `plan_price` int not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `plan_history` add index `plan_history_created_by_index`(`created_by`);
alter table `plan_history` add index `plan_history_created_by_id_index`(`created_by_id`);
alter table `plan_history` add index `plan_history_type_index`(`type`);
alter table `plan_history` add index `plan_history_message_index`(`message`);
alter table `plan_history` add index `plan_history_client_id_index`(`client_id`);
alter table `plan_history` add index `plan_history_plan_price_index`(`plan_price`);

create table `plan_usage_history` (`id` varchar(255) not null, `created` datetime not null, `created_by` varchar(255) not null default '', `created_by_id` varchar(255) not null default '', `type` varchar(255) not null default '', `message` varchar(255) not null default '', `client_id` varchar(255) not null, `month_year` datetime not null, `active_plan_name` varchar(255) not null, `active_plan_storage_size` int not null, `active_plan_contact_count` int not null, `active_plan_email_per_month` int not null, `active_plan_job_per_month` int not null, `active_plan_price` int not null, `active_plan_usage_name` varchar(255) not null, `active_plan_usage_storage_size` int not null, `active_plan_usage_contact_count` int not null, `active_plan_usage_email_per_month` int not null, `active_plan_usage_job_per_month` int not null, `active_plan_usage_price` int not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `plan_usage_history` add index `plan_usage_history_created_by_index`(`created_by`);
alter table `plan_usage_history` add index `plan_usage_history_created_by_id_index`(`created_by_id`);
alter table `plan_usage_history` add index `plan_usage_history_type_index`(`type`);
alter table `plan_usage_history` add index `plan_usage_history_message_index`(`message`);
alter table `plan_usage_history` add index `plan_usage_history_client_id_index`(`client_id`);
alter table `plan_usage_history` add index `plan_usage_history_active_plan_price_index`(`active_plan_price`);
alter table `plan_usage_history` add index `plan_usage_history_active_plan_usage_price_index`(`active_plan_usage_price`);

create table `contact` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `email` varchar(255) not null, `mobile_no` varchar(255) not null, `text1` varchar(255) null, `number1` int null, `date1` datetime null, `text2` varchar(255) null, `number2` int null, `date2` datetime null, `text3` varchar(255) null, `number3` int null, `date3` datetime null, `text4` varchar(255) null, `number4` int null, `date4` datetime null, `text5` varchar(255) null, `number5` int null, `date5` datetime null, `text6` varchar(255) null, `number6` int null, `date6` datetime null, `text7` varchar(255) null, `number7` int null, `date7` datetime null, `text8` varchar(255) null, `number8` int null, `date8` datetime null, `text9` varchar(255) null, `number9` int null, `date9` datetime null, `text10` varchar(255) null, `number10` int null, `date10` datetime null, `client_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `contact` add index `contact_client_id_index`(`client_id`);

create table `contact_field` (`client_id` varchar(255) not null, `no` int unsigned not null, `name` varchar(255) not null, `label` varchar(255) not null, `required` tinyint(1) not null, `data_type` varchar(255) not null, primary key (`client_id`, `no`)) default character set utf8mb4 engine = InnoDB;
alter table `contact_field` add index `contact_field_client_id_index`(`client_id`);

create table `contact_group` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `description` text not null, `client_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `contact_group` add index `contact_group_client_id_index`(`client_id`);

create table `contact_groups` (`contact_id` varchar(255) not null, `contact_group_id` varchar(255) not null, primary key (`contact_id`, `contact_group_id`)) default character set utf8mb4 engine = InnoDB;
alter table `contact_groups` add index `contact_groups_contact_id_index`(`contact_id`);
alter table `contact_groups` add index `contact_groups_contact_group_id_index`(`contact_group_id`);

create table `df_type` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `code` varchar(255) not null, `index_file_name` varchar(255) not null, `index_file_type` varchar(255) not null, `column_separator` varchar(255) not null, `client_id` varchar(255) not null, `auto_purge` int not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `df_type` add index `df_type_client_id_index`(`client_id`);

create table `df_activity` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `schedule_at` datetime null, `process_status` enum('draft', 'submitted', 'processing', 'processed', 'error') not null, `process_priority` int not null, `process_retry_count` int not null, `process_message` varchar(255) not null, `process_draft_timestamp` datetime null, `process_submitted_timestamp` datetime null, `process_processing_timestamp` datetime null, `process_processed_timestamp` datetime null, `process_error_timestamp` datetime null, `type_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `df_activity` add index `df_activity_type_id_index`(`type_id`);

create table `df_file` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `file_size` int not null, `file_type` varchar(255) not null, `activity_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `df_file` add index `df_file_activity_id_index`(`activity_id`);

create table `df_record` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `process_status` enum('draft', 'submitted', 'processing', 'processed', 'error') not null, `process_priority` int not null, `process_retry_count` int not null, `process_message` varchar(255) not null, `process_draft_timestamp` datetime null, `process_submitted_timestamp` datetime null, `process_processing_timestamp` datetime null, `process_processed_timestamp` datetime null, `process_error_timestamp` datetime null, `activity_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `df_record` add index `df_record_activity_id_index`(`activity_id`);

create table `df_index_row` (`id` varchar(255) not null, `no` int unsigned not null, `activity_id` varchar(255) not null, `text1` varchar(255) null, `number1` int null, `date1` datetime null, `text2` varchar(255) null, `number2` int null, `date2` datetime null, `text3` varchar(255) null, `number3` int null, `date3` datetime null, `text4` varchar(255) null, `number4` int null, `date4` datetime null, `text5` varchar(255) null, `number5` int null, `date5` datetime null, `text6` varchar(255) null, `number6` int null, `date6` datetime null, `text7` varchar(255) null, `number7` int null, `date7` datetime null, `text8` varchar(255) null, `number8` int null, `date8` datetime null, `text9` varchar(255) null, `number9` int null, `date9` datetime null, `text10` varchar(255) null, `number10` int null, `date10` datetime null, `record_id` varchar(255) null, primary key (`id`, `no`, `activity_id`)) default character set utf8mb4 engine = InnoDB;
alter table `df_index_row` add index `df_index_row_activity_id_index`(`activity_id`);
alter table `df_index_row` add index `df_index_row_record_id_index`(`record_id`);
alter table `df_index_row` add unique `df_index_row_record_id_unique`(`record_id`);
alter table `df_index_row` add unique `df_index_row_activity_id_no_unique`(`activity_id`, `no`);

create table `df_index_column` (`no` int unsigned not null, `type_id` varchar(255) not null, `column_label` varchar(255) not null, `column_header` varchar(255) not null, `required` tinyint(1) not null, `data_type` enum('text', 'number', 'date', 'boolean') not null, primary key (`no`, `type_id`)) default character set utf8mb4 engine = InnoDB;
alter table `df_index_column` add index `df_index_column_type_id_index`(`type_id`);

create table `ec_type` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `code` varchar(255) not null, `auto_purge` int not null, `email_from` varchar(255) not null, `email_to` varchar(255) not null, `email_subject` varchar(255) not null, `email_contents` text not null, `email_attachment_filename` varchar(255) not null, `stats_updated` datetime not null, `stats_contact_count` int not null, `stats_email_sent_count` int not null, `stats_email_bounce_count` int not null, `client_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `ec_type` add index `ec_type_client_id_index`(`client_id`);

create table `ec_activity` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `schedule_at` datetime null, `process_status` enum('draft', 'submitted', 'processing', 'processed', 'error') not null, `process_priority` int not null, `process_retry_count` int not null, `process_message` varchar(255) not null, `process_draft_timestamp` datetime null, `process_submitted_timestamp` datetime null, `process_processing_timestamp` datetime null, `process_processed_timestamp` datetime null, `process_error_timestamp` datetime null, `email_from` varchar(255) not null, `email_to` varchar(255) not null, `email_subject` varchar(255) not null, `email_contents` text not null, `email_attachment_filename` varchar(255) not null, `stats_updated` datetime not null, `stats_contact_count` int not null, `stats_email_sent_count` int not null, `stats_email_bounce_count` int not null, `target_contact_group_id` varchar(255) null, `type_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `ec_activity` add index `ec_activity_target_contact_group_id_index`(`target_contact_group_id`);
alter table `ec_activity` add index `ec_activity_type_id_index`(`type_id`);

create table `ec_record` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `process_status` enum('draft', 'submitted', 'processing', 'processed', 'error') not null, `process_priority` int not null, `process_retry_count` int not null, `process_message` varchar(255) not null, `process_draft_timestamp` datetime null, `process_submitted_timestamp` datetime null, `process_processing_timestamp` datetime null, `process_processed_timestamp` datetime null, `process_error_timestamp` datetime null, `activity_id` varchar(255) not null, `email_from` varchar(255) not null, `email_to` varchar(255) not null, `email_subject` varchar(255) not null, `email_contents` text not null, `email_attachment_filename` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `ec_record` add index `ec_record_activity_id_index`(`activity_id`);

create table `txe_type` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `code` varchar(255) not null, `index_file_name` varchar(255) not null, `index_file_type` varchar(255) not null, `column_separator` varchar(255) not null, `email_from` varchar(255) not null, `email_to` varchar(255) not null, `email_subject` varchar(255) not null, `email_contents` text not null, `email_attachment_filename` varchar(255) not null, `auto_purge` int not null, `send_sms` tinyint(1) not null, `is_password_protect` tinyint(1) not null, `has_attachment` tinyint(1) not null, `archive` tinyint(1) not null, `sms_content` varchar(255) not null, `client_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `txe_type` add index `txe_type_client_id_index`(`client_id`);

create table `txe_activity` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `schedule_at` datetime null, `process_status` enum('draft', 'submitted', 'processing', 'processed', 'error') not null, `process_priority` int not null, `process_retry_count` int not null, `process_message` varchar(255) not null, `process_draft_timestamp` datetime null, `process_submitted_timestamp` datetime null, `process_processing_timestamp` datetime null, `process_processed_timestamp` datetime null, `process_error_timestamp` datetime null, `email_from` varchar(255) not null, `email_to` varchar(255) not null, `email_subject` varchar(255) not null, `email_contents` text not null, `email_attachment_filename` varchar(255) not null, `sms_content` varchar(255) not null, `purged_timestamp` datetime not null, `type_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `txe_activity` add index `txe_activity_type_id_index`(`type_id`);

create table `txe_file` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `file_size` int not null, `file_type` varchar(255) not null, `activity_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `txe_file` add index `txe_file_activity_id_index`(`activity_id`);

create table `txe_record` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `process_status` enum('draft', 'submitted', 'processing', 'processed', 'error') not null, `process_priority` int not null, `process_retry_count` int not null, `process_message` varchar(255) not null, `process_draft_timestamp` datetime null, `process_submitted_timestamp` datetime null, `process_processing_timestamp` datetime null, `process_processed_timestamp` datetime null, `process_error_timestamp` datetime null, `activity_id` varchar(255) not null, `email_status_skip_unsubscribed` tinyint(1) not null, `email_status_skip_bounced` tinyint(1) not null, `email_status_send_response_soft_bounce` tinyint(1) not null, `email_status_send_response_hard_bounce` tinyint(1) not null, `email_status_user_read_timestamp` datetime not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `txe_record` add index `txe_record_activity_id_index`(`activity_id`);

create table `txe_index_row` (`id` varchar(255) not null, `no` int unsigned not null, `text1` varchar(255) null, `number1` int null, `date1` datetime null, `text2` varchar(255) null, `number2` int null, `date2` datetime null, `text3` varchar(255) null, `number3` int null, `date3` datetime null, `text4` varchar(255) null, `number4` int null, `date4` datetime null, `text5` varchar(255) null, `number5` int null, `date5` datetime null, `text6` varchar(255) null, `number6` int null, `date6` datetime null, `text7` varchar(255) null, `number7` int null, `date7` datetime null, `text8` varchar(255) null, `number8` int null, `date8` datetime null, `text9` varchar(255) null, `number9` int null, `date9` datetime null, `text10` varchar(255) null, `number10` int null, `date10` datetime null, `activity_id` varchar(255) not null, `record_id` varchar(255) not null, primary key (`id`, `no`)) default character set utf8mb4 engine = InnoDB;
alter table `txe_index_row` add index `txe_index_row_activity_id_index`(`activity_id`);
alter table `txe_index_row` add index `txe_index_row_record_id_index`(`record_id`);
alter table `txe_index_row` add unique `txe_index_row_record_id_unique`(`record_id`);
alter table `txe_index_row` add unique `txe_index_row_activity_id_no_unique`(`activity_id`, `no`);

create table `txe_index_column` (`no` int unsigned not null, `type_id` varchar(255) not null, `column_label` varchar(255) not null, `column_header` varchar(255) not null, `required` tinyint(1) not null, `data_type` enum('text', 'number', 'date', 'boolean') not null, primary key (`no`, `type_id`)) default character set utf8mb4 engine = InnoDB;
alter table `txe_index_column` add index `txe_index_column_type_id_index`(`type_id`);

create table `credit_history` (`id` varchar(255) not null, `created` datetime not null, `created_by` json not null, `created_by_id` json not null, `client_id` varchar(255) not null, `type` varchar(255) not null, `amount` int not null, `message` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB comment = 'Credit usage history (e.g. for SMS usage)';
alter table `credit_history` add index `credit_history_created_by_index`(`created_by`);
alter table `credit_history` add index `credit_history_created_by_id_index`(`created_by_id`);
alter table `credit_history` add index `credit_history_client_id_index`(`client_id`);

create table `credit_card` (`id` varchar(255) not null, `created` datetime not null, `updated` datetime not null, `created_by` varchar(255) not null, `created_by_id` varchar(255) not null, `updated_by` varchar(255) not null, `updated_by_id` varchar(255) not null, `name` varchar(255) not null, `client_id` varchar(255) not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB;
alter table `credit_card` add index `credit_card_client_id_index`(`client_id`);

create table `billing_history` (`id` varchar(255) not null, `created` datetime not null, `created_by` varchar(255) not null default '', `created_by_id` varchar(255) not null default '', `type` varchar(255) not null default '', `message` varchar(255) not null default '', `client_id` varchar(255) not null, `status` varchar(255) not null, `bill_no` varchar(255) not null, `address_street1` varchar(255) not null, `address_street2` varchar(255) not null, `address_postcode` varchar(255) not null, `address_city` varchar(255) not null, `address_state` varchar(255) not null, `address_country` varchar(255) not null, `amount` numeric(10,2) not null, `tax` numeric(10,2) not null, `total` numeric(10,2) not null, `detail` text not null, primary key (`id`)) default character set utf8mb4 engine = InnoDB comment = 'client billing history';
alter table `billing_history` add index `billing_history_created_by_index`(`created_by`);
alter table `billing_history` add index `billing_history_created_by_id_index`(`created_by_id`);
alter table `billing_history` add index `billing_history_type_index`(`type`);
alter table `billing_history` add index `billing_history_message_index`(`message`);
alter table `billing_history` add index `billing_history_client_id_index`(`client_id`);

alter table `user` add constraint `user_role_id_foreign` foreign key (`role_id`) references `role` (`id`) on update cascade on delete set null;

alter table `client_log` add constraint `client_log_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `plan_history` add constraint `plan_history_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `plan_usage_history` add constraint `plan_usage_history_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `contact` add constraint `contact_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `contact_field` add constraint `contact_field_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `contact_group` add constraint `contact_group_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `contact_groups` add constraint `contact_groups_contact_id_foreign` foreign key (`contact_id`) references `contact` (`id`) on update cascade on delete cascade;
alter table `contact_groups` add constraint `contact_groups_contact_group_id_foreign` foreign key (`contact_group_id`) references `contact_group` (`id`) on update cascade on delete cascade;

alter table `df_type` add constraint `df_type_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `df_activity` add constraint `df_activity_type_id_foreign` foreign key (`type_id`) references `df_type` (`id`) on update cascade on delete cascade;

alter table `df_file` add constraint `df_file_activity_id_foreign` foreign key (`activity_id`) references `df_activity` (`id`) on update cascade on delete cascade;

alter table `df_record` add constraint `df_record_activity_id_foreign` foreign key (`activity_id`) references `df_activity` (`id`) on update cascade;

alter table `df_index_row` add constraint `df_index_row_activity_id_foreign` foreign key (`activity_id`) references `df_activity` (`id`) on update cascade on delete cascade;
alter table `df_index_row` add constraint `df_index_row_record_id_foreign` foreign key (`record_id`) references `df_record` (`id`) on update cascade on delete cascade;

alter table `df_index_column` add constraint `df_index_column_type_id_foreign` foreign key (`type_id`) references `df_type` (`id`) on update cascade on delete cascade;

alter table `ec_type` add constraint `ec_type_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `ec_activity` add constraint `ec_activity_target_contact_group_id_foreign` foreign key (`target_contact_group_id`) references `contact_group` (`id`) on update cascade on delete set null;
alter table `ec_activity` add constraint `ec_activity_type_id_foreign` foreign key (`type_id`) references `ec_type` (`id`) on update cascade on delete cascade;

alter table `ec_record` add constraint `ec_record_activity_id_foreign` foreign key (`activity_id`) references `ec_activity` (`id`) on update cascade;

alter table `txe_type` add constraint `txe_type_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `txe_activity` add constraint `txe_activity_type_id_foreign` foreign key (`type_id`) references `txe_type` (`id`) on update cascade on delete cascade;

alter table `txe_file` add constraint `txe_file_activity_id_foreign` foreign key (`activity_id`) references `txe_activity` (`id`) on update cascade on delete cascade;

alter table `txe_record` add constraint `txe_record_activity_id_foreign` foreign key (`activity_id`) references `txe_activity` (`id`) on update cascade;

alter table `txe_index_row` add constraint `txe_index_row_activity_id_foreign` foreign key (`activity_id`) references `txe_activity` (`id`) on update cascade on delete cascade;
alter table `txe_index_row` add constraint `txe_index_row_record_id_foreign` foreign key (`record_id`) references `txe_record` (`id`) on update cascade on delete cascade;

alter table `txe_index_column` add constraint `txe_index_column_type_id_foreign` foreign key (`type_id`) references `txe_type` (`id`) on update cascade on delete cascade;

alter table `credit_history` add constraint `credit_history_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `credit_card` add constraint `credit_card_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

alter table `billing_history` add constraint `billing_history_client_id_foreign` foreign key (`client_id`) references `client` (`id`) on update cascade on delete cascade;

set foreign_key_checks = 1;
