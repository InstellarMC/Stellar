-- Stellar MariaDB

CREATE TABLE `player` (
  `pl_id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `pl_mc_uuid` BINARY(16) NOT NULL,
  `pl_mc_name` VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `pl_nickname` VARCHAR(32) NOT NULL,
  `pl_first_seen` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `pl_last_seen` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `pl_latest` INT UNSIGNED NULL
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `pl_mc_uuid` ON `player` (pl_mc_uuid);
CREATE INDEX `pl_last_seen` ON `player` (pl_last_seen);

CREATE TABLE `player_revision` (
  `pL_rev_id` BIGINT UNSIGNED AUTO_INCREMENT,
  `pL_rev_player` INT UNSIGNED NOT NULL,
  `pL_rev_created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `pL_rev_parent_id` BIGINT UNSIGNED DEFAULT NULL,
  `pL_rev_hash` BINARY(32) DEFAULT '' NOT NULL,
  PRIMARY KEY (`pl_rev_id`)
);
CREATE INDEX `pl_rev_player_created` ON `player_revision` (pl_rev_player, pl_rev_created_at);

CREATE TABLE `player_vanilla_data` (
  `pvd_id` BIGINT UNSIGNED,
  `pvd_type` SMALLINT UNSIGNED NOT NULL, -- 0: inventory, 1: ender chest
  `pvd_data` MEDIUMBLOB NOT NULL,
  PRIMARY KEY (`pvd_id`, `pvd_type`)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE player_party_data (
  `ppd_ref` BIGINT UNSIGNED NOT NULL,
  `ppd_data` MEDIUMBLOB NOT NULL,
  PRIMARY KEY (ppd_ref)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE player_pc_data (
  `pcd_ref` BIGINT UNSIGNED NOT NULL,
  `pcd_box_id` TINYINT UNSIGNED NOT NULL,
  `pcd_data` MEDIUMBLOB NOT NULL,
  PRIMARY KEY (pcd_ref, pcd_box_id)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE player_research_data (
  `prd_ref` BIGINT UNSIGNED NOT NULL,
  `prd_data` MEDIUMBLOB NOT NULL,
  PRIMARY KEY (prd_ref)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE `player_custom_data` (
  `pdm_id` BIGINT UNSIGNED,
  `pdm_slot` INT UNSIGNED NOT NULL,
  `pdm_data` MEDIUMBLOB NOT NULL,
  `pdm_arg0` VARCHAR(64) NULL,
  `pdm_arg1` VARCHAR(64) NULL,
  `pdm_arg2` VARCHAR(64) NULL,
  `pdm_arg3` VARCHAR(64) NULL,
  PRIMARY KEY (pdm_id, pdm_slot)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE `player_server_data` (
    `psd_player` INT UNSIGNED NOT NULL,
    `psd_server` VARCHAR(16) NOT NULL,
    `psd_dimension` VARCHAR(32) NOT NULL,
    `psd_world_uuid` BINARY(16) NOT NULL,
    `psd_pos_x` DOUBLE NOT NULL,
    `psd_pos_y` DOUBLE NOT NULL,
    `psd_pos_z` DOUBLE NOT NULL,
    `psd_yaw` FLOAT NOT NULL,
    `psd_pitch` FLOAT NOT NULL,
    PRIMARY KEY (psd_player, psd_server, psd_world_uuid)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE `player_property` (
    `pp_player` INT UNSIGNED NOT NULL,
    `pp_key` VARCHAR(128) NOT NULL,
    `pp_value` VARCHAR(255) NOT NULL,
    `pp_updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (pp_player, pp_key)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE `player_stat` (
  `ps_player` INT UNSIGNED NOT NULL,
  `ps_data` BLOB NOT NULL,
  `ps_updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (ps_player)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE `player_pokedex` (
  `ppd_player` INT UNSIGNED NOT NULL,
  `ppd_species` SMALLINT UNSIGNED NOT NULL,
  `ppd_form` TINYINT UNSIGNED NOT NULL DEFAULT 0, -- 0: 기본, 1: 이로치, 2: 메가, 3: 메가X, 4: 메가Y, 5: Z, 6: 다이맥스, 7: 테라스탈
  `ppd_caught` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `ppd_caught_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (ppd_player, ppd_species, ppd_form)
) DEFAULT CHARSET = utf8mb4;
CREATE INDEX `ppd_player_caught` ON `player_pokedex` (ppd_player, ppd_caught);

CREATE TABLE `player_ban` (
  `pb_player` INT UNSIGNED NOT NULL,
  `pb_issuer` INT UNSIGNED NULL,
  `pb_reason` VARCHAR(255) NOT NULL,
  `pb_banned_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `pb_expires_at` DATETIME(6) NULL,
  PRIMARY KEY (pb_player)
) DEFAULT CHARSET = utf8mb4;
ALTER TABLE `player_ban` ADD CONSTRAINT `pb_valid_expires` CHECK (pb_expires_at IS NULL OR pb_expires_at > pb_banned_at);
CREATE INDEX `pb_expires_at` ON `player_ban` (pb_expires_at);

DELIMITER //

CREATE PROCEDURE SavePlayerData(
    IN p_player_id INT UNSIGNED,
    IN p_hash BINARY(32),
    IN p_vanilla_inventory MEDIUMBLOB,
    IN p_vanilla_enderchest MEDIUMBLOB,
    IN p_party_data MEDIUMBLOB,
    IN p_research_data MEDIUMBLOB,
    IN p_pc_boxes JSON  -- PC 박스 데이터를 JSON으로 전달
)
BEGIN
    DECLARE v_pd_id BIGINT UNSIGNED;
    DECLARE v_minutes_elapsed INT;
    DECLARE v_parent_id BIGINT UNSIGNED DEFAULT NULL;
    DECLARE v_create_new BOOLEAN DEFAULT FALSE;

    -- 최신 데이터 조회
    SELECT pd_id,
           TIMESTAMPDIFF(MINUTE, pd_created_at, NOW()),
           pd_id
    INTO v_pd_id, v_minutes_elapsed, v_parent_id
    FROM player_data
    WHERE pd_player = p_player_id
    ORDER BY pd_created_at DESC
    LIMIT 1;

    -- 15분 초과 또는 첫 저장인 경우 새 레코드 생성
    IF v_pd_id IS NULL OR v_minutes_elapsed >= 15 THEN
        INSERT INTO player_data (pd_player, pd_parent, pd_hash)
        VALUES (p_player_id, v_parent_id, p_hash);

        SET v_pd_id = LAST_INSERT_ID();
        SET v_create_new = TRUE;
    END IF;

    -- 데이터 저장/업데이트
    INSERT INTO player_vanilla_data (pdv_id, pdv_type, pdv_data) VALUES
    (v_pd_id, 0, p_vanilla_inventory),
    (v_pd_id, 1, p_vanilla_enderchest)
    ON DUPLICATE KEY UPDATE pdv_data = VALUES(pdv_data);

    INSERT INTO player_party_data (ppd_ref, ppd_data)
    VALUES (v_pd_id, p_party_data)
    ON DUPLICATE KEY UPDATE ppd_data = VALUES(ppd_data);

    INSERT INTO player_research_data (prd_ref, prd_data)
    VALUES (v_pd_id, p_research_data)
    ON DUPLICATE KEY UPDATE prd_data = VALUES(prd_data);

    -- PC 박스 데이터 처리 (JSON 파싱하여 반복 처리)
    -- 실제 구현 시 JSON_TABLE 함수 사용 또는 애플리케이션에서 개별 호출

    -- 결과 반환
    SELECT v_pd_id AS used_pd_id, v_create_new AS created_new;

END //

DELIMITER ;