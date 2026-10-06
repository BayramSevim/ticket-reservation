CREATE TABLE users (
                       id            BIGSERIAL    PRIMARY KEY,
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
                       created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE seats (
                       id         BIGSERIAL     PRIMARY KEY,
                       show_id    BIGINT        NOT NULL REFERENCES shows(id),
                       label      VARCHAR(10)   NOT NULL,
                       price      NUMERIC(10,2) NOT NULL,
                       created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
                       CONSTRAINT uq_seat_per_show       UNIQUE (show_id, label),
                       CONSTRAINT chk_price_non_negative CHECK (price >= 0)
);

CREATE TABLE reservations (
                              id           BIGSERIAL   PRIMARY KEY,
                              seat_id      BIGINT      NOT NULL REFERENCES seats(id),
                              user_id      BIGINT      NOT NULL REFERENCES users(id),
                              status       VARCHAR(20) NOT NULL,
                              expires_at   TIMESTAMPTZ NOT NULL,
                              confirmed_at TIMESTAMPTZ,
                              cancelled_at TIMESTAMPTZ,
                              created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                              CONSTRAINT chk_reservation_status
                                  CHECK (status IN ('HELD', 'CONFIRMED', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_reservations_seat_id ON reservations(seat_id);
CREATE INDEX idx_reservations_user_id ON reservations(user_id);