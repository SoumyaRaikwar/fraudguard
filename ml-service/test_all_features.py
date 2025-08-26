import requests
import time
import random
import json

BASE_URL = "http://localhost:8000"

def test_all_features():
    """Comprehensive test of all FraudGuard features"""
    
    print("🛡️ FRAUDGUARD ML COMPREHENSIVE TESTING")
    print("=" * 50)
    
    # 1. Test service health
    print("\n1️⃣ Testing Service Health...")
    response = requests.get(f"{BASE_URL}/")
    print(f"   Status: {response.status_code}")
    print(f"   Version: {response.json()['version']}")
    
    # 2. Train multiple user types
    print("\n2️⃣ Training Different User Profiles...")
    
    training_datasets = [
        {
            "user_id": "NORMAL_USER_001",
            "transactions": [
                {"user_id": "NORMAL_USER_001", "amount": 45.50, "merchant_category": "grocery", "hour": 18, "day_of_week": 1},
                {"user_id": "NORMAL_USER_001", "amount": 67.80, "merchant_category": "gas", "hour": 8, "day_of_week": 2},
                {"user_id": "NORMAL_USER_001", "amount": 89.20, "merchant_category": "restaurant", "hour": 19, "day_of_week": 3},
                {"user_id": "NORMAL_USER_001", "amount": 34.75, "merchant_category": "grocery", "hour": 17, "day_of_week": 4},
                {"user_id": "NORMAL_USER_001", "amount": 56.40, "merchant_category": "pharmacy", "hour": 16, "day_of_week": 5},
                {"user_id": "NORMAL_USER_001", "amount": 78.90, "merchant_category": "grocery", "hour": 18, "day_of_week": 1},
                {"user_id": "NORMAL_USER_001", "amount": 123.45, "merchant_category": "retail", "hour": 15, "day_of_week": 6},
                {"user_id": "NORMAL_USER_001", "amount": 41.20, "merchant_category": "gas", "hour": 9, "day_of_week": 1},
                {"user_id": "NORMAL_USER_001", "amount": 95.30, "merchant_category": "restaurant", "hour": 20, "day_of_week": 5},
                {"user_id": "NORMAL_USER_001", "amount": 52.15, "merchant_category": "grocery", "hour": 17, "day_of_week": 2},
                {"user_id": "NORMAL_USER_001", "amount": 69.85, "merchant_category": "pharmacy", "hour": 11, "day_of_week": 3},
                {"user_id": "NORMAL_USER_001", "amount": 144.60, "merchant_category": "retail", "hour": 14, "day_of_week": 4},
                {"user_id": "NORMAL_USER_001", "amount": 38.90, "merchant_category": "gas", "hour": 8, "day_of_week": 5},
                {"user_id": "NORMAL_USER_001", "amount": 87.25, "merchant_category": "restaurant", "hour": 19, "day_of_week": 6},
                {"user_id": "NORMAL_USER_001", "amount": 43.70, "merchant_category": "grocery", "hour": 18, "day_of_week": 0}
            ]
        },
        {
            "user_id": "BUSINESS_USER_001",
            "transactions": [
                {"user_id": "BUSINESS_USER_001", "amount": 450.00, "merchant_category": "office_supplies", "hour": 10, "day_of_week": 1},
                {"user_id": "BUSINESS_USER_001", "amount": 780.50, "merchant_category": "restaurant", "hour": 12, "day_of_week": 2},
                {"user_id": "BUSINESS_USER_001", "amount": 1250.75, "merchant_category": "travel", "hour": 14, "day_of_week": 3},
                {"user_id": "BUSINESS_USER_001", "amount": 320.00, "merchant_category": "gas", "hour": 9, "day_of_week": 4},
                {"user_id": "BUSINESS_USER_001", "amount": 890.25, "merchant_category": "electronics", "hour": 11, "day_of_week": 5},
                {"user_id": "BUSINESS_USER_001", "amount": 567.80, "merchant_category": "office_supplies", "hour": 10, "day_of_week": 1},
                {"user_id": "BUSINESS_USER_001", "amount": 1100.00, "merchant_category": "hotel", "hour": 16, "day_of_week": 2},
                {"user_id": "BUSINESS_USER_001", "amount": 234.50, "merchant_category": "restaurant", "hour": 13, "day_of_week": 3},
                {"user_id": "BUSINESS_USER_001", "amount": 675.30, "merchant_category": "retail", "hour": 15, "day_of_week": 4},
                {"user_id": "BUSINESS_USER_001", "amount": 445.90, "merchant_category": "gas", "hour": 8, "day_of_week": 5},
                {"user_id": "BUSINESS_USER_001", "amount": 1350.00, "merchant_category": "travel", "hour": 14, "day_of_week": 1},
                {"user_id": "BUSINESS_USER_001", "amount": 289.75, "merchant_category": "restaurant", "hour": 12, "day_of_week": 2},
                {"user_id": "BUSINESS_USER_001", "amount": 756.40, "merchant_category": "electronics", "hour": 11, "day_of_week": 3},
                {"user_id": "BUSINESS_USER_001", "amount": 398.20, "merchant_category": "office_supplies", "hour": 10, "day_of_week": 4},
                {"user_id": "BUSINESS_USER_001", "amount": 823.15, "merchant_category": "hotel", "hour": 16, "day_of_week": 5}
            ]
        }
    ]
    
    for dataset in training_datasets:
        response = requests.post(f"{BASE_URL}/train-user", json=dataset)
        if response.status_code == 200:
            print(f"   ✅ Trained {dataset['user_id']}")
        else:
            print(f"   ❌ Failed to train {dataset['user_id']}: {response.text}")
    
    # 3. Test different fraud scenarios
    print("\n3️⃣ Testing Fraud Detection Scenarios...")
    
    test_cases = [
        {"name": "Normal Transaction", "data": {"user_id": "NORMAL_USER_001", "amount": 62.40, "merchant_category": "grocery", "hour": 18, "day_of_week": 2}},
        {"name": "Suspicious Amount", "data": {"user_id": "NORMAL_USER_001", "amount": 2800.00, "merchant_category": "grocery", "hour": 18, "day_of_week": 2}},
        {"name": "Suspicious Time", "data": {"user_id": "NORMAL_USER_001", "amount": 75.00, "merchant_category": "grocery", "hour": 3, "day_of_week": 2}},
        {"name": "Suspicious Category", "data": {"user_id": "NORMAL_USER_001", "amount": 85.00, "merchant_category": "gambling", "hour": 18, "day_of_week": 2}},
        {"name": "Multiple Red Flags", "data": {"user_id": "NORMAL_USER_001", "amount": 3500.00, "merchant_category": "jewelry", "hour": 2, "day_of_week": 6}},
        {"name": "Critical Risk", "data": {"user_id": "BUSINESS_USER_001", "amount": 15000.00, "merchant_category": "cash_advance", "hour": 4, "day_of_week": 0}}
    ]
    
    for test_case in test_cases:
        response = requests.post(f"{BASE_URL}/detect-fraud", json=test_case["data"])
        if response.status_code == 200:
            result = response.json()
            print(f"   {test_case['name']}: {result['risk_level']} ({result['anomaly_score']:.3f})")
        else:
            print(f"   ❌ {test_case['name']}: Failed")
    
    # 4. Test monitoring endpoints
    print("\n4️⃣ Testing Monitoring & Analytics...")
    
    endpoints = ["/monitoring/metrics", "/monitoring/health", "/monitoring/dashboard", "/health"]
    for endpoint in endpoints:
        response = requests.get(f"{BASE_URL}{endpoint}")
        print(f"   {endpoint}: {'✅' if response.status_code == 200 else '❌'}")
    
    # 5. Load testing
    print("\n5️⃣ Load Testing (50 transactions)...")
    
    users = ["NORMAL_USER_001", "BUSINESS_USER_001"]
    fraud_count = 0
    
    for i in range(50):
        user = random.choice(users)
        
        if i % 10 == 0:  # 10% fraudulent
            transaction = {
                "user_id": user,
                "amount": random.uniform(3000, 8000),
                "merchant_category": random.choice(["electronics", "jewelry", "gambling"]),
                "hour": random.choice([1, 2, 3, 4]),
                "day_of_week": random.choice([5, 6])
            }
        else:  # 90% normal
            if user == "NORMAL_USER_001":
                transaction = {
                    "user_id": user,
                    "amount": random.uniform(30, 150),
                    "merchant_category": random.choice(["grocery", "gas", "restaurant"]),
                    "hour": random.randint(8, 20),
                    "day_of_week": random.randint(0, 4)
                }
            else:  # BUSINESS_USER_001
                transaction = {
                    "user_id": user,
                    "amount": random.uniform(200, 1200),
                    "merchant_category": random.choice(["restaurant", "travel", "office_supplies"]),
                    "hour": random.randint(9, 17),
                    "day_of_week": random.randint(0, 4)
                }
        
        response = requests.post(f"{BASE_URL}/detect-fraud", json=transaction)
        if response.status_code == 200:
            result = response.json()
            if result["is_suspicious"]:
                fraud_count += 1
            print(f"   Transaction {i+1}: {result['risk_level']} ({'FRAUD' if result['is_suspicious'] else 'SAFE'})")
        
        time.sleep(0.1)  # Small delay
    
    print(f"\n   📊 Load Test Results: {fraud_count}/50 flagged as fraud ({fraud_count/50*100:.1f}%)")
    
    # 6. Final metrics check
    print("\n6️⃣ Final System Metrics...")
    response = requests.get(f"{BASE_URL}/monitoring/dashboard")
    if response.status_code == 200:
        dashboard = response.json()
        overview = dashboard["system_overview"]
        print(f"   Total Requests: {overview['total_requests']}")
        print(f"   Fraud Rate: {overview['fraud_rate']:.1%}")
        print(f"   Avg Response Time: {overview['avg_response_time_ms']:.1f}ms")
        print(f"   Active Alerts: {len(overview['active_alerts'])}")
    
    print("\n🎉 COMPREHENSIVE TESTING COMPLETED!")
    print("=" * 50)

if __name__ == "__main__":
    test_all_features()
