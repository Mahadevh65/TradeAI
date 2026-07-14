-- ============================================================
-- TradeMind AI - Module 1: Auth & User Management Schema
-- PostgreSQL (Supabase) - UUID primary keys, audit fields
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- for gen_random_uuid()

-- ------------------------------------------------------------
-- ROLES
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(30) NOT NULL UNIQUE,   -- ADMIN, TRADER, ANALYST
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'Full system access'),
    ('TRADER', 'Can trade, manage portfolio and watchlists'),
    ('ANALYST', 'Read-only analytics and research access')
ON CONFLICT (name) DO NOTHING;

-- ------------------------------------------------------------
-- USERS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name             VARCHAR(150) NOT NULL,
    email                 VARCHAR(180) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    phone_number          VARCHAR(20),
    is_email_verified     BOOLEAN NOT NULL DEFAULT FALSE,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    is_locked             BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- ------------------------------------------------------------
-- USER_ROLES (many-to-many)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id     UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);

-- ------------------------------------------------------------
-- EMAIL VERIFICATION TOKENS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_evt_token ON email_verification_tokens (token);

-- ------------------------------------------------------------
-- PASSWORD RESET TOKENS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_prt_token ON password_reset_tokens (token);

-- ------------------------------------------------------------
-- REFRESH TOKENS (for JWT refresh flow, supports multi-device)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    device_info VARCHAR(255),
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_rt_user ON refresh_tokens (user_id);

-- ------------------------------------------------------------
-- AUDIT LOG (used across modules, starting with auth events)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES users (id) ON DELETE SET NULL,
    action      VARCHAR(100) NOT NULL,     -- LOGIN_SUCCESS, LOGIN_FAILED, REGISTER, etc.
    details     TEXT,
    ip_address  VARCHAR(64),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs (action);

-- ============================================================
-- Modules 2-8 (appended)
-- ============================================================
-- ============================================================

-- ------------------------------------------------------------
-- STOCKS (reference/cache table - populated lazily from market data API)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS stocks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol          VARCHAR(20) NOT NULL UNIQUE,
    company_name    VARCHAR(200) NOT NULL,
    exchange        VARCHAR(20),
    sector          VARCHAR(100),
    industry        VARCHAR(100),
    currency        VARCHAR(10) DEFAULT 'USD',
    logo_url        VARCHAR(255),
    last_price      NUMERIC(18,4),
    day_change_pct  NUMERIC(8,4),
    market_cap      NUMERIC(20,2),
    pe_ratio        NUMERIC(10,2),
    eps             NUMERIC(10,2),
    week52_high     NUMERIC(18,4),
    week52_low      NUMERIC(18,4),
    dividend_yield  NUMERIC(8,4),
    volume          BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_stocks_symbol ON stocks (symbol);
CREATE INDEX IF NOT EXISTS idx_stocks_sector ON stocks (sector);

-- ------------------------------------------------------------
-- PORTFOLIO: HOLDINGS + TRADES
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS holdings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    stock_id        UUID NOT NULL REFERENCES stocks (id),
    quantity        NUMERIC(18,4) NOT NULL DEFAULT 0,
    average_price   NUMERIC(18,4) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, stock_id)
);
CREATE INDEX IF NOT EXISTS idx_holdings_user ON holdings (user_id);

CREATE TABLE IF NOT EXISTS trades (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    stock_id        UUID NOT NULL REFERENCES stocks (id),
    trade_type      VARCHAR(10) NOT NULL CHECK (trade_type IN ('BUY','SELL')),
    quantity        NUMERIC(18,4) NOT NULL CHECK (quantity > 0),
    price           NUMERIC(18,4) NOT NULL CHECK (price >= 0),
    total_amount    NUMERIC(20,4) NOT NULL,
    realized_pl     NUMERIC(20,4),                 -- populated on SELL trades
    executed_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_trades_user ON trades (user_id);
CREATE INDEX IF NOT EXISTS idx_trades_user_executed ON trades (user_id, executed_at DESC);

-- Daily snapshot of total portfolio value, used for the performance line chart / history
CREATE TABLE IF NOT EXISTS portfolio_snapshots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    snapshot_date   DATE NOT NULL,
    total_value     NUMERIC(20,4) NOT NULL,
    total_invested  NUMERIC(20,4) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, snapshot_date)
);
CREATE INDEX IF NOT EXISTS idx_snapshots_user_date ON portfolio_snapshots (user_id, snapshot_date);

-- ------------------------------------------------------------
-- WATCHLISTS
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS watchlists (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, name)
);

CREATE TABLE IF NOT EXISTS watchlist_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    watchlist_id    UUID NOT NULL REFERENCES watchlists (id) ON DELETE CASCADE,
    stock_id        UUID NOT NULL REFERENCES stocks (id),
    sort_order      INT NOT NULL DEFAULT 0,
    added_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (watchlist_id, stock_id)
);
CREATE INDEX IF NOT EXISTS idx_watchlist_items_watchlist ON watchlist_items (watchlist_id);

CREATE TABLE IF NOT EXISTS price_alerts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    stock_id        UUID NOT NULL REFERENCES stocks (id),
    condition       VARCHAR(10) NOT NULL CHECK (condition IN ('ABOVE','BELOW')),
    target_price    NUMERIC(18,4) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    triggered_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_price_alerts_user ON price_alerts (user_id);
CREATE INDEX IF NOT EXISTS idx_price_alerts_active ON price_alerts (is_active);

-- ------------------------------------------------------------
-- AI COPILOT QUERY LOG
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ai_query_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    question        TEXT NOT NULL,
    answer_summary  TEXT,
    recommendation  VARCHAR(20),      -- BUY, SELL, HOLD, WATCH
    confidence_score NUMERIC(5,2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_ai_logs_user ON ai_query_logs (user_id);

-- ------------------------------------------------------------
-- NEWS CACHE
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS news_articles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(500) NOT NULL,
    source          VARCHAR(150),
    url             VARCHAR(500) NOT NULL UNIQUE,
    image_url       VARCHAR(500),
    category        VARCHAR(50),
    sentiment       VARCHAR(20) CHECK (sentiment IN ('BULLISH','BEARISH','NEUTRAL')),
    ai_summary      TEXT,
    published_at    TIMESTAMPTZ,
    fetched_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_news_published ON news_articles (published_at DESC);
CREATE INDEX IF NOT EXISTS idx_news_category ON news_articles (category);

-- ------------------------------------------------------------
-- ADMIN: MARKET CONFIGURATION
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS market_config (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    market_name         VARCHAR(50) NOT NULL UNIQUE,   -- e.g. NSE, NASDAQ
    is_open             BOOLEAN NOT NULL DEFAULT FALSE,
    open_time           TIME,
    close_time          TIME,
    timezone            VARCHAR(50) DEFAULT 'UTC',
    updated_by          VARCHAR(100),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO market_config (market_name, is_open, open_time, close_time, timezone) VALUES
    ('NASDAQ', FALSE, '09:30', '16:00', 'America/New_York'),
    ('NSE', FALSE, '09:15', '15:30', 'Asia/Kolkata')
ON CONFLICT (market_name) DO NOTHING;
