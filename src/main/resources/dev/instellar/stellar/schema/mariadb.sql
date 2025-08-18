-- Stellar MariaDB

CREATE TABLE `{prefix}player` (
  `pl_id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `pl_game_uuid` BINARY(16) NOT NULL,
  `pl_game_name` VARCHAR(32) NOT NULL,
  `pl_nickname` VARCHAR(32) NOT NULL,
  `pl_first_seen` DATETIME(6) NOT NULL,
  `pl_last_seen` DATETIME(6) NOT NULL,
  `pl_latest_data` INT UNSIGNED NULL,
  CONSTRAINT fk_pl_latest_data FOREIGN KEY (pl_latest_data) REFERENCES player_data (pd_id) ON DELETE SET NULL
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `{prefix}pl_game_uuid` ON `{prefix}player` (pl_game_uuid);
CREATE INDEX `{prefix}pl_last_seen` ON `{prefix}player` (pl_last_seen);

CREATE TABLE `{prefix}player_data` (
  `pd_id` BIGINT UNSIGNED AUTO_INCREMENT,
  `pd_player` INT UNSIGNED NOT NULL,
  `pd_vanilla_data` BLOB NULL,
  `pd_party_data` BLOB NULL,
  `pd_pc_data` BLOB NULL,
  `pd_research_data` BLOB NULL,
  `pd_created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `pd_month` INT AS (YEAR(pd_created_at) * 100 + MONTH(pd_created_at)) PERSISTENT,
  `pd_hash` BINARY(32) NOT NULL,
  PRIMARY KEY (pd_month, pd_id),
  CONSTRAINT fk_pd_player FOREIGN KEY (pd_player) REFERENCES player (pl_id) ON DELETE CASCADE
)
CREATE UNIQUE INDEX `{prefix}pd_player_month_hash` ON `{prefix}player_data` (pd_player, pd_month, pd_hash);
CREATE INDEX `{prefix}pd_player_created` ON `{prefix}player_data` (pd_player, pd_created_at);
CREATE INDEX `{prefix}pd_player_hash` ON `{prefix}player_data` (pd_player, pd_hash);

CREATE TABLE `{prefix}player_server_data` (
    `psd_player` INT UNSIGNED NOT NULL,
    `psd_server` VARCHAR(64) NOT NULL,
    `psd_dimension` VARCHAR(64) NOT NULL,
    `psd_world_uuid` BINARY(16) NOT NULL,
    `psd_pos_x` DOUBLE NOT NULL,
    `psd_pos_y` DOUBLE NOT NULL,
    `psd_pos_z` DOUBLE NOT NULL,
    `psd_yaw` FLOAT NOT NULL,
    `psd_pitch` FLOAT NOT NULL,
    PRIMARY KEY (psd_player, psd_server, psd_world_uuid),
    CONSTRAINT fk_psd_player FOREIGN KEY (psd_player) REFERENCES player (pl_id) ON DELETE CASCADE
);

CREATE TABLE `{prefix}player_property` (
    `pp_player` INT UNSIGNED NOT NULL,
    `pp_key` VARCHAR(128) NOT NULL,
    `pp_value` VARCHAR(255) NOT NULL,
    `pp_updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (pp_player, pp_key),
    CONSTRAINT fk_pp_player FOREIGN KEY (pp_player) REFERENCES player (pl_id) ON DELETE CASCADE
);

CREATE TABLE `{prefix}stats` (
  `st_player` INT UNSIGNED NOT NULL,
  `st_data` BLOB NOT NULL,
  PRIMARY KEY (st_player),
  CONSTRAINT fk_st_player FOREIGN KEY (st_player) REFERENCES player (pl_id) ON DELETE CASCADE
);