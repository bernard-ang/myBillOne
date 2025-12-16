# db index changes

This file is used to track db index changes.

## Indexes
```

create index whatsapp_event_processed_processed_count_index
    on whatsapp_event (processed, processed_count);

create index mt_wa_record_wa_message_id_index
    on mt_wa_record (wa_message_id);

create index whatsapp_event_wa_message_id_index
    on whatsapp_event (wa_message_id);

```


## Useful Commands

```shell

mysql -u grabbill.root -p

# once logged in, check queries with:

SHOW PROCESSLIST;

```

## Common Queries
```sql

SELECT COUNT(*) FROM whatsapp_event;
SELECT COUNT(*) FROM sms_type;
SELECT COUNT(*) FROM mt_wa_record;
SELECT COUNT(*) FROM job;


SELECT * FROM mt_wa_activity WHERE id = 999;
SELECT * FROM mt_wa_record WHERE mt_wa_activity_id = 999;

SELECT * FROM mt_wa_record WHERE wa_message_id = 'wamid.HBgLNjAxOTMwMTU0ODMVAgARGBI3MEVFQTNCOUE5OUNBRjFFOTcA';

SELECT * FROM whatsapp_event WHERE wa_message_id = 'wamid.HBgLNjAxOTMwMTU0ODMVAgARGBI3MEVFQTNCOUE5OUNBRjFFOTcA';

SELECT * FROM job WHERE id = 3245;

SELECT * FROM job WHERE activity_name LIKE '%2025-08-19%' AND status = 'FAILED';
```


## Common Errors

```
Could not open JPA EntityManager for transaction; nested exception is org.hibernate.exception.JDBCConnectionException: Unable to acquire JDBC Connection
```
