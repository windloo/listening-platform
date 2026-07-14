-- ============================================================
-- Seata AT 模式 undo_log 表初始化脚本
-- 必须在启动 listening-service / media-encoder-service 之前执行！
-- 原因：Seata 在 Spring Bean 创建阶段就检查 undo_log 表是否存在，
--       而 Flyway 要等 DataSource 就绪后才执行，此时 Seata 已先崩溃。
--       所以 undo_log 表必须在服务启动前手动创建。
-- ============================================================

-- windloo_listening 库
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT transaction mode undo table';

-- windloo_encoder 库
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT transaction mode undo table';