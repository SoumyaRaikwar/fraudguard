import requests
import random
import time
from datetime import datetime

BASE_URL = 'http://localhost:8000'

def test_network_endpoints():
    """Test the network/cycles endpoint"""
    print("🔗 Testing Network Visualization Endpoints")
    print("=" * 50)
    
    try:
        # Test cycles endpoint
        response = requests.get(f'{BASE_URL}/monitoring/cycles')
        if response.status_code == 200:
            data = response.json()
            print("✅ Cycles endpoint working")
            print(f"   Nodes: {data['statistics']['total_nodes']}")
            print(f"   Edges: {data['statistics']['total_edges']}")
            print(f"   Cycles Detected: {data['statistics']['cycles_detected']}")
            print(f"   Fraud Rings: {data['analysis_summary']['fraud_rings_detected']}")
        else:
            print(f"❌ Cycles endpoint failed: {response.status_code}")
    
    except Exception as e:
        print(f"❌ Network test failed: {e}")

def generate_network_activity():
    """Generate activity to populate network visualization"""
    print("🌐 Generating Network Activity...")
    
    # Train multiple users first
    users = ['NETWORK_USER_A', 'NETWORK_USER_B', 'NETWORK_USER_C', 'SUSPICIOUS_USER_X']
    
    for user in users:
        transactions = []
        for i in range(15):
            # Create different spending patterns for each user
            if user == 'SUSPICIOUS_USER_X':
                amount = random.uniform(1000, 5000)  # Suspicious amounts
                category = random.choice(['electronics', 'jewelry', 'travel'])
                hour = random.choice([1, 2, 3, 4])  # Suspicious times
            else:
                amount = random.uniform(20, 300)     # Normal amounts
                category = random.choice(['grocery', 'gas', 'restaurant', 'retail'])
                hour = random.randint(8, 22)        # Normal times
            
            transactions.append({
                'user_id': user,
                'amount': amount,
                'merchant_category': category,
                'hour': hour,
                'day_of_week': random.randint(0, 6)
            })
        
        # Train user
        train_data = {'user_id': user, 'transactions': transactions}
        response = requests.post(f'{BASE_URL}/train-user', json=train_data)
        
        if response.status_code == 200:
            print(f"✅ Trained {user}")
        else:
            print(f"❌ Failed to train {user}")
        
        time.sleep(0.5)
    
    # Generate transactions between users to create network relationships
    print("🔄 Creating Network Relationships...")
    
    network_transactions = [
        # Normal transaction chain
        {'user_id': 'NETWORK_USER_A', 'amount': 150, 'merchant_category': 'transfer', 'hour': 14, 'day_of_week': 2},
        {'user_id': 'NETWORK_USER_B', 'amount': 200, 'merchant_category': 'restaurant', 'hour': 19, 'day_of_week': 3},
        {'user_id': 'NETWORK_USER_C', 'amount': 100, 'merchant_category': 'grocery', 'hour': 18, 'day_of_week': 4},
        
        # Suspicious cycle
        {'user_id': 'SUSPICIOUS_USER_X', 'amount': 3000, 'merchant_category': 'electronics', 'hour': 2, 'day_of_week': 6},
        {'user_id': 'NETWORK_USER_A', 'amount': 2800, 'merchant_category': 'jewelry', 'hour': 3, 'day_of_week': 0},
        {'user_id': 'NETWORK_USER_B', 'amount': 3500, 'merchant_category': 'travel', 'hour': 1, 'day_of_week': 5},
        {'user_id': 'SUSPICIOUS_USER_X', 'amount': 5000, 'merchant_category': 'cash_advance', 'hour': 4, 'day_of_week': 6},
    ]
    
    fraud_detected = 0
    for transaction in network_transactions:
        response = requests.post(f'{BASE_URL}/detect-fraud', json=transaction)
        if response.status_code == 200:
            result = response.json()
            if result['is_suspicious']:
                fraud_detected += 1
                print(f"🚨 Fraud detected: {transaction['user_id']} - {result['risk_level']}")
            else:
                print(f"✅ Normal: {transaction['user_id']} - {result['risk_level']}")
        time.sleep(1)
    
    print(f"\n📊 Network Activity Generated:")
    print(f"   Users Trained: {len(users)}")
    print(f"   Transactions Processed: {len(network_transactions)}")
    print(f"   Fraud Detected: {fraud_detected}")
    print(f"🌐 Check network visualization at: http://localhost:8000/dashboard")

if __name__ == '__main__':
    test_network_endpoints()
    print()
    generate_network_activity()
