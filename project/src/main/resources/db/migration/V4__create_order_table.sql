CREATE TABLE `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total DECIMAL(10,2) NOT NULL,
    quantity BIGINT NOT NULL,
    placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    custom_id BIGINT,
    event_id BIGINT,
    CONSTRAINT fk_order_custom FOREIGN KEY (custom_id) REFERENCES customer(id) ON DELETE SET NULL,
    CONSTRAINT fk_order_event FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE SET NULL
);