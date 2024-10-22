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
