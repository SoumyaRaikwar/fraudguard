import requests

BASE_URL = "http://localhost:8000"

# Sample data to train
train_data = {
    "user_id": "TEST_USER",
    "transactions": [
        {"user_id": "TEST_USER", "amount": 50, "merchant_category": "grocery", "hour": 18, "day_of_week": 1},
        {"user_id": "TEST_USER", "amount": 75, "merchant_category": "gas", "hour": 8, "day_of_week": 2},
        {"user_id": "TEST_USER", "amount": 120, "merchant_category": "restaurant", "hour": 19, "day_of_week": 3},
        {"user_id": "TEST_USER", "amount": 45, "merchant_category": "grocery", "hour": 17, "day_of_week": 4},
        {"user_id": "TEST_USER", "amount": 89, "merchant_category": "pharmacy", "hour": 16, "day_of_week": 5},
        {"user_id": "TEST_USER", "amount": 67, "merchant_category": "grocery", "hour": 18, "day_of_week": 1},
        {"user_id": "TEST_USER", "amount": 134, "merchant_category": "retail", "hour": 15, "day_of_week": 6},
        {"user_id": "TEST_USER", "amount": 56, "merchant_category": "gas", "hour": 9, "day_of_week": 1},
        {"user_id": "TEST_USER", "amount": 98, "merchant_category": "restaurant", "hour": 20, "day_of_week": 5},
        {"user_id": "TEST_USER", "amount": 43, "merchant_category": "grocery", "hour": 17, "day_of_week": 2},
        {"user_id": "TEST_USER", "amount": 87, "merchant_category": "pharmacy", "hour": 11, "day_of_week": 3},
        {"user_id": "TEST_USER", "amount": 156, "merchant_category": "retail", "hour": 14, "day_of_week": 4},
        {"user_id": "TEST_USER", "amount": 62, "merchant_category": "gas", "hour": 8, "day_of_week": 5},
        {"user_id": "TEST_USER", "amount": 91, "merchant_category": "restaurant", "hour": 19, "day_of_week": 6},
        {"user_id": "TEST_USER", "amount": 38, "merchant_category": "grocery", "hour": 18, "day_of_week": 0}
    ]
}

# Sample test transactions
test_cases = [
    {
        "name": "Normal Transaction",
        "data": {"user_id": "TEST_USER", "amount": 75.50, "merchant_category": "grocery", "hour": 18, "day_of_week": 2}
    },
    {
        "name": "High Amount Suspicious",
        "data": {"user_id": "TEST_USER", "amount": 2300.00, "merchant_category": "electronics", "hour": 2, "day_of_week": 3}
    },
    {
        "name": "Late Night Suspicious",
        "data": {"user_id": "TEST_USER", "amount": 150.00, "merchant_category": "restaurant", "hour": 1, "day_of_week": 5}
    },
    {
        "name": "High Risk Category",
        "data": {"user_id": "TEST_USER", "amount": 500.00, "merchant_category": "jewelry", "hour": 14, "day_of_week": 2}
    }
]

def test_fraudguard():
    print("🛡️ Testing FraudGuard ML Service")
    print("=" * 40)
    
    # 1. Test service health
    try:
        response = requests.get(f"{BASE_URL}/")
        print(f"✅ Service Status: {response.json()['status']}")
    except Exception as e:
        print(f"❌ Service not accessible: {e}")
        return
    
    # 2. Train user
    print(f"\n🧠 Training user: {train_data['user_id']}")
    try:
        response = requests.post(f"{BASE_URL}/train-user", json=train_data)
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Training successful!")
            print(f"   - Model trained: {result['model_trained']}")
            print(f"   - Ensemble enabled: {result['ensemble_trained']}")
            print(f"   - SHAP enabled: {result['shap_enabled']}")
        else:
            print(f"❌ Training failed: {response.text}")
            return
    except Exception as e:
        print(f"❌ Training error: {e}")
        return
    
    # 3. Test fraud detection
    print(f"\n🕵️ Testing Fraud Detection:")
    for test_case in test_cases:
        print(f"\n   Testing: {test_case['name']}")
        try:
            response = requests.post(f"{BASE_URL}/detect-fraud", json=test_case['data'])
            if response.status_code == 200:
                result = response.json()
                risk_emoji = "🚨" if result['is_suspicious'] else "✅"
                print(f"   {risk_emoji} Risk Level: {result['risk_level']}")
                print(f"   📊 Anomaly Score: {result['anomaly_score']:.3f}")
                print(f"   💡 Key Explanation: {result['explanation'][0] if result['explanation'] else 'N/A'}")
            else:
                print(f"   ❌ Detection failed: {response.text}")
        except Exception as e:
            print(f"   ❌ Detection error: {e}")
    
    # 4. Check monitoring
    print(f"\n📊 Checking Monitoring:")
    try:
        response = requests.get(f"{BASE_URL}/monitoring/metrics")
        if response.status_code == 200:
            metrics = response.json()
            print(f"   📈 Total Requests: {metrics['total_requests']}")
            print(f"   🚨 Fraud Detected: {metrics['fraud_detected']}")
            print(f"   📊 Fraud Rate: {metrics['fraud_rate']:.1%}")
            print(f"   ⚡ Avg Response Time: {metrics['avg_response_time_ms']:.1f}ms")
        else:
            print(f"   ❌ Monitoring failed: {response.text}")
    except Exception as e:
        print(f"   ❌ Monitoring error: {e}")
    
    print(f"\n🎉 Testing completed!")
    print("📊 Visit http://localhost:8000/dashboard for visual monitoring")

if __name__ == "__main__":
    test_fraudguard()
