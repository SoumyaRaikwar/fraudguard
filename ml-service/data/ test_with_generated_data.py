import requests
import pandas as pd
from data.advanced_generator import AdvancedDataGenerator

def test_ml_service_with_realistic_data():
    """Test ML service with realistic generated data"""
    base_url = "http://localhost:8000"
    generator = AdvancedDataGenerator()
    
    # 1. Check service health
    response = requests.get(f"{base_url}/")
    print("🛡️ FraudGuard ML Service Status:", response.json()['message'])
    
    # 2. Generate training data for a professional user
    user_id = "USER_PROFESSIONAL_TEST"
    training_transactions = generator.generate_user_transactions(user_id, "professional", 25)
    
    # 3. Train the user model
    training_data = {
        "user_id": user_id,
        "transactions": training_transactions
    }
    
    print(f"\n🧠 Training model for {user_id}...")
    response = requests.post(f"{base_url}/train-user", json=training_data)
    
    if response.status_code == 200:
        result = response.json()
        profile = result['profile']
        print(f"   ✅ Training successful!")
        print(f"   📊 Profile: {profile['total_transactions']} transactions, avg=${profile['avg_amount']:.2f}")
    else:
        print(f"   ❌ Training failed: {response.text}")
        return
    
    # 4. Test normal transaction detection
    normal_txn = {
        "user_id": user_id,
        "amount": 180.0,  # Close to professional average
        "merchant_category": "restaurant",  # Common for professionals
        "hour": 19,  # Evening
        "day_of_week": 3,  # Thursday
        "location": "normal_area"
    }
    
    print(f"\n✅ Testing normal transaction...")
    response = requests.post(f"{base_url}/detect-fraud", json=normal_txn)
    result = response.json()
    print(f"   Result: {result['risk_level']} (score: {result['anomaly_score']:.3f})")
    
    # 5. Test suspicious transaction detection
    suspicious_txn = {
        "user_id": user_id,
        "amount": 3000.0,  # Very high for professional
        "merchant_category": "jewelry",  # High-risk category
        "hour": 2,  # 2 AM
        "day_of_week": 6,  # Sunday
        "location": "suspicious_area"
    }
    
    print(f"\n🚨 Testing suspicious transaction...")
    response = requests.post(f"{base_url}/detect-fraud", json=suspicious_txn)
    result = response.json()
    print(f"   Result: {result['risk_level']} (score: {result['anomaly_score']:.3f})")
    print(f"   Explanation: {result['explanation'][0][:60]}...")
    
    print(f"\n🎉 Realistic data testing completed!")

if __name__ == "__main__":
    test_ml_service_with_realistic_data()
