-- Test data for integration tests
INSERT INTO users (id, email, first_name, last_name, password_hash, is_active, risk_profile, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'test1@example.com', 'Test', 'User1', '$2a$10$hash1', true, 'LOW', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', 'test2@example.com', 'Test', 'User2', '$2a$10$hash2', true, 'MEDIUM', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440003', 'test3@example.com', 'Test', 'User3', '$2a$10$hash3', false, 'HIGH', CURRENT_TIMESTAMP);

INSERT INTO merchants (id, name, category, country_code, risk_level, created_at) VALUES
('test-merchant-1', 'Test Merchant 1', 'E-COMMERCE', 'US', 'LOW', CURRENT_TIMESTAMP),
('test-merchant-2', 'Test Merchant 2', 'GAS_STATION', 'US', 'LOW', CURRENT_TIMESTAMP),
('test-merchant-3', 'Risky Test Merchant', 'OTHER', 'XX', 'HIGH', CURRENT_TIMESTAMP);

INSERT INTO fraud_rules (id, name, description, rule_type, threshold_value, is_active, created_at) VALUES
('test-rule-1', 'Test High Amount', 'Test rule for high amounts', 'AMOUNT', 5000.00, true, CURRENT_TIMESTAMP),
('test-rule-2', 'Test Velocity', 'Test rule for transaction velocity', 'VELOCITY', 10.0, true, CURRENT_TIMESTAMP),
('test-rule-3', 'Inactive Test Rule', 'Inactive test rule', 'GEOGRAPHY', 1.0, false, CURRENT_TIMESTAMP);
