CREATE TABLE hearings (
                          id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

                          case_id         UUID            NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
                          lawyer_id       UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- When this hearing took place
                          hearing_date    DATE            NOT NULL,

    -- What happened — AI extracts this from raw note
                          outcome         TEXT,

    -- Next scheduled court date — AI extracts this
                          next_date       DATE,

    -- JSON array of action items: ["Submit evidence", "Call witness"]
    -- Stored as TEXT, parsed in service layer
                          action_items    TEXT,

    -- Raw note the lawyer typed before AI parsed it — keep for audit trail
                          raw_note        TEXT,

                          created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                          updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Upcoming hearings query — sorted by next_date ascending
CREATE INDEX idx_hearings_next_date ON hearings(next_date);
CREATE INDEX idx_hearings_case_id ON hearings(case_id);
CREATE INDEX idx_hearings_lawyer_id ON hearings(lawyer_id);