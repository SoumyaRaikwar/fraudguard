
import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
from typing import List, Dict

class BehavioralTwin:
    def __init__(self):
        self.user_profiles = {}
        self.models = {}
        self.scalers = {}

    def create_profile(self, user_id: str, transactions: List[Dict]):
        df = pd.DataFrame(transactions)

        # Calculate basic user stats
        profile = {
            'user_id': user_id,
            'transaction_count': len(transactions),
            'avg_amount': df['amount'].mean(),
            'std_amount': df['amount'].std(),
            'median_amount': df['amount'].median(),
            'max_amount': df['amount'].max(),
            'common_categories': df['merchant_category'].value_counts().head(5).to_dict(),
            'active_hours': df['hour'].value_counts().head(8).index.tolist(),
            'active_days': df['day_of_week'].value_counts().index.tolist(),
        }

        features = self._extract_features(df)
        scaler = StandardScaler()
        scaled_features = scaler.fit_transform(features)

        model = IsolationForest(contamination=0.1, n_estimators=100, random_state=42)
        model.fit(scaled_features)

        self.user_profiles[user_id] = profile
        self.models[user_id] = model
        self.scalers[user_id] = scaler

        return profile

    def _extract_features(self, df: pd.DataFrame) -> np.ndarray:
        features = []
        for _, row in df.iterrows():
            features.append([
                row['amount'],
                row['hour'],
                row['day_of_week'],
                hash(row['merchant_category']) % 10000
            ])
        return np.array(features)

    def detect_anomaly(self, user_id: str, transaction: Dict) -> Dict:
        if user_id not in self.models:
            return {
                'anomaly_score': 0,
                'is_anomaly': False,
                'explanation': ['User not trained'],
            }

        model = self.models[user_id]
        scaler = self.scalers[user_id]
        profile = self.user_profiles[user_id]

        features = self._extract_single_feature(transaction)
        scaled_features = scaler.transform([features])

        score = model.decision_function(scaled_features)[0]
        normalized_score = max(0, min(1, (0.5 - score)))

        explanation = []

        if transaction['amount'] > profile['avg_amount'] + 2 * profile['std_amount']:
            explanation.append('Amount unusually high')
        if transaction['hour'] not in profile['active_hours'][:5]:
            explanation.append('Transaction time unusual')
        if transaction['merchant_category'] not in profile['common_categories']:
            explanation.append('Merchant category unusual')

        return {
            'anomaly_score': normalized_score,
            'is_anomaly': normalized_score > 0.6,
            'explanation': explanation
        }

    def _extract_single_feature(self, transaction: Dict) -> List[float]:
        return [
            transaction['amount'],
            transaction['hour'],
            transaction['day_of_week'],
            hash(transaction['merchant_category']) % 10000
        ]
