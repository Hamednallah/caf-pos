CREATE INDEX idx_order_created_at ON "order"(created_at);
CREATE INDEX idx_item_category ON item(category_id);
CREATE INDEX idx_expense_recorded_at ON expense(recorded_at);
