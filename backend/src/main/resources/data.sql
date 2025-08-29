-- Sample Users
INSERT INTO users (id, email, first_name, last_name, created_at, is_active) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'john.doe@example.com', 'John', 'Doe', NOW(), true),
('550e8400-e29b-41d4-a716-446655440002', 'jane.smith@example.com', 'Jane', 'Smith', NOW(), true),
('550e8400-e29b-41d4-a716-446655440003', 'suspicious@test.com', 'Test', 'User', NOW(), false);

-- Sample Merchants
INSERT INTO merchants (id, name, category, country_code, risk_level) VALUES
('merchant-001', 'Amazon', 'E-COMMERCE', 'US', 'LOW'),
('merchant-002', 'Shell Gas Station', 'GAS_STATION', 'US', 'LOW'),
('merchant-003', 'Suspicious Shop', 'OTHER', 'XX', 'HIGH');

-- Sample Fraud Rules
INSERT INTO fraud_rules (id, name, description, rule_type, threshold_value, is_active) VALUES
('rule-001', 'High Amount Transaction', 'Flag transactions over $10,000', 'AMOUNT', 10000.00, true),
('rule-002', 'Velocity Check', 'More than 5 transactions in 1 hour', 'VELOCITY', 5.0, true),
('rule-003', 'Geographic Anomaly', 'Transaction from unusual location', 'GEOGRAPHY', 1.0, true);
