import numpy as np
import joblib
from sklearn.ensemble import IsolationForest
import os

def train_and_save_model():
    np.random.seed(42)
    n_samples = 1000

    # Normal arcade transactions:
    # Amount: 10 - 30 credits
    # Hour of day: 10am - 10pm (10 - 22)
    # Frequency in last hour: 1 - 4 sessions
    # Terminal ID hash: 1 - 10
    normal_amounts = np.random.normal(loc=15, scale=5, size=n_samples)
    normal_hours = np.random.randint(10, 23, size=n_samples)
    normal_freq = np.random.randint(1, 5, size=n_samples)
    normal_terminals = np.random.randint(1, 10, size=n_samples)

    X_normal = np.column_stack([normal_amounts, normal_hours, normal_freq, normal_terminals])

    # Anomalous/Fraudulent transactions:
    # High amounts (e.g. 500+), midnight hours (3 AM), rapid repeated frequency (50+)
    n_fraud = 50
    fraud_amounts = np.random.uniform(200, 1000, size=n_fraud)
    fraud_hours = np.random.randint(2, 5, size=n_fraud)
    fraud_freq = np.random.randint(20, 100, size=n_fraud)
    fraud_terminals = np.random.randint(1, 10, size=n_fraud)

    X_fraud = np.column_stack([fraud_amounts, fraud_hours, fraud_freq, fraud_terminals])

    X_train = np.vstack([X_normal, X_fraud])

    # Isolation Forest Model
    model = IsolationForest(n_estimators=100, contamination=0.05, random_state=42)
    model.fit(X_train)

    model_path = os.path.join(os.path.dirname(__file__), "isolation_forest.pkl")
    joblib.dump(model, model_path)
    print(f"✅ Isolation Forest fraud model trained and saved to {model_path}")

if __name__ == "__main__":
    train_and_save_model()
