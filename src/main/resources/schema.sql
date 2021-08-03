CREATE TABLE IF NOT EXISTS user_address (
  id INT AUTO_INCREMENT PRIMARY KEY,
  pkey VARCHAR(64) NOT NULL,
  address VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_transaction (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INTEGER,
  from_address VARCHAR(64),
  to_address VARCHAR(64),
  currency VARCHAR(6),
  amount DOUBLE,
  gas BIGINT,
  hash VARCHAR(66)
);

CREATE INDEX IF NOT EXISTS user_transaction_index
ON user_transaction (user_id);