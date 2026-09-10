package com.taplay.backend.dto;

public class TransactionResponse {
    private boolean authorized;
    private String message;
    private String transactionId;
    private int sessionDuration;
    private int creditsDeducted;
    private double remainingBalance;
    private String machineName;
    private String machineId;

    public TransactionResponse() {}

    public TransactionResponse(boolean authorized, String message, String transactionId, int sessionDuration, int creditsDeducted, double remainingBalance, String machineName, String machineId) {
        this.authorized = authorized;
        this.message = message;
        this.transactionId = transactionId;
        this.sessionDuration = sessionDuration;
        this.creditsDeducted = creditsDeducted;
        this.remainingBalance = remainingBalance;
        this.machineName = machineName;
        this.machineId = machineId;
    }

    public boolean isAuthorized() { return authorized; }
    public void setAuthorized(boolean authorized) { this.authorized = authorized; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public int getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(int sessionDuration) { this.sessionDuration = sessionDuration; }

    public int getCreditsDeducted() { return creditsDeducted; }
    public void setCreditsDeducted(int creditsDeducted) { this.creditsDeducted = creditsDeducted; }

    public double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(double remainingBalance) { this.remainingBalance = remainingBalance; }

    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }
}
