
from faker import Faker
import random
import pandas as pd
import numpy as np
from datetime import datetime

fake = Faker()

class AdvancedDataGenerator:
    def __init__(self):
        self.personas = {
            'student': {'base_amount': 50, 'categories': ['grocery', 'restaurant', 'gas'], 'fraud_rate': 0.03},
            'professional': {'base_amount': 200, 'categories': ['restaurant', 'retail', 'gas', 'grocery'], 'fraud_rate': 0.05},
            'family': {'base_amount': 150, 'categories': ['grocery', 'pharmacy', 'retail'], 'fraud_rate': 0.07},
            'high_roller': {'base_amount': 500, 'categories': ['restaurant', 'travel', 'luxury'], 'fraud_rate': 0.15},
        }

    def generate_user_transactions(self, user_id, persona='professional', n=30):
        config = self.personas.get(persona, self.personas['professional'])
        base = config['base_amount']
        cats = config['categories']
        fraud_rate = config['fraud_rate']

        transactions = []
        frauds = 0
        for i in range(n):
            is_fraud = (random.random() < fraud_rate)
            if is_fraud:
                amount = base * random.uniform(5, 15)
                cat = random.choice(['electronics', 'jewelry', 'travel', 'cash_advance'])
                hour = random.choice([1, 2, 3, 4, 23])
                day = random.choice([5, 6])
                location = 'suspicious_area'
                frauds += 1
            else:
                amount = max(5, random.gauss(base, base*0.3))
                cat = random.choice(cats)
                hour = random.choice(range(7, 22))
                day = random.choice(range(0, 5))
                location = 'normal_area'
            transaction = {
                'user_id': user_id,
                'amount': round(amount, 2),
                'merchant_category': cat,
                'hour': hour,
                'day': day,
                'location': location,
                'is_fraud': is_fraud,
                'timestamp': datetime.now().isoformat()
            }
            transactions.append(transaction)
        print(f"Generated {n} transactions for {user_id} ({frauds} fraudulent)")
        return transactions

if __name__ == '__main__':
    gen = AdvancedDataGenerator()
    user_txns = gen.generate_user_transactions('USER_001', 'professional', 30)
    df = pd.DataFrame(user_txns)
    df.to_csv('advanced_sample.csv', index=False)
    print('Sample data saved to advanced_sample.csv')
