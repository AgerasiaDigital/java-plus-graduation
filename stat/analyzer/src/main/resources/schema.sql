CREATE TABLE IF NOT EXISTS user_event_weights (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    max_weight DOUBLE PRECISION NOT NULL,
    last_interaction_ts BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_user_event UNIQUE (user_id, event_id)
);

CREATE TABLE IF NOT EXISTS event_similarities (
    id BIGSERIAL PRIMARY KEY,
    event_a BIGINT NOT NULL,
    event_b BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    CONSTRAINT uq_event_similar UNIQUE (event_a, event_b)
);
