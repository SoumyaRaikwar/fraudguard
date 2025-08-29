import requests
import json

base_url = 'http://127.0.0.1:8000'

# Test Health Endpoint
response = requests.get(base_url + '/')
print(f'Health Endpoint responded with status: {response.status_code}')
print(response.json())

# Test Learn Endpoint
user_id = 'TESTUSER001'
transactions = [
    {"user_id": user_id, "amount": 20 + i, "merchant_category": "grocery", "hour": 10, "day_of_week": 2} for i in range(20)
]

response = requests.post(f'{base_url}/learn-user?user_id={user_id}', json=transactions)
print(f'Learn-user Endpoint responded with status: {response.status_code}')
print(response.json())

# Test Detect Endpoint Normal Transaction
normal_txn = {"user_id": user_id, "amount": 22, "merchant_category": "grocery", "hour": 10, "day_of_week": 2}
response = requests.post(f'{base_url}/detect-fraud', json=normal_txn)
print(f'Detect-fraud (normal) responded with status: {response.status_code}')
print(response.json())

# Test Detect Endpoint Suspicious Transaction
suspicious_txn = {"user_id": user_id, "amount": 1000, "merchant_category": "electronics", "hour": 3, "day_of_week": 6}
response = requests.post(f'{base_url}/detect-fraud', json=suspicious_txn)
print(f'Detect-fraud (suspicious) responded with status: {response.status_code}')
print(response.json())
