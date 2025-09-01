-- Users
CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE,
    login        VARCHAR(100) NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    birthday     DATE NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);

-- Friendships
CREATE TABLE friendships (
    id             BIGSERIAL PRIMARY KEY,
    requester_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    addressee_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status         VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','CONFIRMED')),
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    confirmed_at   TIMESTAMP NULL,
    CONSTRAINT uq_friend UNIQUE (requester_id, addressee_id),
    CONSTRAINT chk_not_self_friend CHECK (requester_id <> addressee_id)
);

-- MPA ratings
CREATE TABLE mpa_ratings (
    id    SMALLINT PRIMARY KEY,
    code  VARCHAR(10)  NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL
);

-- Films
CREATE TABLE films (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255)  NOT NULL,
    description   VARCHAR(2000) NOT NULL,
    release_date  DATE NOT NULL,
    duration_min  INTEGER NOT NULL CHECK (duration_min > 0),
    mpa_id        SMALLINT NOT NULL REFERENCES mpa_ratings(id),
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

-- Genres
CREATE TABLE genres (
    id   SMALLINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Film ↔ Genre (M:N)
CREATE TABLE film_genres (
    film_id  BIGINT   NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    genre_id SMALLINT NOT NULL REFERENCES genres(id),
    PRIMARY KEY (film_id, genre_id)
);

-- Film likes
CREATE TABLE film_likes (
    film_id  BIGINT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    user_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    liked_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (film_id, user_id)
);
