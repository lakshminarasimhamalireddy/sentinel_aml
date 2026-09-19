# Sentinel AML backend

Spring Boot API for real-time transaction ingestion, explainable AML rule detection, risk-ranked alerts, and analyst dispositions.

## Run locally

```powershell
cd backend
mvn spring-boot:run
```

The default development database is in-memory H2. Open Swagger at `http://localhost:8080/swagger-ui.html`; the H2 console is at `http://localhost:8080/h2-console`.

For PostgreSQL, create `sentinel_aml` and run with:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/sentinel_aml"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-password"
$env:DB_DRIVER="org.postgresql.Driver"
mvn spring-boot:run
```

## Demo sequence

The startup seed provides `CUST-DEMO-001` and `ACC-1001`. Create the customer/account using the API if you need their numeric IDs, then submit three INR transactions of `800000`, `805000`, and `810000` for the same account within 24 hours. The third one produces a `STRUCTURING` alert.

Configured rules are visible/editable at `GET`/`PUT /api/v1/admin/rules/{key}`. The current MVP implements large-transaction, structuring, and high-risk-jurisdiction rules. Rule configurations are stored in the database, so tuning does not require a deployment.
