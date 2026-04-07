CREATE TABLE IF NOT EXISTS user_actions (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    event_id      BIGINT NOT NULL,
    action_type   VARCHAR(20) NOT NULL,
    weight        DOUBLE PRECISION NOT NULL,
    event_timestamp TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_event UNIQUE (user_id, event_id)
);

CREATE TABLE IF NOT EXISTS event_similarity (
    id         BIGSERIAL PRIMARY KEY,
    event_a    BIGINT NOT NULL,
    event_b    BIGINT NOT NULL,
    score      DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_event_pair UNIQUE (event_a, event_b)
);

CREATE INDEX IF NOT EXISTS idx_user_actions_user_id  ON user_actions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_actions_event_id ON user_actions(event_id);
CREATE INDEX IF NOT EXISTS idx_similarity_event_a    ON event_similarity(event_a);
CREATE INDEX IF NOT EXISTS idx_similarity_event_b    ON event_similarity(event_b);
