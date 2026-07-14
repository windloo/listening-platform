CREATE TABLE encoding_item (
  id BIGINT NOT NULL PRIMARY KEY,
  source_system VARCHAR(64) NOT NULL,
  name VARCHAR(256) NOT NULL,
  source_url VARCHAR(1024) NOT NULL,
  output_url VARCHAR(1024) NULL,
  output_format VARCHAR(10) NOT NULL,
  status VARCHAR(10) NOT NULL,
  file_sha256_hash VARCHAR(64) NULL,
  file_size_in_bytes BIGINT NULL,
  log_text TEXT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  deletion_time DATETIME NULL,
  KEY idx_enc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;