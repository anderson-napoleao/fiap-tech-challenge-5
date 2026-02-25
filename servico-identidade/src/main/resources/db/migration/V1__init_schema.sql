CREATE TABLE identity_users (
  id VARCHAR(36) PRIMARY KEY,
  username VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(120) NOT NULL,
  enabled BOOLEAN NOT NULL
);

CREATE TABLE identity_user_roles (
  user_id VARCHAR(36) NOT NULL,
  role VARCHAR(50) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_identity_user_roles_user
    FOREIGN KEY (user_id)
    REFERENCES identity_users(id)
    ON DELETE CASCADE
);

CREATE INDEX idx_identity_user_roles_user ON identity_user_roles(user_id);
