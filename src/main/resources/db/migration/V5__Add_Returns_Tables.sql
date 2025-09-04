CREATE TABLE returns (
    id IDENTITY PRIMARY KEY,
    order_id INT NOT NULL,
    returned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_by_user_id INT NOT NULL,
    total_refund_amount DECIMAL(18, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES "order"(id),
    FOREIGN KEY (processed_by_user_id) REFERENCES "user"(id)
);

CREATE TABLE return_item (
    id IDENTITY PRIMARY KEY,
    return_id INT NOT NULL,
    order_item_id INT NOT NULL,
    quantity_returned INT NOT NULL,
    FOREIGN KEY (return_id) REFERENCES returns(id),
    FOREIGN KEY (order_item_id) REFERENCES order_item(id)
);
