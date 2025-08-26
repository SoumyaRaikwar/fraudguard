
import shap
import numpy as np

class Explainability:
    def __init__(self, model, scaler):
        self.model = model
        self.scaler = scaler
        self.explainer = None
        self._init_explainer()

    def _init_explainer(self):
        try:
            self.explainer = shap.Explainer(self.model)
        except Exception as e:
            print(f"Error creating SHAP explainer: {e}")
            self.explainer = None

    def explain(self, X):
        if self.explainer is None:
            return None
        X_scaled = self.scaler.transform(X)
        shap_values = self.explainer(X_scaled)
        return shap_values

    def get_feature_importance(self, shap_values, feature_names):
        if shap_values is None:
            return None
        importance = np.abs(shap_values.values).mean(axis=0)
        return dict(zip(feature_names, importance))

