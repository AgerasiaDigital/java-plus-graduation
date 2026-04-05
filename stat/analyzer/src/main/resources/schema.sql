CREATE TABLE IF NOT EXISTS user_event_interaction (
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    max_weight DOUBLE PRECISION NOT NULL,
    last_interaction_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, event_id)
);

CREATE TABLE IF NOT EXISTS event_similarity (
    event_a BIGINT NOT NULL,
    event_b BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (event_a, event_b),
    CONSTRAINT event_pair_ordered CHECK (event_a < event_b)
);

CREATE INDEX IF NOT EXISTS idx_similarity_event_a ON event_similarity (event_a);
CREATE INDEX IF NOT EXISTS idx_similarity_event_b ON event_similarity (event_b);
CREATE INDEX IF NOT EXISTS idx_interaction_user_time ON user_event_interaction (user_id, last_interaction_at DESC);
