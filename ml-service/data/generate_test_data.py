from data.advanced_generator import AdvancedDataGenerator
import pandas as pd

def create_test_dataset():
    """Generate comprehensive test dataset"""
    generator = AdvancedDataGenerator()
    
    all_transactions = []
    
    # Generate users with different personas
    test_users = [
        ("USER_STUDENT_001", "student", 25),
        ("USER_STUDENT_002", "student", 20),
        ("USER_PROFESSIONAL_001", "professional", 35),
        ("USER_PROFESSIONAL_002", "professional", 30),
        ("USER_FAMILY_001", "family", 40),
        ("USER_FAMILY_002", "family", 35),
        ("USER_HIGHROLLER_001", "high_roller", 25),
    ]
    
    for user_id, persona, num_txns in test_users:
        print(f"\n🎭 Generating data for {user_id} ({persona})...")
        user_transactions = generator.generate_user_transactions(user_id, persona, num_txns)
        all_transactions.extend(user_transactions)
    
    # Save comprehensive dataset
    df = pd.DataFrame(all_transactions)
    df.to_csv('comprehensive_test_data.csv', index=False)
    
    # Print summary
    total_fraud = df['is_fraud'].sum()
    fraud_rate = total_fraud / len(df) * 100
    
    print(f"\n📊 Dataset Summary:")
    print(f"   Total Users: {len(test_users)}")
    print(f"   Total Transactions: {len(df)}")
    print(f"   Fraudulent Transactions: {total_fraud}")
    print(f"   Fraud Rate: {fraud_rate:.1f}%")
    print(f"   Saved to: comprehensive_test_data.csv")

if __name__ == "__main__":
    create_test_dataset()
