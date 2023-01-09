CREATE TABLE transactions (
    account_id  BIGINT,
    amount      BIGINT,
    transaction_time TIMESTAMP(3),
    WATERMARK FOR transaction_time AS transaction_time - INTERVAL '5' SECOND
) WITH (
    'connector' = 'kafka',
    'topic'     = 'transactions',
    'properties.bootstrap.servers' = 'kafka:9092',
    'format'    = 'csv'
)