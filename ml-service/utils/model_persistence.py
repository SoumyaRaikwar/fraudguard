import os
import pickle
import logging
from typing import Optional, List, Tuple, Dict, Any

logger = logging.getLogger(__name__)

class ModelPersistence:
    """Handle saving and loading of ML models and associated data"""
    
    def __init__(self, models_dir: str = "saved_models"):
        """
        Initialize model persistence handler
        
        Args:
            models_dir: Directory to save models
        """
        self.models_dir = models_dir
        
        # Create directory if it doesn't exist
        try:
            os.makedirs(models_dir, exist_ok=True)
            logger.info(f"Model persistence initialized with directory: {models_dir}")
        except Exception as e:
            logger.error(f"Failed to create models directory {models_dir}: {e}")
            raise
    
    def save_model(self, user_id: str, profile: Dict, model: Any, scaler: Any) -> bool:
        """
        Save model and related data for a user
        
        Args:
            user_id: Unique user identifier
            profile: User profile dictionary
            model: Trained ML model
            scaler: Fitted scaler
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            model_path = os.path.join(self.models_dir, f"{user_id}.pkl")
            
            # Package all data together
            model_data = {
                'user_id': user_id,
                'profile': profile,
                'model': model,
                'scaler': scaler,
                'saved_at': os.path.getmtime(model_path) if os.path.exists(model_path) else None
            }
            
            # Save using pickle with highest protocol for efficiency
            with open(model_path, 'wb') as f:
                pickle.dump(model_data, f, protocol=pickle.HIGHEST_PROTOCOL)
            
            # Verify the file was created
            if os.path.exists(model_path):
                file_size = os.path.getsize(model_path)
                logger.info(f"Saved model for {user_id} at {model_path} ({file_size} bytes)")
                return True
            else:
                logger.error(f"Model file not created for {user_id}")
                return False
                
        except Exception as e:
            logger.error(f"Failed to save model for {user_id}: {e}")
            return False
    
    def load_model(self, user_id: str) -> Tuple[Optional[Dict], Optional[Any], Optional[Any]]:
        """
        Load model and related data for a user
        
        Args:
            user_id: Unique user identifier
            
        Returns:
            Tuple of (profile, model, scaler) or (None, None, None) if failed
        """
        try:
            model_path = os.path.join(self.models_dir, f"{user_id}.pkl")
            
            if not os.path.exists(model_path):
                logger.warning(f"Model file not found for {user_id} at {model_path}")
                return None, None, None
            
            # Load the pickled data
            with open(model_path, 'rb') as f:
                model_data = pickle.load(f)
            
            # Extract components
            profile = model_data.get('profile')
            model = model_data.get('model')
            scaler = model_data.get('scaler')
            
            # Validate loaded data
            if profile is None or model is None or scaler is None:
                logger.error(f"Incomplete model data for {user_id}")
                return None, None, None
            
            logger.info(f"Loaded model for {user_id} from {model_path}")
            return profile, model, scaler
            
        except Exception as e:
            logger.error(f"Failed to load model for {user_id}: {e}")
            return None, None, None
    
    def list_models(self) -> List[str]:
        """
        List all available saved models
        
        Returns:
            List of user IDs that have saved models
        """
        try:
            if not os.path.exists(self.models_dir):
                return []
            
            # Get all .pkl files and extract user IDs
            pkl_files = [f for f in os.listdir(self.models_dir) if f.endswith('.pkl')]
            user_ids = [f.replace('.pkl', '') for f in pkl_files]
            
            logger.info(f"Found {len(user_ids)} saved models: {user_ids}")
            return user_ids
            
        except Exception as e:
            logger.error(f"Failed to list models: {e}")
            return []
    
    def delete_model(self, user_id: str) -> bool:
        """
        Delete saved model for a user
        
        Args:
            user_id: Unique user identifier
            
        Returns:
            bool: True if successful, False otherwise
        """
        try:
            model_path = os.path.join(self.models_dir, f"{user_id}.pkl")
            
            if os.path.exists(model_path):
                os.remove(model_path)
                logger.info(f"Deleted model for {user_id}")
                return True
            else:
                logger.warning(f"Model file not found for deletion: {user_id}")
                return False
                
        except Exception as e:
            logger.error(f"Failed to delete model for {user_id}: {e}")
            return False
    
    def get_model_info(self, user_id: str) -> Optional[Dict]:
        """
        Get metadata about a saved model
        
        Args:
            user_id: Unique user identifier
            
        Returns:
            Dictionary with model metadata or None if not found
        """
        try:
            model_path = os.path.join(self.models_dir, f"{user_id}.pkl")
            
            if not os.path.exists(model_path):
                return None
            
            # Get file stats
            file_stats = os.stat(model_path)
            
            return {
                'user_id': user_id,
                'file_path': model_path,
                'file_size_bytes': file_stats.st_size,
                'created_at': file_stats.st_ctime,
                'modified_at': file_stats.st_mtime,
                'accessible': os.access(model_path, os.R_OK)
            }
            
        except Exception as e:
            logger.error(f"Failed to get model info for {user_id}: {e}")
            return None
    
    def cleanup_old_models(self, max_age_days: int = 30) -> int:
        """
        Remove models older than specified days
        
        Args:
            max_age_days: Maximum age in days
            
        Returns:
            Number of models deleted
        """
        try:
            if not os.path.exists(self.models_dir):
                return 0
            
            import time
            current_time = time.time()
            max_age_seconds = max_age_days * 24 * 60 * 60
            deleted_count = 0
            
            for filename in os.listdir(self.models_dir):
                if filename.endswith('.pkl'):
                    file_path = os.path.join(self.models_dir, filename)
                    file_age = current_time - os.path.getmtime(file_path)
                    
                    if file_age > max_age_seconds:
                        os.remove(file_path)
                        deleted_count += 1
                        logger.info(f"Deleted old model: {filename}")
            
            logger.info(f"Cleanup completed: {deleted_count} old models removed")
            return deleted_count
            
        except Exception as e:
            logger.error(f"Failed to cleanup old models: {e}")
            return 0
