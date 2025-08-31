
-- Sample seed data
INSERT INTO users (username, email, password, role) VALUES
 ('admin', 'admin@fraudguard.com', '$2a$10$6s3Zbq8r8i0D9gXw/9uQ3e4bC6S7v0JmJmB5n1D2p8cQFZ9s9d9y2', 'ADMIN'); -- password: admin123

INSERT INTO alert (title, description, severity, status) VALUES
 ('Suspicious Transaction', 'Large wire transfer flagged', 'HIGH', 'NEW'),
 ('Crypto Layering', 'Unusual crypto exchange activity', 'HIGH', 'IN_REVIEW'),
 ('Structuring', 'Multiple deposits below threshold', 'MEDIUM', 'ESCALATED');
