CREATE TABLE tournaments (
    id          UUID PRIMARY KEY,
    shop_id     UUID NOT NULL REFERENCES shops(id),
    name        VARCHAR(200) NOT NULL,
    format      VARCHAR(30) NOT NULL,   -- SINGLE_ELIMINATION | DOUBLE_ELIMINATION
    event_date  DATE,
    prize       TEXT,
    note        TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT',  -- DRAFT | SETUP | IN_PROGRESS | COMPLETED
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_tournaments_shop_id ON tournaments(shop_id);

CREATE TABLE tournament_participants (
    id             UUID PRIMARY KEY,
    shop_id        UUID NOT NULL REFERENCES shops(id),
    tournament_id  UUID NOT NULL REFERENCES tournaments(id),
    display_name   VARCHAR(100) NOT NULL,
    display_order  INT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_tournament_participants_tournament_id ON tournament_participants(tournament_id);

CREATE TABLE tournament_matches (
    id                      UUID PRIMARY KEY,
    shop_id                 UUID NOT NULL REFERENCES shops(id),
    tournament_id           UUID NOT NULL REFERENCES tournaments(id),
    bracket                 VARCHAR(20) NOT NULL,   -- WINNER | LOSER | GRAND_FINAL
    round                   INT NOT NULL,
    match_index             INT NOT NULL,
    participant1_id         UUID REFERENCES tournament_participants(id),
    participant2_id         UUID REFERENCES tournament_participants(id),
    winner_id               UUID REFERENCES tournament_participants(id),
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING | READY | COMPLETED
    next_match_id           UUID,
    next_match_slot         INT,
    loser_next_match_id     UUID,
    loser_next_match_slot   INT,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_tournament_matches_tournament_id ON tournament_matches(tournament_id);
CREATE INDEX idx_tournament_matches_tournament_bracket_round ON tournament_matches(tournament_id, bracket, round);
