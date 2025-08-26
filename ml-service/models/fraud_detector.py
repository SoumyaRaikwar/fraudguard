import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.neural_network import MLPClassifier
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import cross_val_score
import joblib
import logging
from typing import Dict, List, Tuple
from datetime import datetime

logger = logging.getLogger("fraudguard_ml")

class EnsembleFraudDetector:
    """Advanced ensemble fraud detection combining multiple ML models and business rules"""
    
    def __init__(self):
        self.models = {
            'isolation_forest': None,  # Already exists
            'random_forest': RandomForestClassifier(
                n_estimators=200, 
                max_depth=10, 
                random_state=42,
                class_weight='balanced'
            ),
            'gradient_boost': GradientBoostingClassifier(
                n_estimators=100,
                learning_rate=0.1,
                max_depth=5,
                random_state=42
            ),
            'neural_network': MLPClassifier(
                hidden_layer_sizes=(100, 50),
                max_iter=1000,
                random_state=42,
                early_stopping=True
            ),
            'logistic_regression': LogisticRegression(
                random_state=42,
                class_weight='balanced',
                max_iter=1000
            )
        }
        
        self.scalers = {}
        self.model_weights = {
            'isolation_forest': 0.3,
            'random_forest': 0.25,
            'gradient_boost': 0.2,
            'neural_network': 0.15,
            'logistic_regression': 0.1
        }
        
        self.business_rules_weight = 0.2
        self.ensemble_threshold = 0.6
        
    def prepare_training_data(self, transactions: List[Dict], labels: List[int] = None) -> Tuple[np.ndarray, np.ndarray]:
        """Prepare data for supervised learning with synthetic fraud labels"""
        df = pd.DataFrame(transactions)
        
        # Extract features
        features = []
        fraud_labels = []
        
        for i, (_, row) in enumerate(df.iterrows()):
            feature_vector = [
                row['amount'],
                row['hour'],
                row['day_of_week'],
                hash(row['merchant_category']) % 1000,
                1 if row['day_of_week'] in [5, 6] else 0,  # Weekend
                1 if row['hour'] in [22, 23, 0, 1, 2, 3, 4, 5] else 0,  # Night
                len(str(row['merchant_category'])),  # Category length
                1 if row['amount'] > 1000 else 0,  # High amount flag
            ]
            features.append(feature_vector)
            
            # Generate synthetic fraud labels based on suspicious patterns
            if labels and i < len(labels):
                fraud_labels.append(labels[i])
            else:
                # Synthetic labeling based on known fraud indicators
                is_fraud = (
                    row['amount'] > 2000 and 
                    row['hour'] in [0, 1, 2, 3, 4] and 
                    row['merchant_category'] in ['electronics', 'jewelry', 'travel']
                )
                fraud_labels.append(1 if is_fraud else 0)
        
        return np.array(features), np.array(fraud_labels)
    
    def train_ensemble(self, user_id: str, transactions: List[Dict], labels: List[int] = None):
        """Train all models in the ensemble"""
        logger.info(f"🤖 Training ensemble models for {user_id}")
        
        X, y = self.prepare_training_data(transactions, labels)
        
        # Scale features
        scaler = StandardScaler()
        X_scaled = scaler.fit_transform(X)
        self.scalers[user_id] = scaler
        
        trained_models = {}
        
        # Train supervised models (skip isolation forest as it's already trained)
        for name, model in self.models.items():
            if name == 'isolation_forest':
                continue
                
            try:
                logger.info(f"  Training {name}...")
                model.fit(X_scaled, y)
                
                # Cross-validation score
                cv_score = cross_val_score(model, X_scaled, y, cv=3, scoring='f1').mean()
                logger.info(f"  {name} CV F1-score: {cv_score:.3f}")
                
                trained_models[name] = model
                
            except Exception as e:
                logger.warning(f"  Failed to train {name}: {e}")
        
        self.models.update(trained_models)
        
        # Calculate fraud rate for user
        fraud_rate = np.mean(y)
        logger.info(f"✅ Ensemble training completed. Fraud rate: {fraud_rate:.1%}")
        
        return {
            'models_trained': list(trained_models.keys()),
            'fraud_rate': fraud_rate,
            'total_transactions': len(transactions)
        }
    
    def predict_ensemble(self, user_id: str, transaction: Dict, isolation_score: float = None) -> Dict:
        """Make ensemble prediction combining all models"""
        
        # Extract features
        features = [
            transaction['amount'],
            transaction['hour'],
            transaction['day_of_week'],
            hash(transaction['merchant_category']) % 1000,
            1 if transaction['day_of_week'] in [5, 6] else 0,
            1 if transaction['hour'] in [22, 23, 0, 1, 2, 3, 4, 5] else 0,
            len(str(transaction['merchant_category'])),
            1 if transaction['amount'] > 1000 else 0,
        ]
        
        # Scale features
        if user_id in self.scalers:
            features_scaled = self.scalers[user_id].transform([features])
        else:
            features_scaled = np.array([features])
        
        model_predictions = {}
        model_probabilities = {}
        
        # Get predictions from all models
        for name, model in self.models.items():
            if name == 'isolation_forest':
                if isolation_score is not None:
                    # Use provided isolation forest score
                    model_predictions[name] = 1 if isolation_score > 0.6 else 0
                    model_probabilities[name] = isolation_score
                continue
            
            try:
                if hasattr(model, 'predict_proba'):
                    proba = model.predict_proba(features_scaled)[0]
                    fraud_proba = proba[1] if len(proba) > 1 else proba[0]
                    model_probabilities[name] = fraud_proba
                    model_predictions[name] = 1 if fraud_proba > 0.5 else 0
                else:
                    pred = model.predict(features_scaled)[0]
                    model_predictions[name] = pred
                    model_probabilities[name] = float(pred)
                    
            except Exception as e:
                logger.warning(f"Prediction failed for {name}: {e}")
                model_predictions[name] = 0
                model_probabilities[name] = 0.0
        
        # Calculate weighted ensemble score
        ensemble_score = 0.0
        total_weight = 0.0
        
        for name, weight in self.model_weights.items():
            if name in model_probabilities:
                ensemble_score += weight * model_probabilities[name]
                total_weight += weight
        
        # Normalize
        if total_weight > 0:
            ensemble_score /= total_weight
        
        # Business rules adjustment
        business_score = self._apply_advanced_business_rules(transaction)
        final_score = (ensemble_score * 0.8) + (business_score * 0.2)
        
        # Determine final prediction
        is_fraud = final_score > self.ensemble_threshold
        
        # Risk level
        if final_score > 0.9:
            risk_level = "CRITICAL"
        elif final_score > 0.7:
            risk_level = "HIGH"
        elif final_score > 0.5:
            risk_level = "MEDIUM"
        else:
            risk_level = "LOW"
        
        return {
            'ensemble_score': final_score,
            'is_fraud': is_fraud,
            'risk_level': risk_level,
            'model_predictions': model_predictions,
            'model_probabilities': model_probabilities,
            'business_rules_score': business_score,
            'confidence': abs(final_score - 0.5) * 2
        }
    
    def _apply_advanced_business_rules(self, transaction: Dict) -> float:
        """Apply comprehensive business rules"""
        score = 0.0
        
        amount = transaction['amount']
        hour = transaction['hour']
        day = transaction['day_of_week']
        category = transaction['merchant_category']
        
        # Amount-based rules
        if amount > 10000:
            score += 0.4
        elif amount > 5000:
            score += 0.2
        elif amount > 2000:
            score += 0.1
        
        # Time-based rules
        if hour in [0, 1, 2, 3, 4, 5]:  # Late night
            score += 0.3
        elif hour in [22, 23]:  # Very late evening
            score += 0.1
        
        # Category-based rules
        high_risk_categories = ['electronics', 'jewelry', 'travel', 'cash_advance', 'gambling']
        medium_risk_categories = ['online', 'retail', 'entertainment']
        
        if category in high_risk_categories:
            score += 0.3
        elif category in medium_risk_categories:
            score += 0.1
        
        # Weekend late night combination
        if day in [5, 6] and hour in [22, 23, 0, 1, 2]:
            score += 0.2
        
        # Round amounts (potential testing)
        if amount % 100 == 0 and amount > 500:
            score += 0.1
        
        return min(1.0, score)
    
    def get_ensemble_explanation(self, prediction_result: Dict, transaction: Dict) -> List[str]:
        """Generate detailed ensemble explanations"""
        explanations = []
        
        # Model agreement analysis
        predictions = prediction_result['model_predictions']
        fraud_votes = sum(predictions.values())
        total_votes = len(predictions)
        
        if fraud_votes > total_votes * 0.7:
            explanations.append(f"🚨 Strong consensus: {fraud_votes}/{total_votes} models flagged as fraud")
        elif fraud_votes > total_votes * 0.5:
            explanations.append(f"⚠️ Moderate consensus: {fraud_votes}/{total_votes} models flagged as fraud")
        else:
            explanations.append(f"✅ Low risk: Only {fraud_votes}/{total_votes} models flagged as fraud")
        
        # Individual model insights
        probabilities = prediction_result['model_probabilities']
        top_model = max(probabilities.keys(), key=lambda x: probabilities[x])
        explanations.append(f"🎯 Strongest signal from: {top_model} ({probabilities[top_model]:.3f})")
        
        # Business rules contribution
        business_score = prediction_result['business_rules_score']
        if business_score > 0.5:
            explanations.append(f"📋 Business rules strongly support fraud ({business_score:.3f})")
        elif business_score > 0.2:
            explanations.append(f"📋 Business rules moderately support fraud ({business_score:.3f})")
        
        return explanations
