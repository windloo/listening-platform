CREATE TABLE role (
  id BIGINT NOT NULL PRIMARY KEY,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(64) NOT NULL,
  create_time DATETIME NOT NULL,
  UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user (
  id BIGINT NOT NULL PRIMARY KEY,
  user_name VARCHAR(64) NOT NULL,
  normalized_user_name VARCHAR(64),
  phone VARCHAR(32),
  password_hash VARCHAR(255) NOT NULL,
  is_locked TINYINT NOT NULL DEFAULT 0,
  lockout_end DATETIME NULL,
  access_failed_count INT NOT NULL DEFAULT 0,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  deletion_time DATETIME NULL,
  UNIQUE KEY uk_user_normalized (normalized_user_name, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_role (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_user_role_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE undo_log (
  branch_id     BIGINT      NOT NULL,
  xid           VARCHAR(128) NOT NULL,
  context       VARCHAR(128) NOT NULL,
  rollback_info LONGBLOB    NOT NULL,
  log_status    INT         NOT NULL,
  log_created   DATETIME(6) NOT NULL,
  log_modified  DATETIME(6) NOT NULL,
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

INSERT INTO role (id, name, code, create_time) VALUES
  (1, 'User',  'USER',  NOW()),
  (2, 'Admin', 'ADMIN', NOW());