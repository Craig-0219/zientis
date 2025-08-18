-- Zientis 資料庫初始化腳本
-- 版本: 0.1.0-ALPHA

USE zientis_development;

-- 玩家基礎資料表
CREATE TABLE IF NOT EXISTS players (
    id CHAR(36) PRIMARY KEY COMMENT '玩家UUID',
    username VARCHAR(16) NOT NULL COMMENT '玩家名稱',
    display_name VARCHAR(32) COMMENT '顯示名稱',
    first_login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '首次登入時間',
    last_login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後登入時間',
    total_playtime BIGINT DEFAULT 0 COMMENT '總遊戲時間(秒)',
    is_banned BOOLEAN DEFAULT FALSE COMMENT '是否被封禁',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_username (username),
    INDEX idx_last_login (last_login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='玩家基礎資料表';

-- 經濟帳戶表
CREATE TABLE IF NOT EXISTS economy_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '帳戶ID',
    player_id CHAR(36) NOT NULL COMMENT '玩家UUID',
    balance DECIMAL(15,2) DEFAULT 100.00 COMMENT '帳戶餘額',
    is_frozen BOOLEAN DEFAULT FALSE COMMENT '帳戶是否凍結',
    frozen_reason VARCHAR(255) COMMENT '凍結原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    UNIQUE KEY uk_player_account (player_id),
    INDEX idx_balance (balance)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='經濟帳戶表';

-- 交易記錄表
CREATE TABLE IF NOT EXISTS transactions (
    id CHAR(36) PRIMARY KEY COMMENT '交易UUID',
    from_player_id CHAR(36) COMMENT '發送方玩家UUID',
    to_player_id CHAR(36) COMMENT '接收方玩家UUID',
    amount DECIMAL(15,2) NOT NULL COMMENT '交易金額',
    transaction_type ENUM('TRANSFER', 'DEPOSIT', 'WITHDRAW', 'SYSTEM', 'REWARD', 'PURCHASE') NOT NULL COMMENT '交易類型',
    description VARCHAR(500) COMMENT '交易描述',
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') DEFAULT 'COMPLETED' COMMENT '交易狀態',
    metadata JSON COMMENT '額外的交易元數據',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '交易時間',
    FOREIGN KEY (from_player_id) REFERENCES players(id) ON DELETE SET NULL,
    FOREIGN KEY (to_player_id) REFERENCES players(id) ON DELETE SET NULL,
    INDEX idx_from_player (from_player_id),
    INDEX idx_to_player (to_player_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易記錄表';

-- 島嶼資料表
CREATE TABLE IF NOT EXISTS islands (
    id CHAR(36) PRIMARY KEY COMMENT '島嶼UUID',
    owner_id CHAR(36) NOT NULL COMMENT '島主UUID',
    world_name VARCHAR(100) NOT NULL COMMENT '世界名稱',
    island_name VARCHAR(50) COMMENT '島嶼名稱',
    island_level INT DEFAULT 1 COMMENT '島嶼等級',
    island_size INT DEFAULT 100 COMMENT '島嶼大小',
    spawn_x DOUBLE DEFAULT 0 COMMENT '重生點X座標',
    spawn_y DOUBLE DEFAULT 64 COMMENT '重生點Y座標',
    spawn_z DOUBLE DEFAULT 0 COMMENT '重生點Z座標',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公開',
    visit_count INT DEFAULT 0 COMMENT '訪問次數',
    last_visit_time TIMESTAMP COMMENT '最後訪問時間',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    FOREIGN KEY (owner_id) REFERENCES players(id) ON DELETE CASCADE,
    UNIQUE KEY uk_world_name (world_name),
    INDEX idx_owner (owner_id),
    INDEX idx_level (island_level),
    INDEX idx_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='島嶼資料表';

-- 國家資料表
CREATE TABLE IF NOT EXISTS nations (
    id CHAR(36) PRIMARY KEY COMMENT '國家UUID',
    nation_name VARCHAR(50) NOT NULL COMMENT '國家名稱',
    founder_id CHAR(36) NOT NULL COMMENT '創建者UUID',
    leader_id CHAR(36) NOT NULL COMMENT '國家領袖UUID',
    nation_level INT DEFAULT 1 COMMENT '國家等級',
    treasury_balance DECIMAL(15,2) DEFAULT 0.00 COMMENT '國庫餘額',
    max_members INT DEFAULT 10 COMMENT '最大成員數',
    description TEXT COMMENT '國家描述',
    founded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建國時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    FOREIGN KEY (founder_id) REFERENCES players(id) ON DELETE RESTRICT,
    FOREIGN KEY (leader_id) REFERENCES players(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_nation_name (nation_name),
    INDEX idx_leader (leader_id),
    INDEX idx_level (nation_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='國家資料表';

-- 國家成員表
CREATE TABLE IF NOT EXISTS nation_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成員ID',
    nation_id CHAR(36) NOT NULL COMMENT '國家UUID',
    player_id CHAR(36) NOT NULL COMMENT '玩家UUID',
    role ENUM('LEADER', 'OFFICER', 'MEMBER') DEFAULT 'MEMBER' COMMENT '成員角色',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入時間',
    FOREIGN KEY (nation_id) REFERENCES nations(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    UNIQUE KEY uk_nation_player (nation_id, player_id),
    INDEX idx_player (player_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='國家成員表';

-- Discord 用戶映射表
CREATE TABLE IF NOT EXISTS discord_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '映射ID',
    discord_user_id VARCHAR(20) NOT NULL COMMENT 'Discord用戶ID',
    player_id CHAR(36) NOT NULL COMMENT '玩家UUID',
    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '驗證時間',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否啟用',
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    UNIQUE KEY uk_discord_user (discord_user_id),
    UNIQUE KEY uk_player_discord (player_id),
    INDEX idx_discord_id (discord_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Discord用戶映射表';

-- 系統設定表
CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(100) PRIMARY KEY COMMENT '設定鍵',
    setting_value TEXT COMMENT '設定值',
    setting_type ENUM('STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'JSON') DEFAULT 'STRING' COMMENT '設定類型',
    description VARCHAR(255) COMMENT '設定描述',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系統設定表';

-- 插入初始設定
INSERT INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('economy.starting_balance', '100.00', 'DECIMAL', '新玩家起始餘額'),
('economy.max_transfer_amount', '1000000.00', 'DECIMAL', '單次轉帳最大金額'),
('multiworld.max_islands_per_player', '1', 'INTEGER', '每位玩家最大島嶼數'),
('nations.max_members_base', '10', 'INTEGER', '國家基礎最大成員數'),
('nations.founding_cost', '10000.00', 'DECIMAL', '建國費用');