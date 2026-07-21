CREATE TABLE ai_conversation (
  id            BIGINT NOT NULL PRIMARY KEY,
  user_id       BIGINT NOT NULL,
  title         VARCHAR(255) NOT NULL,
  is_deleted    TINYINT NOT NULL DEFAULT 0,
  create_time   DATETIME NOT NULL,
  update_time   DATETIME NOT NULL,
  deletion_time DATETIME NULL,
  KEY idx_conv_user (user_id, is_deleted, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ai_message (
  id              BIGINT NOT NULL PRIMARY KEY,
  conversation_id BIGINT NOT NULL,
  role            VARCHAR(16) NOT NULL,
  content         MEDIUMTEXT NOT NULL,
  is_deleted      TINYINT NOT NULL DEFAULT 0,
  create_time     DATETIME NOT NULL,
  update_time     DATETIME NOT NULL,
  deletion_time   DATETIME NULL,
  KEY idx_msg_conv (conversation_id, is_deleted, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;