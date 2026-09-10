package com.taplay.backend.dto;

public class MachineSessionStatusResponse {
    private String machineId;
    private String sessionStatus; // "IDLE", "ACTIVE", "EXPIRED"
    private int sessionDuration;
    private int timeRemaining;
    private String lastTransactionId;
    private String userName;
    private String gameName;

    public MachineSessionStatusResponse() {}

    public MachineSessionStatusResponse(String machineId, String sessionStatus, int sessionDuration, int timeRemaining, String lastTransactionId, String userName, String gameName) {
        this.machineId = machineId;
        this.sessionStatus = sessionStatus;
        this.sessionDuration = sessionDuration;
        this.timeRemaining = timeRemaining;
        this.lastTransactionId = lastTransactionId;
        this.userName = userName;
        this.gameName = gameName;
    }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public String getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }

    public int getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(int sessionDuration) { this.sessionDuration = sessionDuration; }

    public int getTimeRemaining() { return timeRemaining; }
    public void setTimeRemaining(int timeRemaining) { this.timeRemaining = timeRemaining; }

    public String getLastTransactionId() { return lastTransactionId; }
    public void setLastTransactionId(String lastTransactionId) { this.lastTransactionId = lastTransactionId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
}
