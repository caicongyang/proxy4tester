CREATE TABLE IF NOT EXISTS route_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id VARCHAR(255) NOT NULL,
    path VARCHAR(255) NOT NULL,
    uri VARCHAR(255) NOT NULL,
    filters TEXT,
    `filter_order` INT,
    enabled BOOLEAN DEFAULT TRUE,
    direct_response BOOLEAN DEFAULT FALSE,
    response_body TEXT,
    response_content_type VARCHAR(100),
    response_status_code INT
);

-- TCP Route Definition Table
CREATE TABLE IF NOT EXISTS tcp_route_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id VARCHAR(255) NOT NULL UNIQUE,
    local_port INT NOT NULL,
    remote_host VARCHAR(255),
    remote_port INT,
    direct_response BOOLEAN NOT NULL DEFAULT FALSE,
    mock_response TEXT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
