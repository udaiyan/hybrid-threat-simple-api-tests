CREATE TABLE IF NOT EXISTS threats (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(32)  NOT NULL,
    severity    VARCHAR(32)  NOT NULL,
    source      VARCHAR(255) NOT NULL,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status      VARCHAR(32)  NOT NULL
);
