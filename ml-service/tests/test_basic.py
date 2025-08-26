
import requests

BASE_URL = "http://localhost:8000"

def test_health_check():
    resp = requests.get(f"{BASE_URL}/")
    assert resp.status_code == 200
    print("✅ Health check passed")

def test_docs_available():
    resp = requests.get(f"{BASE_URL}/docs")
    assert resp.status_code == 200
    print("✅ Swagger docs available")

def test_training_endpoint():
    user_id = "TEST_USER"
    transactions = [
        {
            "user_id": user_id,
            "amount": 100 + i,
            "merchant_category": "grocery",
            "hour": 12,
            "day_of_week": 1
        }
        for i in range(15)
    ]

    training_data = {
        "user_id": user_id,
        "transactions": transactions
    }

    resp = requests.post(f"{BASE_URL}/train-user", json=training_data)
    assert resp.status_code == 200
    print("✅ Training endpoint passed")

    # Test detect-fraud endpoint
    test_txn = {
        "user_id": user_id,
        "amount": 5000,
        "merchant_category": "electronics",
        "hour": 2,
        "day_of_week": 6
    }
    resp = requests.post(f"{BASE_URL}/detect-fraud", json=test_txn)
    assert resp.status_code == 200
    print(f"✅ Detect fraud endpoint passed with risk level {resp.json()['risk_level']}")

if __name__ == '__main__':
    test_health_check()
    test_docs_available()
    test_training_endpoint()
    print("All basic tests passed successfully!")
