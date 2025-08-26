# Create this file in ml-service directory
import requests
import random

def generate_sample_transactions(user_id, count=20):
    """Generate sample transactions for testing"""
    transactions = []
    
    for i in range(count):
        transaction = {
            "user_id": user_id,
            "amount": round(random.uniform(20, 200), 2),
            "merchant_category": random.choice(['grocery', 'gas', 'restaurant', 'pharmacy']),
            "hour": random.randint(8, 20),
            "day_of_week": random.randint(0, 4),
            "location": "normal_area"
        }
        transactions.append(transaction)
    
    return transactions

def test_ml_service():
    """Test the ML service with sample data"""
    base_url = "http://localhost:8000"
    
    # 1. Check service health
    response = requests.get(f"{base_url}/")
    print("Service Status:", response.json()['message'])
    
    # 2. Train a user model
    user_id = "USER_TEST_001"
    transactions = generate_sample_transactions(user_id, 25)
    
    training_data = {
        "user_id": user_id,
        "transactions": transactions
    }
    
    response = requests.post(f"{base_url}/train-user", json=training_data)
    print("Training Result:", response.json()['message'])
    
    # 3. Test fraud detection - Normal transaction
    normal_txn = {
        "user_id": user_id,
        "amount": 75.0,
        "merchant_category": "grocery",
        "hour": 18,
        "day_of_week": 2
    }
    
    response = requests.post(f"{base_url}/detect-fraud", json=normal_txn)
    result = response.json()
    print(f"Normal Transaction: {result['risk_level']} (score: {result['anomaly_score']:.3f})")
    
    # 4. Test fraud detection - Suspicious transaction
    suspicious_txn = {
        "user_id": user_id,
        "amount": 2500.0,
        "merchant_category": "electronics",
        "hour": 3,
        "day_of_week": 6
    }
    
    response = requests.post(f"{base_url}/detect-fraud", json=suspicious_txn)
    result = response.json()
    print(f"Suspicious Transaction: {result['risk_level']} (score: {result['anomaly_score']:.3f})")
    print(f"Explanation: {result['explanation'][0][:60]}...")

if __name__ == "__main__":
    test_ml_service()
