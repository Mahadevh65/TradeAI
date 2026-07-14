# TradeMind AI — Complete Build (Modules 1–8)

An enterprise-style trading dashboard combining portfolio management, watchlists,
stock research, an AI copilot, market news, analytics, and an admin panel.

## Stack

**Backend:** Java 21, Spring Boot 3.3, Spring Security + JWT, Spring Data JPA/Hibernate, Maven
**Frontend:** React 18, Vite, Tailwind CSS, Framer Motion, React Query, Recharts, React Icons
**Database:** PostgreSQL (Supabase)

## Modules

| # | Module | What's included |
|---|--------|------------------|
| 1 | Auth | Register/login/JWT/refresh tokens, email verification, forgot/reset password, roles (ADMIN/TRADER/ANALYST), account lockout, audit logging |
| 2 | Portfolio | Buy/sell with weighted-average cost basis, holdings, realized/unrealized P&L, ROI, trade history, daily value snapshots + performance chart |
| 3 | Watchlist | Multiple watchlists, add/remove/sort/filter items, price alerts (email-notified, checked every 5 min) |
| 4 | Stock Details | Company profile, financial ratios (P/E, EPS, market cap, 52w high/low, dividend yield), historical OHLC chart, search, top gainers/losers |
| 5 | AI Copilot | Natural-language Q&A grounded in real stock + portfolio data, structured JSON output (summary, risk, pros/cons, technical/fundamental analysis, recommendation, confidence) |
| 6 | Market News | NewsAPI integration, keyword-based sentiment tagging (Bullish/Bearish/Neutral), category filters, auto-refresh every 15 min |
| 7 | Analytics | Portfolio growth chart, sector allocation, monthly returns, dividend income estimate |
| 8 | Admin | User & role management, activate/deactivate/unlock accounts, platform-wide trade view, audit log viewer, dashboard stats, market-hours config |

Plus: full dashboard shell (sidebar, top bar, Cmd+K command palette), buy/sell modals,
loading skeletons, animated stat cards, dark glassmorphic design system throughout.

## Project structure

```
trademind-ai/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/trademind/
│       │   ├── TradeMindAiApplication.java
│       │   ├── auth/          (Module 1)
│       │   ├── portfolio/     (Module 2)
│       │   ├── watchlist/     (Module 3)
│       │   ├── stock/         (Module 4)
│       │   ├── ai/            (Module 5)
│       │   ├── news/          (Module 6)
│       │   ├── analytics/     (Module 7)
│       │   ├── admin/         (Module 8)
│       │   ├── security/      (JWT filter, JwtService, UserDetailsService)
│       │   ├── config/        (SecurityConfig, WebClientConfig, MarketDataProperties)
│       │   └── common/        (exceptions, EmailService, AuditService, API clients)
│       └── resources/
│           ├── application.yml
│           └── schema.sql     (all tables, modules 1-8)
└── frontend/
    └── src/
        ├── api/                (one file per module: auth, portfolio, watchlist, stock, ai, news, analytics, admin)
        ├── components/
        │   ├── auth/           (AuthLayout, FormField)
        │   ├── layout/         (Sidebar, TopBar, CommandPalette, DashboardLayout)
        │   └── dashboard/      (StatCard, Badge, Modal, Skeleton)
        ├── context/AuthContext.jsx
        ├── routes/             (ProtectedRoute, AdminRoute)
        └── pages/
            ├── auth/           (Login, Register, ForgotPassword, ResetPassword, VerifyEmail)
            ├── Dashboard.jsx   (overview with all widgets)
            └── portfolio/, watchlist/, stock/, ai/, news/, analytics/, admin/
```

## Setup

### 1. Database
Run `backend/src/main/resources/schema.sql` against your Supabase Postgres instance
(SQL Editor or `psql`). It's idempotent (`CREATE TABLE IF NOT EXISTS`) so it's safe
to re-run.

### 2. Backend environment
Copy `backend/.env.example` to `.env` and fill in:
- `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` — your Supabase connection (note the
  `?prepareThreshold=0` already in the example — required for Supabase's PgBouncer pooler)
- `JWT_SECRET` — any long random string
- Optional provider keys (all features degrade gracefully if left blank):
  - `TWELVE_DATA_API_KEY` / `FINNHUB_API_KEY` — stock quotes & company profiles
  - `NEWS_API_KEY` — market news feed
  - `AI_API_KEY` (+ `AI_BASE_URL`, `AI_MODEL`) — AI Copilot, any OpenAI-compatible endpoint

```bash
cd backend
export $(cat .env | xargs)
mvn spring-boot:run
```
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173`.

## Testing instructions

**Auth (Module 1):** register, verify email (link logged server-side if SMTP isn't
configured), log in, land on `/dashboard`.

**Portfolio (Module 2):**
```bash
curl -X POST http://localhost:8080/api/v1/portfolio/buy \
  -H "Authorization: Bearer $ACCESS_TOKEN" -H "Content-Type: application/json" \
  -d '{"symbol":"AAPL","quantity":10,"price":195.50}'
```
Check `/portfolio` in the UI — holdings, P&L and ROI update immediately; the
performance chart populates once a snapshot exists (recorded immediately after
every trade, plus nightly at 00:05).

**Watchlist (Module 3):** create a list, add a symbol, set a price alert — the
scheduler checks alerts every 5 minutes and deactivates + emails on trigger.

**Stock Details (Module 4):** visit `/stocks/AAPL` (or any symbol). Without
`TWELVE_DATA_API_KEY`/`FINNHUB_API_KEY`, price/fundamentals stay blank until you
set one; the price chart uses a seeded, clearly-labeled modeled series as a
fallback so the UI is still demoable without a data subscription.

**AI Copilot (Module 5):** ask "Should I buy AAPL?" — without `AI_API_KEY`, you'll
get a friendly "not configured" message instead of an error; with a key set,
responses are grounded in whatever stock/portfolio data is available.

**News (Module 6):** without `NEWS_API_KEY`, the feed stays empty (explained in the
UI's empty state) — set the key and either wait 15 minutes or call
`POST /api/v1/news/refresh?query=stock market`.

**Analytics (Module 7):** `/analytics` — sector allocation and dividend estimates
populate as soon as you have holdings; monthly returns need at least a few days of
snapshots to show a trend.

**Admin (Module 8):** log in as a user with the `ADMIN` role (promote one via direct
SQL: `insert into user_roles select id, (select id from roles where name='ADMIN') from users where email='you@example.com';`),
then visit `/admin` — Users, Trades, Audit logs, Market config tabs.

## Known simplifications (documented, not hidden)
- Historical OHLC data falls back to a seeded random-walk series when a free-tier
  time-series endpoint isn't available — clearly labeled in the UI, not passed off
  as certified data.
- Sentiment tagging on news is keyword-heuristic, not ML-based — the swap-in point
  is noted in `NewsService` if you want to route it through the AI Copilot's client instead.
- Dividend income is an estimate from current yield, not a ledger of actual declared
  payouts (no dividend-payment-history table in this build).
- Admin dashboard stats iterate in-memory over `findAll()` for a couple of counts —
  fine at demo scale, worth replacing with `COUNT` queries before real production load.
