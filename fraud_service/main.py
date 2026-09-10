import os
import joblib
import numpy as np
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="Taplay Fraud Detection Microservice", version="1.0.0")

MODEL_PATH = os.path.join(os.path.dirname(__file__), "isolation_forest.pkl")

# Load or generate model on startup
if not os.path.exists(MODEL_PATH):
    from train_model import train_and_save_model
    train_and_save_model()

model = joblib.load(MODEL_PATH)

class PredictionRequest(BaseModel):
    user_id: int
    amount: float
    hour_of_day: int
    frequency_1h: int
    terminal_id_hash: int = 1

class PredictionResponse(BaseModel):
    is_fraud: bool
    anomaly_score: float
    message: str

@app.get("/health")
def health_check():
    return {"status": "UP", "service": "Taplay Fraud Detection ML Service"}

@app.post("/predict", response_model=PredictionResponse)
def predict_fraud(req: PredictionRequest):
    try:
        features = np.array([[req.amount, req.hour_of_day, req.frequency_1h, req.terminal_id_hash]])
        
        # Isolation Forest prediction: -1 for anomaly/fraud, 1 for normal
        pred = model.predict(features)[0]
        score = float(model.score_samples(features)[0])
        
        is_fraud = bool(pred == -1)
        msg = "High risk anomalous transaction pattern detected!" if is_fraud else "Transaction pattern normal."
        
        return PredictionResponse(
            is_fraud=is_fraud,
            anomaly_score=round(score, 4),
            message=msg
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=5000, reload=True)
