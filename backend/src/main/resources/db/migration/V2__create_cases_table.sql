CREATE TABLE cases (
                       id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Every case belongs to a lawyer
                       lawyer_id       UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,

                       case_title      VARCHAR(200)    NOT NULL,

    -- e.g. CIVIL, CRIMINAL, FAMILY, PROPERTY
                       case_type       VARCHAR(50)     NOT NULL,

    -- e.g. ACTIVE, CLOSED, ON_HOLD
                       case_status     VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                           CHECK (case_status IN ('ACTIVE', 'CLOSED', 'ON_HOLD')),

                       court_name      VARCHAR(200),
                       case_number     VARCHAR(100),

                       created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                       updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Lawyer's case dashboard query — most frequent read
CREATE INDEX idx_cases_lawyer_id ON cases(lawyer_id);

-- Filter by status — active cases tab
CREATE INDEX idx_cases_status ON cases(case_status);