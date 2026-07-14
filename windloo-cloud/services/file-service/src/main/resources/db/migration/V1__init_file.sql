CREATE TABLE uploaded_item (
  id BIGINT NOT NULL PRIMARY KEY,
  file_name VARCHAR(1024) NOT NULL,
  file_size_in_bytes BIGINT NOT NULL,
  file_sha256_hash VARCHAR(64) NOT NULL,
  remote_url VARCHAR(1024) NOT NULL,
  backup_url VARCHAR(1024) NOT NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  deletion_time DATETIME NULL,
  UNIQUE KEY uk_hash_size (file_sha256_hash, file_size_in_bytes)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;