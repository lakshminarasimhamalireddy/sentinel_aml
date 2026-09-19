# Sentinel AML ERD

```text
customer (id, customer_reference, full_name, risk_rating)
   1 └──── * account (id, customer_id, account_number, account_type, currency)
                  1 └──── * aml_transactions (id, account_id, amount_in_inr, transaction_time)

customer 1 ──── * aml_alerts (id, customer_id, account_id, rule_name, risk_score, status)
account  1 ──── * aml_alerts

rule_config (rule_key, enabled, value, risk_weight)
```

The versioned PostgreSQL schema is defined in `src/main/resources/db/migration/V1__create_sentinel_aml_schema.sql`.
