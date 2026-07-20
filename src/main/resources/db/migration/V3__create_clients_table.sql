CREATE TABLE clients (
                         id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Client belongs to a specific case
                         case_id             UUID            NOT NULL REFERENCES cases(id) ON DELETE CASCADE,

    -- Also linked directly to lawyer for fast dashboard queries
                         lawyer_id           UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,

                         client_name         VARCHAR(100)    NOT NULL,
                         phone               VARCHAR(20),
                         email               VARCHAR(150),

    -- Opposing party name — extracted by AI from intake form
                         opposing_party      VARCHAR(100),

    -- Raw background story — AI fills this from lawyer's rough note
                         case_background     TEXT,

                         created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
                         updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_clients_case_id ON clients(case_id);
CREATE INDEX idx_clients_lawyer_id ON clients(lawyer_id);