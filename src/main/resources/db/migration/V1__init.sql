CREATE TABLE shows (
                       id             BIGSERIAL    PRIMARY KEY,
                       title          VARCHAR(200) NOT NULL,
                       location       VARCHAR(200) NOT NULL,
                       starts_at      TIMESTAMPTZ  NOT NULL,
                       sale_starts_at TIMESTAMPTZ  NOT NULL,
                       sale_ends_at   TIMESTAMPTZ  NOT NULL,
                       created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       CONSTRAINT chk_sale_window      CHECK (sale_starts_at < sale_ends_at),
                       CONSTRAINT chk_sale_before_show CHECK (sale_ends_at <= starts_at)
);