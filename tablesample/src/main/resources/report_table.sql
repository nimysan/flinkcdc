create TABLE spend_report (
    account_id BIGINT,
    log_ts     TIMESTAMP(3),
    amount     BIGINT
,    PRIMARY KEY (account_id, log_ts) NOT ENFORCED) with (
   'connector'  = 'jdbc',
   'url'        = 'jdbc:mysql://localhost:3307/sql-demo',
   'table-name' = 'spend_report',
   'driver'     = 'com.mysql.jdbc.Driver',
   'username'   = 'sql-demo',
   'password'   = 'demo-sql'
)