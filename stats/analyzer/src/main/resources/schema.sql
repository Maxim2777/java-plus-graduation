-- Таблица действий пользователей
CREATE TABLE IF NOT EXISTS actions (
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (user_id, event_id)
);

-- Таблица схожести событий
CREATE TABLE IF NOT EXISTS similarities (
    event_a BIGINT NOT NULL,
    event_b BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (event_a, event_b)
);