from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse, FileResponse
from fastapi.encoders import jsonable_encoder
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, ConfigDict
import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest, RandomForestClassifier, GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.neural_network import MLPClassifier
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import cross_val_score
import joblib
import shap
from typing import List, Dict, Optional
import logging
from datetime import datetime, timedelta
import json
import os
from collections import defaultdict, deque
import threading
import traceback

# Import the model persistence utilities
from utils.model_persistence import ModelPersistence

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("fraudguard_ml")

# Utility function to convert numpy types
def convert_numpy_types(obj):
    """Convert numpy types to native Python types recursively"""
    if isinstance(obj, np.generic):
        return obj.item()
    elif isinstance(obj, np.ndarray):
        return obj.tolist()
    elif isinstance(obj, dict):
        return {key: convert_numpy_types(value) for key, value in obj.items()}
    elif isinstance(obj, list):
        return [convert_numpy_types(item) for item in obj]
    else:
        return obj

# Monitoring System
class FraudGuardMonitor:
    """Comprehensive monitoring and alerting system"""
    
    def __init__(self):
        self.metrics = {
            'total_requests': 0,
            'fraud_detected': 0,
            'false_positives': 0,
            'processing_times': deque(maxlen=1000),
            'error_count': 0,
            'model_predictions': defaultdict(int),
            'hourly_stats': defaultdict(lambda: {'requests': 0, 'fraud': 0}),
            'user_activity': defaultdict(int),
            'high_risk_transactions': []
        }
        
        self.alerts = {
            'high_fraud_rate': False,
            'slow_response': False,
            'model_errors': False,
            'unusual_activity': False
        }
        
        self.thresholds = {
            'max_fraud_rate': 0.15,
            'max_response_time': 200,
            'max_error_rate': 0.05,
            'min_requests_for_alert': 50
        }
        
        self.logger = logging.getLogger("fraudguard_monitor")
        
    def record_transaction(self, user_id: str, result: Dict, processing_time: float):
        """Record transaction metrics"""
        self.metrics['total_requests'] += 1
        self.metrics['user_activity'][user_id] += 1
        self.metrics['processing_times'].append(processing_time)
        
        if result.get('is_suspicious', False):
            self.metrics['fraud_detected'] += 1
            
            if result.get('risk_level') in ['HIGH', 'CRITICAL']:
                self.metrics['high_risk_transactions'].append({
                    'user_id': user_id,
                    'timestamp': datetime.now().isoformat(),
                    'risk_level': result['risk_level'],
                    'anomaly_score': result.get('anomaly_score', 0)
                })
        
        hour = datetime.now().hour
        self.metrics['hourly_stats'][hour]['requests'] += 1
        if result.get('is_suspicious', False):
            self.metrics['hourly_stats'][hour]['fraud'] += 1
        
        self._check_alerts()
        
    def record_error(self, error_type: str, details: str):
        """Record system errors"""
        self.metrics['error_count'] += 1
        self.logger.error(f"Error recorded: {error_type} - {details}")
        self._check_alerts()
    
    def _check_alerts(self):
        """Check if any alert thresholds are exceeded"""
        if self.metrics['total_requests'] < self.thresholds['min_requests_for_alert']:
            return
        
        fraud_rate = self.metrics['fraud_detected'] / self.metrics['total_requests']
        if fraud_rate > self.thresholds['max_fraud_rate']:
            if not self.alerts['high_fraud_rate']:
                self._trigger_alert('high_fraud_rate', f"Fraud rate {fraud_rate:.1%} exceeds threshold")
                self.alerts['high_fraud_rate'] = True
        else:
            self.alerts['high_fraud_rate'] = False
    
    def _trigger_alert(self, alert_type: str, message: str):
        """Trigger an alert"""
        self.logger.warning(f"ALERT TRIGGERED: {alert_type} - {message}")
        print(f"🚨 ALERT: {alert_type} - {message}")
    
    def get_current_metrics(self) -> Dict:
        """Get current system metrics"""
        if self.metrics['total_requests'] > 0:
            fraud_rate = self.metrics['fraud_detected'] / self.metrics['total_requests']
            error_rate = self.metrics['error_count'] / self.metrics['total_requests']
        else:
            fraud_rate = 0
            error_rate = 0
        
        processing_times = list(self.metrics['processing_times'])
        avg_response_time = sum(processing_times) / len(processing_times) if processing_times else 0
        
        return {
            'total_requests': self.metrics['total_requests'],
            'fraud_detected': self.metrics['fraud_detected'],
            'fraud_rate': fraud_rate,
            'error_count': self.metrics['error_count'],
            'error_rate': error_rate,
            'avg_response_time_ms': avg_response_time,
            'active_alerts': [k for k, v in self.alerts.items() if v],
            'top_active_users': dict(sorted(self.metrics['user_activity'].items(), 
                                          key=lambda x: x[1], reverse=True)[:5]),
            'recent_high_risk_count': len([t for t in self.metrics['high_risk_transactions'] 
                                         if datetime.fromisoformat(t['timestamp']) > 
                                         datetime.now() - timedelta(hours=1)])
        }

app = FastAPI(
    title="🛡️ FraudGuard ML Service",
    version="2.3.0",
    description="Complete AI-powered fraud detection with ensemble models, SHAP explainability, real-time monitoring, and visual dashboard",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Add CORS middleware for dashboard
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify your domain
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global Exception Handler for Better Debugging
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    tb_str = ''.join(traceback.format_tb(exc.__traceback__))
    logger.error(f"🚨 UNHANDLED EXCEPTION: {exc}")
    logger.error(f"📍 TRACEBACK:\n{tb_str}")
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal Server Error", "error": str(exc), "traceback": tb_str}
    )

# Pydantic Models with V2 compatibility
class Transaction(BaseModel):
    model_config = ConfigDict(protected_namespaces=())
    
    user_id: str
    amount: float
    merchant_category: str
    hour: int
    day_of_week: int
    location: str = "unknown"
    merchant_id: Optional[str] = None
    transaction_id: Optional[str] = None

class TrainingData(BaseModel):
    model_config = ConfigDict(protected_namespaces=())
    
    user_id: str
    transactions: List[Transaction]

class FraudResult(BaseModel):
    model_config = ConfigDict(protected_namespaces=())
    
    user_id: str
    transaction_id: Optional[str] = None
    is_suspicious: bool
    anomaly_score: float
    risk_level: str
    explanation: List[str]
    confidence: float
    processing_time_ms: float
    shap_analysis: Optional[Dict] = None
    ensemble_analysis: Optional[Dict] = None

class UserProfile(BaseModel):
    model_config = ConfigDict(protected_namespaces=())
    
    user_id: str
    total_transactions: int
    avg_amount: float
    std_amount: float
    common_categories: Dict[str, int]
    active_hours: List[int]
    risk_indicators: Dict[str, float]
    model_trained: bool
    created_at: str

# SHAP Explainability Class
class SHAPExplainer:
    """Enhanced SHAP integration for fraud detection explainability"""
    
    def __init__(self):
        self.explainers = {}
        self.feature_names = ['amount', 'hour', 'day_of_week', 'category_encoded', 'is_weekend', 'is_night']
    
    def create_explainer(self, user_id: str, model, training_data: np.ndarray):
        """Create SHAP explainer for a specific user's model"""
        try:
            explainer = shap.Explainer(model, training_data)
            self.explainers[user_id] = explainer
            logger.info(f"✅ Created SHAP explainer for {user_id}")
            return True
        except Exception as e:
            logger.warning(f"Failed to create SHAP explainer for {user_id}: {e}")
            return False
    
    def explain_prediction(self, user_id: str, features: np.ndarray) -> Dict:
        """Generate SHAP explanations for a prediction"""
        if user_id not in self.explainers:
            return {"shap_available": False, "error": "SHAP explainer not found"}
        
        try:
            explainer = self.explainers[user_id]
            shap_values = explainer(features)
            
            if hasattr(shap_values, 'values'):
                values = shap_values.values[0]
            else:
                values = shap_values[0]
            
            feature_importance = {}
            explanations = []
            
            for i, (feature_name, shap_val) in enumerate(zip(self.feature_names, values)):
                feature_importance[feature_name] = float(shap_val)
                
                if abs(shap_val) > 0.01:
                    impact = "increases" if shap_val > 0 else "decreases" 
                    strength = "strongly" if abs(shap_val) > 0.1 else "moderately"
                    explanations.append(f"🔬 {feature_name}: {strength} {impact} fraud risk ({shap_val:.3f})")
            
            return {
                "shap_available": True,
                "feature_importance": feature_importance,
                "explanations": explanations,
                "total_impact": float(np.sum(values)),
                "most_important_feature": self.feature_names[np.argmax(np.abs(values))]
            }
            
        except Exception as e:
            logger.error(f"SHAP explanation failed for {user_id}: {e}")
            return {"shap_available": False, "error": str(e)}

# FIXED: Ensemble Fraud Detector with proper type conversion
class EnsembleFraudDetector:
    """Ensemble fraud detection with numpy type conversion fixes"""
    
    def __init__(self):
        self.models = {}
        self.scalers = {}
        
    def prepare_training_data_with_synthetic_fraud(self, transactions: List[Dict]) -> tuple:
        """Create training data with synthetic fraud examples"""
        df = pd.DataFrame(transactions)
        
        features = []
        labels = []
        
        for _, row in df.iterrows():
            feature_vector = [
                row['amount'],
                row['hour'],
                row['day_of_week'],
                hash(row['merchant_category']) % 1000,
                1 if row['day_of_week'] in [5, 6] else 0,
                1 if row['hour'] in [22, 23, 0, 1, 2, 3, 4, 5] else 0,
            ]
            features.append(feature_vector)
            labels.append(0)
        
        # Add synthetic fraud examples
        avg_amount = df['amount'].mean()
        for i in range(len(transactions) // 3):
            fraud_feature = [
                avg_amount * np.random.uniform(3, 10),
                np.random.choice([1, 2, 3, 23]),
                np.random.choice([5, 6]),
                hash('electronics') % 1000,
                1,
                1,
            ]
            features.append(fraud_feature)
            labels.append(1)
        
        return np.array(features), np.array(labels)
    
    def train_ensemble(self, user_id: str, transactions: List[Dict]):
        """Train simplified ensemble"""
        logger.info(f"🤖 Training simplified ensemble for {user_id}")
        
        try:
            X, y = self.prepare_training_data_with_synthetic_fraud(transactions)
            
            scaler = StandardScaler()
            X_scaled = scaler.fit_transform(X)
            self.scalers[user_id] = scaler
            
            model = RandomForestClassifier(
                n_estimators=100, 
                max_depth=8, 
                random_state=42,
                class_weight='balanced'
            )
            model.fit(X_scaled, y)
            
            self.models[user_id] = model
            
            fraud_rate = np.mean(y)
            logger.info(f"✅ Simplified ensemble training completed. Synthetic fraud rate: {fraud_rate:.1%}")
            
            return {
                'models_trained': ['random_forest'],
                'fraud_rate': float(fraud_rate),  # Convert to Python float
                'total_samples': int(len(X))  # Convert to Python int
            }
            
        except Exception as e:
            logger.error(f"Ensemble training failed for {user_id}: {e}")
            return None
    
    def predict_ensemble(self, user_id: str, transaction: Dict, isolation_score: float = None) -> Dict:
        """FIXED: Make ensemble prediction with proper type conversion"""
        if user_id not in self.models:
            return None
        
        try:
            features = [
                transaction['amount'],
                transaction['hour'],
                transaction['day_of_week'],
                hash(transaction['merchant_category']) % 1000,
                1 if transaction['day_of_week'] in [5, 6] else 0,
                1 if transaction['hour'] in [22, 23, 0, 1, 2, 3, 4, 5] else 0,
            ]
            
            features_scaled = self.scalers[user_id].transform([features])
            
            model = self.models[user_id]
            pred_proba = model.predict_proba(features_scaled)[0]
            fraud_proba = pred_proba[1] if len(pred_proba) > 1 else 0
            
            # CRITICAL FIX: Convert all numpy types to native Python types
            return {
                'ensemble_score': float(fraud_proba),  # Convert to Python float
                'is_fraud': bool(fraud_proba > 0.5),   # Convert to Python bool
                'model_predictions': {'random_forest': int(fraud_proba > 0.5)},  # Convert to Python int
                'model_probabilities': {'random_forest': float(fraud_proba)},   # Convert to Python float
                'confidence': float(abs(fraud_proba - 0.5) * 2)  # Convert to Python float
            }
            
        except Exception as e:
            logger.error(f"Ensemble prediction failed for {user_id}: {e}")
            return None

# Enhanced User Behavioral Profile Manager
class FraudGuardML:
    def __init__(self):
        self.profiles = {}
        self.models = {}
        self.scalers = {}
        self.training_data = {}
        self.shap_explainer = SHAPExplainer()
        self.ensemble_detector = EnsembleFraudDetector()
        self.ensemble_trained = {}
        self.feature_columns = ['amount', 'hour', 'day_of_week', 'category_encoded', 'is_weekend', 'is_night']
        
    def create_comprehensive_profile(self, user_id: str, transactions: List[Dict]) -> Dict:
        """Create detailed behavioral profile for user with ensemble and SHAP integration"""
        logger.info(f"🏗️ Starting profile creation for {user_id}")
        
        df = pd.DataFrame(transactions)
        
        # Basic statistics
        profile = {
            'user_id': user_id,
            'total_transactions': len(transactions),
            'avg_amount': float(df['amount'].mean()),
            'std_amount': float(df['amount'].std()),
            'median_amount': float(df['amount'].median()),
            'min_amount': float(df['amount'].min()),
            'max_amount': float(df['amount'].max()),
            
            'common_categories': df['merchant_category'].value_counts().head(5).to_dict(),
            'active_hours': df['hour'].value_counts().head(8).index.tolist(),
            'active_days': df['day_of_week'].value_counts().head(5).index.tolist(),
            
            'weekend_ratio': float(len(df[df['day_of_week'].isin([5, 6])]) / len(df)),
            'night_ratio': float(len(df[df['hour'].isin([22, 23, 0, 1, 2, 3, 4, 5])]) / len(df)),
            'high_amount_ratio': float(len(df[df['amount'] > df['amount'].quantile(0.9)]) / len(df)),
            
            'spending_velocity': float(len(transactions) / 30),
            'avg_daily_amount': float(df['amount'].sum() / 30),
            
            'created_at': datetime.now().isoformat(),
            'last_updated': datetime.now().isoformat(),
            'model_version': '2.3.0'
        }
        
        logger.info("📊 Extracting features...")
        features = self._extract_advanced_features(df)
        
        logger.info("🔄 Training Isolation Forest...")
        scaler = StandardScaler()
        scaled_features = scaler.fit_transform(features)
        
        model = IsolationForest(
            contamination=0.1,
            n_estimators=200,
            max_samples='auto',
            max_features=1.0,
            bootstrap=True,
            random_state=42,
            n_jobs=-1
        )
        model.fit(scaled_features)
        
        self.profiles[user_id] = profile
        self.models[user_id] = model
        self.scalers[user_id] = scaler
        self.training_data[user_id] = scaled_features
        
        logger.info("🔬 Creating SHAP explainer...")
        self.shap_explainer.create_explainer(user_id, model, scaled_features)
        
        logger.info("🤖 Training ensemble models...")
        try:
            ensemble_result = self.ensemble_detector.train_ensemble(user_id, transactions)
            if ensemble_result:
                self.ensemble_trained[user_id] = True
                profile['ensemble_models'] = ensemble_result['models_trained']
                profile['ensemble_fraud_rate'] = ensemble_result['fraud_rate']
            else:
                self.ensemble_trained[user_id] = False
        except Exception as e:
            logger.warning(f"Ensemble training failed for {user_id}: {e}")
            self.ensemble_trained[user_id] = False
        
        logger.info(f"✅ Profile creation completed for {user_id}")
        return profile
    
    def detect_advanced_fraud(self, user_id: str, transaction: Dict) -> Dict:
        """Advanced fraud detection with ensemble models, SHAP explanations, and monitoring"""
        start_time = datetime.now()
        
        if user_id not in self.models:
            return {
                'anomaly_score': 0.0,
                'is_suspicious': False,
                'risk_level': 'UNKNOWN',
                'explanation': ['User profile not found - please train model first'],
                'confidence': 0.0,
                'processing_time_ms': 0.0,
                'shap_analysis': None,
                'ensemble_analysis': None
            }
        
        model = self.models[user_id]
        scaler = self.scalers[user_id]
        profile = self.profiles[user_id]
        
        features = self._extract_single_transaction_features(transaction)
        scaled_features = scaler.transform([features])
        
        isolation_score = model.decision_function(scaled_features)[0]
        normalized_score = max(0, min(1, (0.5 - isolation_score)))
        
        rule_score = self._apply_business_rules(transaction, profile)
        
        ensemble_result = None
        final_score = (normalized_score * 0.7) + (rule_score * 0.3)
        
        if user_id in self.ensemble_trained and self.ensemble_trained[user_id]:
            try:
                ensemble_result = self.ensemble_detector.predict_ensemble(
                    user_id, transaction, normalized_score
                )
                
                if ensemble_result:
                    final_score = ensemble_result['ensemble_score']
                    
            except Exception as e:
                logger.warning(f"Ensemble prediction failed for {user_id}: {e}")
        
        shap_analysis = self.shap_explainer.explain_prediction(user_id, scaled_features)
        
        explanations = self._generate_comprehensive_explanations(transaction, profile, normalized_score, rule_score)
        
        if shap_analysis.get('shap_available', False):
            explanations.append("🔬 AI Model Feature Analysis:")
            explanations.extend(shap_analysis['explanations'][:3])
            most_important = shap_analysis.get('most_important_feature', 'unknown')
            explanations.append(f"🎯 Most influential factor: {most_important}")
        
        if ensemble_result:
            explanations.append("🤖 Ensemble Model Analysis:")
            explanations.append(f"📊 Ensemble confidence: {ensemble_result['confidence']:.3f}")
        
        if final_score > 0.9:
            risk_level = "CRITICAL"
            is_suspicious = True
        elif final_score > 0.7:
            risk_level = "HIGH"
            is_suspicious = True
        elif final_score > 0.5:
            risk_level = "MEDIUM"
            is_suspicious = True
        else:
            risk_level = "LOW"
            is_suspicious = False
        
        confidence = abs(final_score - 0.5) * 2
        processing_time = (datetime.now() - start_time).total_seconds() * 1000
        
        result = {
            'anomaly_score': float(final_score),  # Ensure Python float
            'is_suspicious': bool(is_suspicious),  # Ensure Python bool
            'risk_level': risk_level,
            'explanation': explanations,
            'confidence': float(confidence),  # Ensure Python float
            'processing_time_ms': float(processing_time),  # Ensure Python float
            'shap_analysis': shap_analysis,
            'ensemble_analysis': ensemble_result,
            'model_scores': {
                'isolation_forest': float(normalized_score),  # Ensure Python float
                'business_rules': float(rule_score),  # Ensure Python float
                'ensemble': float(final_score) if ensemble_result else None,  # Ensure Python float
                'combined': float(final_score)  # Ensure Python float
            }
        }
        
        monitor.record_transaction(user_id, result, processing_time)
        
        return result
    
    def _extract_advanced_features(self, df: pd.DataFrame) -> np.ndarray:
        """Extract comprehensive feature set"""
        features = []
        
        for _, row in df.iterrows():
            feature_vector = [
                row['amount'],
                row['hour'],
                row['day_of_week'],
                hash(row['merchant_category']) % 1000,
                1 if row['day_of_week'] in [5, 6] else 0,
                1 if row['hour'] in [22, 23, 0, 1, 2, 3, 4, 5] else 0,
            ]
            features.append(feature_vector)
        
        return np.array(features)
    
    def _extract_single_transaction_features(self, transaction: Dict) -> List[float]:
        """Extract features from single transaction"""
        return [
            transaction['amount'],
            transaction['hour'],
            transaction['day_of_week'],
            hash(transaction['merchant_category']) % 1000,
            1 if transaction['day_of_week'] in [5, 6] else 0,
            1 if transaction['hour'] in [22, 23, 0, 1, 2, 3, 4, 5] else 0
        ]
    
    def _apply_business_rules(self, transaction: Dict, profile: Dict) -> float:
        """Apply business rules for additional fraud detection"""
        rule_score = 0.0
        
        if transaction['amount'] > 5000:
            rule_score += 0.3
        
        if transaction['amount'] > profile['avg_amount'] * 10:
            rule_score += 0.4
        
        if transaction['hour'] in [0, 1, 2, 3, 4, 5]:
            rule_score += 0.2
        
        high_risk_categories = ['electronics', 'jewelry', 'travel', 'cash_advance']
        if transaction['merchant_category'] in high_risk_categories:
            rule_score += 0.2
        
        if transaction['day_of_week'] in [5, 6] and transaction['hour'] in [22, 23, 0, 1, 2]:
            rule_score += 0.15
        
        return min(1.0, rule_score)
    
    def _generate_comprehensive_explanations(self, transaction: Dict, profile: Dict, 
                                           ml_score: float, rule_score: float) -> List[str]:
        """Generate detailed human-readable explanations"""
        explanations = []
        
        amount = transaction['amount']
        avg_amount = profile['avg_amount']
        std_amount = profile['std_amount']
        
        if amount > avg_amount + 3 * std_amount:
            explanations.append(
                f"⚠️ Amount ${amount:.2f} is {amount/avg_amount:.1f}x higher than usual (avg: ${avg_amount:.2f})"
            )
        elif amount > avg_amount + 2 * std_amount:
            explanations.append(
                f"💡 Amount ${amount:.2f} is significantly higher than typical ${avg_amount:.2f}"
            )
        
        hour = transaction['hour']
        active_hours = profile['active_hours']
        
        if hour not in active_hours[:5]:
            if hour in [0, 1, 2, 3, 4, 5]:
                explanations.append(f"🌙 Very unusual time: {hour}:00 (typical hours: {active_hours[:3]})")
            else:
                explanations.append(f"⏰ Uncommon time: {hour}:00 (typical hours: {active_hours[:3]})")
        
        category = transaction['merchant_category']
        common_categories = list(profile['common_categories'].keys())
        
        if category not in common_categories[:3]:
            if category in ['electronics', 'jewelry', 'travel']:
                explanations.append(f"🎯 High-risk category: '{category}' (usual: {common_categories[:2]})")
            else:
                explanations.append(f"🏪 New category: '{category}' (usual: {common_categories[:2]})")
        
        if transaction['day_of_week'] in [5, 6]:
            if profile['weekend_ratio'] < 0.2:
                explanations.append("📅 Weekend transaction unusual for this user")
        
        explanations.append(f"🤖 ML confidence: {ml_score:.3f}, Rules: {rule_score:.3f}")
        
        return explanations if explanations else ["✅ Transaction appears normal for this user"]

# Initialize global components
ml_engine = FraudGuardML()
persistence = ModelPersistence()
monitor = FraudGuardMonitor()

# Visual Dashboard Endpoint
@app.get("/dashboard")
def serve_visual_dashboard():
    """Serve the visual monitoring dashboard"""
    dashboard_path = os.path.join(os.path.dirname(__file__), "dashboard.html")
    if os.path.exists(dashboard_path):
        return FileResponse(dashboard_path)
    else:
        raise HTTPException(status_code=404, detail="Dashboard file not found. Please create dashboard.html in the project directory.")

# API Endpoints
@app.get("/")
def root():
    return {
        "message": "🛡️ FraudGuard ML Service is online!",
        "version": "2.3.0",
        "status": "Ready for intelligent fraud detection with visual dashboard",
        "trained_users": len(ml_engine.profiles),
        "total_models": len(ml_engine.models),
        "features": [
            "Visual Dashboard",
            "Enhanced Error Handling",
            "Ensemble ML Models", 
            "Real-time Monitoring",
            "SHAP Explainable AI",
            "Advanced Pattern Recognition",
            "Model Persistence",
            "Automated Alerting"
        ],
        "endpoints": {
            "dashboard": "/dashboard",
            "api_docs": "/docs",
            "monitoring": "/monitoring/dashboard",
            "health": "/health"
        },
        "timestamp": datetime.now().isoformat()
    }

@app.post("/train-user")
def train_user_model(data: TrainingData):
    """Train comprehensive behavioral model with detailed error logging"""
    try:
        logger.info(f"🧠 Starting training for user: {data.user_id}")
        
        if len(data.transactions) < 15:
            logger.error(f"❌ Not enough transactions: {len(data.transactions)} < 15")
            raise HTTPException(
                status_code=400,
                detail="Need at least 15 transactions to train reliable behavioral model"
            )
        
        logger.info(f"✅ Transaction count OK: {len(data.transactions)}")
        
        start_time = datetime.now()
        
        logger.info("📦 Converting transactions to dict format...")
        try:
            transactions = [t.model_dump() for t in data.transactions]
            logger.info(f"✅ Successfully converted {len(transactions)} transactions")
        except Exception as e:
            logger.error(f"❌ Error converting transactions: {e}")
            raise
        
        logger.info("🏗️ Creating comprehensive profile...")
        try:
            profile = ml_engine.create_comprehensive_profile(data.user_id, transactions)
            logger.info(f"✅ Profile created successfully")
        except Exception as e:
            logger.error(f"❌ Error creating profile: {e}")
            raise
        
        training_time = (datetime.now() - start_time).total_seconds()
        logger.info(f"⏱️ Training completed in {training_time:.2f}s")
        
        logger.info("💾 Attempting to save model...")
        try:
            persistence.save_model(
                data.user_id,
                profile,
                ml_engine.models[data.user_id],
                ml_engine.scalers[data.user_id]
            )
            model_saved = True
            logger.info("✅ Model saved successfully")
        except Exception as e:
            logger.warning(f"⚠️ Failed to auto-save model: {e}")
            model_saved = False
        
        logger.info(f"🎯 Successfully trained model for {data.user_id} in {training_time:.2f}s")
        
        result = {
            "status": "success",
            "message": f"Successfully trained ensemble behavioral model for {data.user_id}",
            "profile": profile,
            "model_trained": True,
            "ensemble_trained": data.user_id in ml_engine.ensemble_trained,
            "model_saved": model_saved,
            "shap_enabled": data.user_id in ml_engine.shap_explainer.explainers,
            "training_time_seconds": training_time,
            "model_metadata": {
                "isolation_forest": "200 estimators, 10% contamination",
                "ensemble_models": profile.get('ensemble_models', []),
                "features_used": ml_engine.feature_columns,
                "shap_explainer": "TreeExplainer"
            }
        }
        
        logger.info("📤 Returning training result...")
        return jsonable_encoder(result)
        
    except HTTPException:
        logger.error("❌ HTTPException raised - re-raising")
        raise
    except Exception as e:
        logger.error(f"❌ CRITICAL ERROR in train_user_model: {e}")
        logger.error(f"📍 Full traceback: {traceback.format_exc()}")
        monitor.record_error("training_error", str(e))
        raise HTTPException(status_code=500, detail=f"Training failed: {str(e)}")

@app.post("/detect-fraud")
def detect_fraud(transaction: Transaction):
    """FIXED: Advanced real-time fraud detection with numpy type conversion"""
    try:
        start_detection_time = datetime.now()
        
        result = ml_engine.detect_advanced_fraud(transaction.user_id, transaction.model_dump())
        
        total_processing_time = (datetime.now() - start_detection_time).total_seconds() * 1000
        
        if result['is_suspicious']:
            logger.warning(
                f"🚨 FRAUD DETECTED: User {transaction.user_id}, "
                f"Amount: ${transaction.amount}, Risk: {result['risk_level']}"
            )
        
        # CRITICAL FIX: Convert any remaining numpy types
        result_clean = convert_numpy_types(result)
        json_compatible_result = jsonable_encoder(result_clean)
        
        return JSONResponse(content=json_compatible_result)
        
    except Exception as e:
        monitor.record_error("detection_error", str(e))
        logger.error(f"❌ Fraud detection failed: {str(e)}")
        logger.error(f"📍 Full traceback: {traceback.format_exc()}")
        raise HTTPException(status_code=500, detail=f"Detection failed: {str(e)}")

# Monitoring Endpoints
@app.get("/monitoring/metrics")
def get_monitoring_metrics():
    """Get current system metrics"""
    return jsonable_encoder(monitor.get_current_metrics())

@app.get("/monitoring/health")
def get_system_health():
    """Get system health status"""
    metrics = monitor.get_current_metrics()
    
    health_score = 100
    if metrics['fraud_rate'] > 0.1:
        health_score -= 20
    if metrics['avg_response_time_ms'] > 150:
        health_score -= 15
    if metrics['error_rate'] > 0.02:
        health_score -= 25
    health_score -= len(metrics['active_alerts']) * 10
    
    health_score = max(0, min(100, health_score))
    
    if health_score >= 90:
        health_status = "EXCELLENT"
    elif health_score >= 70:
        health_status = "GOOD"
    elif health_score >= 50:
        health_status = "FAIR"
    else:
        health_status = "POOR"
    
    result = {
        "system_health": health_status,
        "health_score": health_score,
        "active_alerts": metrics['active_alerts'],
        "key_metrics": {
            "total_requests": metrics['total_requests'],
            "fraud_rate": f"{metrics['fraud_rate']:.1%}",
            "avg_response_time": f"{metrics['avg_response_time_ms']:.1f}ms",
            "error_rate": f"{metrics['error_rate']:.1%}"
        },
        "timestamp": datetime.now().isoformat()
    }
    
    return jsonable_encoder(result)

@app.get("/monitoring/dashboard")
def get_monitoring_dashboard():
    """Get comprehensive monitoring dashboard data"""
    metrics = monitor.get_current_metrics()
    
    hourly_data = []
    for hour in range(24):
        stats = monitor.metrics['hourly_stats'][hour]
        if stats['requests'] > 0:
            hourly_fraud_rate = stats['fraud'] / stats['requests']
        else:
            hourly_fraud_rate = 0
        
        hourly_data.append({
            'hour': hour,
            'requests': stats['requests'],
            'fraud_count': stats['fraud'],
            'fraud_rate': hourly_fraud_rate
        })
    
    result = {
        "dashboard_title": "🛡️ FraudGuard ML Monitoring Dashboard",
        "last_updated": datetime.now().isoformat(),
        "system_overview": metrics,
        "performance_charts": {
            "hourly_activity": hourly_data
        },
        "recent_incidents": monitor.metrics['high_risk_transactions'][-10:],
        "ensemble_info": {
            "users_with_ensemble": len([u for u in ml_engine.ensemble_trained.keys() if ml_engine.ensemble_trained[u]]),
            "total_users": len(ml_engine.profiles),
            "ensemble_coverage": f"{len([u for u in ml_engine.ensemble_trained.keys() if ml_engine.ensemble_trained[u]])}/{len(ml_engine.profiles)}"
        }
    }
    
    return jsonable_encoder(result)

@app.get("/health")
def health_check():
    """Comprehensive system health check"""
    result = {
        "status": "healthy",
        "service": "FraudGuard ML Service",
        "version": "2.3.0",
        "uptime": "Running",
        "metrics": {
            "trained_users": len(ml_engine.profiles),
            "loaded_models": len(ml_engine.models),
            "ensemble_models": len([u for u in ml_engine.ensemble_trained.keys() if ml_engine.ensemble_trained[u]]),
            "saved_models": len(persistence.list_models()),
            "shap_explainers": len(ml_engine.shap_explainer.explainers),
            "total_requests": monitor.metrics['total_requests'],
            "fraud_detected": monitor.metrics['fraud_detected']
        },
        "capabilities": [
            "Visual Dashboard with Real-time Charts",
            "Enhanced Error Handling & Debugging",
            "Fixed Ensemble ML Models",
            "Real-time Monitoring", 
            "SHAP Explainable AI",
            "Automated Alerting",
            "Pattern Recognition",
            "Model Persistence"
        ],
        "access_points": {
            "visual_dashboard": "/dashboard",
            "api_documentation": "/docs",
            "monitoring_api": "/monitoring/dashboard",
            "health_check": "/health"
        },
        "timestamp": datetime.now().isoformat()
    }
    
    return jsonable_encoder(result)

if __name__ == "__main__":
    import uvicorn
    logger.info("🚀 Starting FraudGuard ML Service with Visual Dashboard...")
    logger.info("📊 Dashboard available at: http://localhost:8000/dashboard")
    logger.info("🔧 API Documentation at: http://localhost:8000/docs")
    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="info")
