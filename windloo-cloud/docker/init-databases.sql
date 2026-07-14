-- MySQL 首次启动时自动执行：创建业务数据库 + Seata undo_log 表
CREATE DATABASE IF NOT EXISTS windloo_identity;
CREATE DATABASE IF NOT EXISTS windloo_listening;
CREATE DATABASE IF NOT EXISTS windloo_encoder;
CREATE DATABASE IF NOT EXISTS windloo_file;

USE windloo_listening;
CREATE TABLE IF NOT EXISTS undo_log (
  branch_id     BIGINT      NOT NULL,
  xid           VARCHAR(128) NOT NULL,
  context       VARCHAR(128) NOT NULL,
  rollback_info LONGBLOB    NOT NULL,
  log_status    INT         NOT NULL,
  log_created   DATETIME(6) NOT NULL,
  log_modified  DATETIME(6) NOT NULL,
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE windloo_encoder;
CREATE TABLE IF NOT EXISTS undo_log (
  branch_id     BIGINT      NOT NULL,
  xid           VARCHAR(128) NOT NULL,
  context       VARCHAR(128) NOT NULL,
  rollback_info LONGBLOB    NOT NULL,
  log_status    INT         NOT NULL,
  log_created   DATETIME(6) NOT NULL,
  log_modified  DATETIME(6) NOT NULL,
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;