-- Initialize FraudGuard Database Schema

-- Create Users Table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    amount DECIMAL(15,2) NOT NULL,
    merchant VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    location VARCHAR(255),
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    fraud_score DECIMAL(3,2) DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Fraud Alerts Table
CREATE TABLE IF NOT EXISTS fraud_alerts (
    id SERIAL PRIMARY KEY,
    transaction_id INTEGER REFERENCES transactions(id),
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE
);

-- Create Indexes for Performance
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_fraud_score ON transactions(fraud_score);
CREATE INDEX idx_transactions_time ON transactions(transaction_time);
CREATE INDEX idx_fraud_alerts_transaction_id ON fraud_alerts(transaction_id);

-- Insert Sample Data
INSERT INTO users (email, first_name, last_name) VALUES
('john.doe@email.com', 'John', 'Doe'),
('jane.smith@email.com', 'Jane', 'Smith'),
('mike.johnson@email.com', 'Mike', 'Johnson');

INSERT INTO transactions (user_id, amount, merchant, category, location, fraud_score, status) VALUES
(1, 50.00, 'Starbucks', 'Food & Drink', 'New York', 0.1, 'APPROVED'),
(1, 1200.00, 'Electronics Store', 'Electronics', 'California', 0.8, 'FLAGGED'),
(2, 25.50, 'Gas Station', 'Transportation', 'Texas', 0.2, 'APPROVED'),
(3, 500.00, 'Online Store', 'Shopping', 'Unknown', 0.6, 'PENDING');
