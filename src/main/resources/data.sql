-- Seed data for the hybrid-threat demo API.
-- Runs on every startup (spring.sql.init.mode=always), and before every test
-- (via @Sql on BaseApiTest). TRUNCATE keeps it idempotent.
--
-- For a production system: remove TRUNCATE and migrate to Flyway or Liquibase.
TRUNCATE TABLE threats RESTART IDENTITY CASCADE;

INSERT INTO threats (name, type, severity, source, detected_at, status) VALUES
('Operation Cobalt Drift',             'CYBER',    'CRITICAL', 'SIGINT', '2026-01-15T08:30:00Z', 'ACTIVE'),
('Port facility reconnaissance',        'PHYSICAL', 'HIGH',     'HUMINT', '2026-02-02T14:12:00Z', 'OPEN'),
('Combined cyber-kinetic exercise',     'HYBRID',   'CRITICAL', 'OSINT',  '2026-02-20T06:45:00Z', 'MITIGATED'),
('Phishing campaign against MOD staff', 'CYBER',    'MEDIUM',   'SOC',    '2026-03-05T11:20:00Z', 'OPEN'),
('Suspicious drone activity near site', 'PHYSICAL', 'LOW',      'FIELD',  '2026-03-12T19:05:00Z', 'CLOSED'),
('Coordinated disinformation + DDoS',   'HYBRID',   'HIGH',     'OSINT',  '2026-03-18T09:00:00Z', 'ACTIVE');
