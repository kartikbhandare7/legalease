-- Extension for UUID generation at DB level (backup if JPA doesn't generate)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
                       id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                       full_name           VARCHAR(100)    NOT NULL,
                       email               VARCHAR(150)    NOT NULL UNIQUE,

    -- Nullable: Google OAuth users have no password
                       password            VARCHAR(255),

    -- Enum stored as string for readability in DB
                       role                VARCHAR(20)     NOT NULL
                           CHECK (role IN ('ROLE_LAWYER', 'ROLE_CLERK', 'ROLE_ADMIN')),

                       auth_provider       VARCHAR(10)     NOT NULL
                           CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),

    -- Google's provider ID for OAuth account linking
                       provider_id         VARCHAR(100),

    -- Lawyers only — stored for admin verification
                       bar_council_number  VARCHAR(50),

    -- Lawyers only — path to uploaded enrollment certificate
                       certificate_path    VARCHAR(500),

                       account_status      VARCHAR(10)     NOT NULL DEFAULT 'PENDING'
                           CHECK (account_status IN ('PENDING', 'ACTIVE', 'REJECTED')),

    -- Shown on lawyer dashboard — clerks use this to register under a lawyer
                       referral_code       VARCHAR(20)     UNIQUE,

    -- For CLERK — UUID of the lawyer who invited them
                       invited_by          UUID            REFERENCES users(id) ON DELETE SET NULL,

                       created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
                       updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Fast lookup by email on every login request
CREATE INDEX idx_users_email ON users(email);

-- Fast lookup by referral code on clerk registration
CREATE INDEX idx_users_referral_code ON users(referral_code);

-- Admin dashboard filters by status — PENDING list loads fast
CREATE INDEX idx_users_account_status ON users(account_status);