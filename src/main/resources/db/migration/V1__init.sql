-- V1__init.sql  (Flyway-style migration)
-- Conventions: money = BIGINT paise, dates the user sees = DATE (in user's tz),
-- audit timestamps = timestamptz. Ledgers are append-only; balances are derived.

CREATE TABLE users (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email         text NOT NULL UNIQUE,
  password_hash text NOT NULL,
  display_name  text,
  timezone      text NOT NULL DEFAULT 'Asia/Kolkata',
  currency      char(3) NOT NULL DEFAULT 'INR',
  streak_period text NOT NULL DEFAULT 'WEEK' CHECK (streak_period IN ('DAY','WEEK','MONTH')),
  created_at    timestamptz NOT NULL DEFAULT now()
);

-- ---------- Accounts & categories ----------
CREATE TABLE accounts (
  id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id               uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name                  text NOT NULL,
  type                  text NOT NULL CHECK (type IN ('CASH','BANK','WALLET')),
  opening_balance_paise bigint NOT NULL DEFAULT 0,
  is_archived           boolean NOT NULL DEFAULT false,
  created_at            timestamptz NOT NULL DEFAULT now(),
  UNIQUE (user_id, name)
);

CREATE TABLE categories (
  id      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name    text NOT NULL,
  kind    text NOT NULL CHECK (kind IN ('INCOME','EXPENSE')),
  UNIQUE (user_id, name, kind)
);

-- ---------- Obligations: bills, EMIs, rent (one engine) ----------
CREATE TABLE obligations (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  kind         text NOT NULL CHECK (kind IN ('BILL','EMI','RENT')),
  direction    text NOT NULL DEFAULT 'PAYABLE' CHECK (direction IN ('PAYABLE','RECEIVABLE')),
  name         text NOT NULL,
  amount_paise bigint NOT NULL CHECK (amount_paise > 0),
  frequency    text NOT NULL DEFAULT 'MONTHLY' CHECK (frequency IN ('WEEKLY','MONTHLY','QUARTERLY','YEARLY','ONCE')),
  start_date   date NOT NULL,
  end_date     date,
  account_id   uuid REFERENCES accounts(id),   -- default account used when marking paid
  status       text NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CLOSED')),
  created_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON obligations (user_id, kind);

-- Extra fields only EMIs need (1:1 with obligations where kind = 'EMI')
CREATE TABLE emi_details (
  obligation_id    uuid PRIMARY KEY REFERENCES obligations(id) ON DELETE CASCADE,
  lender           text,
  principal_paise  bigint NOT NULL CHECK (principal_paise > 0),
  annual_rate_bps  int    NOT NULL CHECK (annual_rate_bps >= 0),  -- 10.5% = 1050
  tenure_months    int    NOT NULL CHECK (tenure_months > 0)
);

-- Pre-generated rows: one per due date. Reminders, "7 of 12 paid", dashboards all read this.
CREATE TABLE obligation_instances (
  id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  obligation_id  uuid NOT NULL REFERENCES obligations(id) ON DELETE CASCADE,
  user_id        uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  installment_no int,                           -- EMIs only
  due_date       date NOT NULL,
  amount_paise   bigint NOT NULL CHECK (amount_paise > 0),
  status         text NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','PAID','SKIPPED')),
  paid_on        date,
  UNIQUE (obligation_id, due_date)
);
CREATE INDEX ON obligation_instances (user_id, status, due_date);

-- ---------- Transactions (income / expense ledger) ----------
CREATE TABLE transactions (
  id                     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id                uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  account_id             uuid NOT NULL REFERENCES accounts(id),
  type                   text NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
  amount_paise           bigint NOT NULL CHECK (amount_paise > 0),
  category_id            uuid REFERENCES categories(id),
  occurred_on            date NOT NULL,
  note                   text,
  obligation_instance_id uuid UNIQUE REFERENCES obligation_instances(id),  -- set when created by "mark paid"
  created_at             timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON transactions (user_id, occurred_on DESC);
CREATE INDEX ON transactions (account_id, occurred_on DESC);

-- ---------- Wishlist + piggy ----------
CREATE TABLE wishlist_items (
  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id             uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name                text NOT NULL,
  target_amount_paise bigint NOT NULL CHECK (target_amount_paise > 0),
  target_date         date,
  status              text NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','ACHIEVED','ABANDONED')),
  achieved_at         timestamptz,
  created_at          timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON wishlist_items (user_id, status);

-- Append-only ledger. "Saved so far" = sum of this, never an editable column.
CREATE TABLE piggy_entries (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id      uuid NOT NULL REFERENCES wishlist_items(id) ON DELETE CASCADE,
  user_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type         text NOT NULL CHECK (type IN ('DEPOSIT','WITHDRAWAL')),
  amount_paise bigint NOT NULL CHECK (amount_paise > 0),
  entry_date   date NOT NULL,                   -- computed in user's timezone; streaks read this
  note         text,
  created_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON piggy_entries (item_id);
CREATE INDEX ON piggy_entries (user_id, entry_date);

-- Cache only. Can always be rebuilt from piggy_entries (deposits) by period.
CREATE TABLE streak_stats (
  user_id          uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  current_streak   int  NOT NULL DEFAULT 0,
  best_streak      int  NOT NULL DEFAULT 0,
  last_period_date date,                        -- start of last period with a deposit
  updated_at       timestamptz NOT NULL DEFAULT now()
);

-- ---------- Reminders ----------
CREATE TABLE reminder_rules (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  obligation_id uuid NOT NULL REFERENCES obligations(id) ON DELETE CASCADE,
  days_before   int  NOT NULL CHECK (days_before >= 0),
  channel       text NOT NULL DEFAULT 'PUSH' CHECK (channel IN ('PUSH','EMAIL','SMS')),
  UNIQUE (obligation_id, days_before, channel)
);

-- Scheduler queue: SELECT ... WHERE sent_at IS NULL AND remind_at <= now() FOR UPDATE SKIP LOCKED
CREATE TABLE reminders (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  instance_id uuid NOT NULL REFERENCES obligation_instances(id) ON DELETE CASCADE,
  rule_id     uuid NOT NULL REFERENCES reminder_rules(id) ON DELETE CASCADE,
  remind_at   timestamptz NOT NULL,
  sent_at     timestamptz,
  UNIQUE (instance_id, rule_id)
);
CREATE INDEX ON reminders (remind_at) WHERE sent_at IS NULL;

-- ---------- Derived balances ----------
CREATE VIEW account_balances AS
SELECT a.id AS account_id, a.user_id,
       a.opening_balance_paise
         + COALESCE(SUM(CASE t.type WHEN 'INCOME' THEN t.amount_paise ELSE -t.amount_paise END), 0) AS balance_paise
FROM accounts a
LEFT JOIN transactions t ON t.account_id = a.id
GROUP BY a.id;

CREATE VIEW piggy_balances AS
SELECT w.id AS item_id, w.user_id, w.target_amount_paise,
       COALESCE(SUM(CASE p.type WHEN 'DEPOSIT' THEN p.amount_paise ELSE -p.amount_paise END), 0) AS saved_paise
FROM wishlist_items w
LEFT JOIN piggy_entries p ON p.item_id = w.id
GROUP BY w.id;

-- Free to spend = SUM(account_balances) - SUM(saved_paise of ACTIVE items)
--                 - SUM(PENDING PAYABLE instances due this month)
