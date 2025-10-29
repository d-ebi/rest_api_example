-- Enable foreign keys
PRAGMA foreign_keys = ON;

-- Re-create tables on each startup (dev only)
DROP TABLE IF EXISTS career_histories;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    age INTEGER NOT NULL CHECK (age BETWEEN 0 AND 150),
    birthday TEXT NOT NULL CHECK (birthday >= '1900/01/01' AND birthday <= '2099/12/31'),
    height REAL CHECK (height IS NULL OR (height >= 0.0 AND height <= 300.0 AND CAST(ROUND(height * 10) AS INTEGER) = height * 10)),
    zip_code TEXT CHECK (zip_code IS NULL OR (length(zip_code) = 8 AND zip_code GLOB '???-????')),
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

-- Career histories table
CREATE TABLE IF NOT EXISTS career_histories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    period_from TEXT NOT NULL CHECK (period_from >= '1900/01/01' AND period_from <= '2099/12/31'),
    period_to   TEXT NOT NULL CHECK (period_to   >= '1900/01/01' AND period_to   <= '2099/12/31'),
    CHECK (period_from <= period_to),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);
CREATE INDEX IF NOT EXISTS idx_career_histories_user_id ON career_histories(user_id);
