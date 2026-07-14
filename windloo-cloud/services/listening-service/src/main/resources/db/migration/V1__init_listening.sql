CREATE TABLE category (
  id BIGINT NOT NULL PRIMARY KEY,
  sequence_number INT NOT NULL,
  name_chinese VARCHAR(200) NOT NULL,
  name_english VARCHAR(200) NOT NULL,
  cover_url VARCHAR(500) NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  deletion_time DATETIME NULL,
  KEY idx_category_seq (sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE album (
  id BIGINT NOT NULL PRIMARY KEY,
  sequence_number INT NOT NULL,
  name_chinese VARCHAR(200) NOT NULL,
  name_english VARCHAR(200) NOT NULL,
  category_id BIGINT NOT NULL,
  is_visible TINYINT NOT NULL DEFAULT 0,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  deletion_time DATETIME NULL,
  KEY idx_album_cat (category_id, is_deleted, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE episode (
  id BIGINT NOT NULL PRIMARY KEY,
  sequence_number INT NOT NULL,
  name_chinese VARCHAR(200) NOT NULL,
  name_english VARCHAR(200) NOT NULL,
  album_id BIGINT NOT NULL,
  audio_url VARCHAR(1000) NOT NULL,
  duration_in_second DOUBLE NOT NULL,
  subtitle LONGTEXT NOT NULL,
  subtitle_type VARCHAR(10) NOT NULL,
  is_visible TINYINT NOT NULL DEFAULT 1,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  deletion_time DATETIME NULL,
  KEY idx_ep_album (album_id, is_deleted, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;