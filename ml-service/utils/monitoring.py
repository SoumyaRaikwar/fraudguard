import logging
import time
import json
from datetime import datetime, timedelta
from typing import Dict, List
from collections import defaultdict, deque
import threading

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
            'max_fraud_rate': 0.15,  # 15% fraud rate threshold
            'max_response_time': 200,  # 200ms response time
            'max_error_rate': 0.05,   # 5% error rate
            'min_requests_for_alert': 100
        }
        
        self.logger = logging.getLogger("fraudguard_monitor")
        self._setup_logging()
        
    def _setup_logging(self):
        """Setup monitoring-specific logging"""
        handler = logging.FileHandler('fraudguard_monitoring.log')
        formatter = logging.Formatter(
            '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
        )
        handler.setFormatter(formatter)
        self.logger.addHandler(handler)
        self.logger.setLevel(logging.INFO)
    
    def record_transaction(self, user_id: str, result: Dict, processing_time: float):
        """Record transaction metrics"""
        self.metrics['total_requests'] += 1
        self.metrics['user_activity'][user_id] += 1
        self.metrics['processing_times'].append(processing_time)
        
        # Record fraud detection
        if result.get('is_suspicious', False):
            self.metrics['fraud_detected'] += 1
            
            # Store high-risk transactions for analysis
            if result.get('risk_level') in ['HIGH', 'CRITICAL']:
                self.metrics['high_risk_transactions'].append({
                    'user_id': user_id,
                    'timestamp': datetime.now().isoformat(),
                    'risk_level': result['risk_level'],
                    'anomaly_score': result.get('anomaly_score', 0)
                })
        
        # Hourly statistics
        hour = datetime.now().hour
        self.metrics['hourly_stats'][hour]['requests'] += 1
        if result.get('is_suspicious', False):
            self.metrics['hourly_stats'][hour]['fraud'] += 1
        
        # Check for alerts
        self._check_alerts()
        
    def record_error(self, error_type: str, details: str):
        """Record system errors"""
        self.metrics['error_count'] += 1
        self.logger.error(f"Error recorded: {error_type} - {details}")
        self._check_alerts()
    
    def record_model_prediction(self, model_name: str, prediction: int):
        """Record individual model predictions"""
        self.metrics['model_predictions'][model_name] += prediction
    
    def _check_alerts(self):
        """Check if any alert thresholds are exceeded"""
        if self.metrics['total_requests'] < self.thresholds['min_requests_for_alert']:
            return
        
        # High fraud rate alert
        fraud_rate = self.metrics['fraud_detected'] / self.metrics['total_requests']
        if fraud_rate > self.thresholds['max_fraud_rate']:
            if not self.alerts['high_fraud_rate']:
                self._trigger_alert('high_fraud_rate', f"Fraud rate {fraud_rate:.1%} exceeds threshold")
                self.alerts['high_fraud_rate'] = True
        else:
            self.alerts['high_fraud_rate'] = False
        
        # Slow response alert
        if len(self.metrics['processing_times']) > 50:
            avg_response = sum(list(self.metrics['processing_times'])[-50:]) / 50
            if avg_response > self.thresholds['max_response_time']:
                if not self.alerts['slow_response']:
                    self._trigger_alert('slow_response', f"Avg response time {avg_response:.1f}ms exceeds threshold")
                    self.alerts['slow_response'] = True
            else:
                self.alerts['slow_response'] = False
        
        # Error rate alert
        error_rate = self.metrics['error_count'] / self.metrics['total_requests']
        if error_rate > self.thresholds['max_error_rate']:
            if not self.alerts['model_errors']:
                self._trigger_alert('model_errors', f"Error rate {error_rate:.1%} exceeds threshold")
                self.alerts['model_errors'] = True
        else:
            self.alerts['model_errors'] = False
    
    def _trigger_alert(self, alert_type: str, message: str):
        """Trigger an alert"""
        alert_data = {
            'type': alert_type,
            'message': message,
            'timestamp': datetime.now().isoformat(),
            'metrics_snapshot': self.get_current_metrics()
        }
        
        self.logger.warning(f"ALERT TRIGGERED: {alert_type} - {message}")
        
        # In production, you would send this to monitoring systems
        # like Slack, email, PagerDuty, etc.
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
    
    def get_detailed_analytics(self) -> Dict:
        """Get detailed analytics dashboard data"""
        metrics = self.get_current_metrics()
        
        # Hourly breakdown
        hourly_data = []
        for hour in range(24):
            stats = self.metrics['hourly_stats'][hour]
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
        
        # Model performance comparison
        model_performance = {}
        total_model_predictions = sum(self.metrics['model_predictions'].values())
        for model, predictions in self.metrics['model_predictions'].items():
            if total_model_predictions > 0:
                model_performance[model] = predictions / total_model_predictions
            else:
                model_performance[model] = 0
        
        return {
            'current_metrics': metrics,
            'hourly_breakdown': hourly_data,
            'model_performance': model_performance,
            'recent_high_risk_transactions': self.metrics['high_risk_transactions'][-10:],
            'alert_history': list(self.alerts.keys()),
            'system_health': self._calculate_system_health()
        }
    
    def _calculate_system_health(self) -> str:
        """Calculate overall system health score"""
        health_score = 100
        
        # Deduct points for active alerts
        active_alerts = sum(1 for alert in self.alerts.values() if alert)
        health_score -= active_alerts * 20
        
        # Deduct for high error rate
        if self.metrics['total_requests'] > 0:
            error_rate = self.metrics['error_count'] / self.metrics['total_requests']
            health_score -= error_rate * 100
        
        # Deduct for slow response times
        if len(self.metrics['processing_times']) > 10:
            avg_response = sum(list(self.metrics['processing_times'])[-10:]) / 10
            if avg_response > 100:
                health_score -= 10
        
        health_score = max(0, min(100, health_score))
        
        if health_score >= 90:
            return "EXCELLENT"
        elif health_score >= 70:
            return "GOOD"
        elif health_score >= 50:
            return "FAIR"
        else:
            return "POOR"

# Global monitor instance
monitor = FraudGuardMonitor()
