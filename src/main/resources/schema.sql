CREATE TABLE IF NOT EXISTS user_address (
  id INT AUTO_INCREMENT PRIMARY KEY,
  chain VARCHAR(8) NOT NULL,
  pkey VARCHAR(128) NOT NULL,
  public_key VARCHAR(128),
  address VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_transaction (
  id INT AUTO_INCREMENT PRIMARY KEY,
  from_address VARCHAR(64),
  to_address VARCHAR(64),
  chain VARCHAR(8),
  currency VARCHAR(6),
  amount VARCHAR(64),
  gas VARCHAR(32),
  hash VARCHAR(66),
  block_number BIGINT,
  confirm_count INT
);

CREATE TABLE IF NOT EXISTS block (
  chain VARCHAR(8) PRIMARY KEY,
  last_block BIGINT
);

CREATE INDEX IF NOT EXISTS user_address_index_address
ON user_address (address);

CREATE INDEX IF NOT EXISTS user_transaction_index_from_address
ON user_transaction (from_address);

CREATE INDEX IF NOT EXISTS user_transaction_index_to_address
ON user_transaction (to_address);

CREATE INDEX IF NOT EXISTS user_transaction_index_confirm_count
ON user_transaction (confirm_count);

CREATE INDEX IF NOT EXISTS user_transaction_index_hash
ON user_transaction (hash);