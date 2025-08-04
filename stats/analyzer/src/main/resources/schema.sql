-- Таблица действий пользователей
CREATE TABLE IF NOT EXISTS actions (
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    timestamp BIGINT NOT NULL,
    PRIMARY KEY (user_id, event_id, action_type)
);

-- Таблица схожести событий
CREATE TABLE IF NOT EXISTS similarities (
    event_id BIGINT NOT NULL,
    other_event_id BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (event_id, other_event_id)
);