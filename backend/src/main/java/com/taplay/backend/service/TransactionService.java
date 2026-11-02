package com.taplay.backend.service;

import com.taplay.backend.dto.MachineSessionStatusResponse;
import com.taplay.backend.dto.StartTransactionRequest;
import com.taplay.backend.dto.TransactionResponse;
import com.taplay.backend.entity.ArcadeMachineEntity;
import com.taplay.backend.entity.TransactionEntity;
import com.taplay.backend.entity.UserEntity;
import com.taplay.backend.repository.ArcadeMachineRepository;
import com.taplay.backend.repository.TransactionRepository;
import com.taplay.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArcadeMachineRepository machineRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    // In-memory active session tracker per machineId or tagId
    private final Map<String, ActiveMachineSession> activeSessions = new ConcurrentHashMap<>();

    public static class ActiveMachineSession {
        private final String transactionId;
        private final String userName;
        private final String gameName;
        private final LocalDateTime startTime;
        private final int durationSeconds;
        // Pause support
        private long pauseOffsetSeconds = 0;
        private LocalDateTime pausedAt = null;

        public ActiveMachineSession(String transactionId, String userName, String gameName, LocalDateTime startTime, int durationSeconds) {
            this.transactionId = transactionId;
            this.userName = userName;
            this.gameName = gameName;
            this.startTime = startTime;
            this.durationSeconds = durationSeconds;
        }

        public String getTransactionId() { return transactionId; }
        public String getUserName() { return userName; }
        public String getGameName() { return gameName; }
        public LocalDateTime getStartTime() { return startTime; }
        public int getDurationSeconds() { return durationSeconds; }

        public boolean isPaused() { return pausedAt != null; }

        /** Freeze the countdown; records current elapsed time. */
        public void pause(LocalDateTime now) {
            if (pausedAt == null) {
                pausedAt = now;
                pauseOffsetSeconds += Duration.between(startTime, now).getSeconds();
            }
        }

        /** Resume: shift startTime forward so remaining time is correct. */
        public void resume(LocalDateTime now) {
            pausedAt = null;
            // Reconstruct effective elapsed by keeping pauseOffsetSeconds unchanged
        }

        /** Returns effective elapsed seconds, accounting for pauses. */
        public long effectiveElapsed(LocalDateTime now) {
            if (isPaused()) {
                return pauseOffsetSeconds;
            }
            return pauseOffsetSeconds + Duration.between(
                pausedAt != null ? pausedAt : startTime, now).getSeconds();
        }
    }

    @Transactional
    public TransactionResponse processTransaction(StartTransactionRequest req) {
        Long userId = req.getUserId() != null ? req.getUserId() : 1L;
        Optional<UserEntity> userOpt = userRepository.findById(userId);

        UserEntity user = userOpt.orElseGet(() -> {
            UserEntity newUser = new UserEntity(1L, "Praharsh", "praharsh@taplay.app", 540.0, 54, 120);
            return userRepository.save(newUser);
        });

        // Resolve or create machine entity
        String tagId = (req.getTagId() == null || req.getTagId().trim().isEmpty()) ? "MACH-PHONE-2" : req.getTagId();
        Optional<ArcadeMachineEntity> machineOpt = machineRepository.findByTagId(tagId);
        if (machineOpt.isEmpty()) {
            machineOpt = machineRepository.findById(tagId);
        }

        ArcadeMachineEntity machine = machineOpt.orElseGet(() -> {
            ArcadeMachineEntity defaultMachine = new ArcadeMachineEntity();
            String mId = tagId.startsWith("MACH-") ? tagId : "MACH-PHONE-2";
            defaultMachine.setMachineId(mId);
            defaultMachine.setName("Cyber Strike 2077");
            defaultMachine.setTagId(tagId);
            defaultMachine.setCostCredits(10);
            defaultMachine.setCostAmount(10.0);
            defaultMachine.setSessionDuration(300);
            return machineRepository.save(defaultMachine);
        });

        // ML Fraud evaluation
        boolean isFraud = fraudDetectionService.isTransactionFraudulent(user.getId(), machine.getCostAmount(), tagId);
        if (isFraud) {
            return new TransactionResponse(
                false,
                "Transaction flagged as high-risk by ML Fraud Detection system.",
                null,
                0,
                0,
                user.getBalance(),
                machine.getName(),
                machine.getMachineId()
            );
        }

        // Verify user balance
        if (user.getBalance() < machine.getCostAmount()) {
            return new TransactionResponse(
                false,
                "Insufficient wallet balance (Requires ₹" + machine.getCostAmount() + ")",
                null,
                0,
                0,
                user.getBalance(),
                machine.getName(),
                machine.getMachineId()
            );
        }

        // Deduct balance
        double newBalance = user.getBalance() - machine.getCostAmount();
        user.setBalance(newBalance);
        user.setCredits((int) (newBalance / 10));
        userRepository.save(user);

        // Record transaction in H2 database
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        TransactionEntity txn = new TransactionEntity();
        txn.setId(txnId);
        txn.setUserId(user.getId());
        txn.setTitle(machine.getName());
        txn.setMachineId(machine.getMachineId());
        txn.setAmount(machine.getCostAmount());
        txn.setCredits(machine.getCostCredits());
        txn.setType("GAME_SESSION");
        txn.setTimestamp(LocalDateTime.now());
        txn.setStatus("SUCCESS");
        transactionRepository.save(txn);

        // Register active session for real-time polling
        ActiveMachineSession activeSession = new ActiveMachineSession(
            txnId,
            user.getUsername(),
            machine.getName(),
            LocalDateTime.now(),
            machine.getSessionDuration()
        );
        
        // Map to specific machine ID, tag ID, default emulator, and Galaxy Watch
        activeSessions.put(machine.getMachineId(), activeSession);
        activeSessions.put(tagId, activeSession);
        activeSessions.put("MACH-PHONE-2", activeSession);
        activeSessions.put("CARD-WATCH-01", activeSession);

        return new TransactionResponse(
            true,
            "Transaction Authorized & Session Started",
            txnId,
            machine.getSessionDuration(),
            machine.getCostCredits(),
            newBalance,
            machine.getName(),
            machine.getMachineId()
        );
    }

    public MachineSessionStatusResponse getSessionStatus(String machineId) {
        String key = (machineId == null || machineId.trim().isEmpty()) ? "MACH-PHONE-2" : machineId;
        ActiveMachineSession session = activeSessions.get(key);
        
        if (session == null) {
            return new MachineSessionStatusResponse(key, "IDLE", 0, 0, null, null, null);
        }

        // Use pause-aware elapsed time
        long elapsedSeconds = session.effectiveElapsed(LocalDateTime.now());
        int remaining = (int) (session.getDurationSeconds() - elapsedSeconds);
        String status = session.isPaused() ? "PAUSED" : (remaining > 0 ? "ACTIVE" : "IDLE");

        if (remaining > 0 || session.isPaused()) {
            return new MachineSessionStatusResponse(
                key,
                status,
                session.getDurationSeconds(),
                Math.max(0, remaining),
                session.getTransactionId(),
                session.getUserName(),
                session.getGameName()
            );
        } else {
            activeSessions.remove(key);
            return new MachineSessionStatusResponse(
                key,
                "IDLE",
                session.getDurationSeconds(),
                0,
                session.getTransactionId(),
                session.getUserName(),
                session.getGameName()
            );
        }
    }

    public boolean pauseSession(String machineId) {
        ActiveMachineSession session = activeSessions.get(machineId);
        if (session == null) {
            session = activeSessions.get("CARD-WATCH-01");
            if (session == null) session = activeSessions.get("MACH-PHONE-2");
        }
        if (session != null) {
            session.pause(LocalDateTime.now());
            return true;
        }
        return false;
    }

    public boolean resumeSession(String machineId) {
        ActiveMachineSession session = activeSessions.get(machineId);
        if (session == null) {
            session = activeSessions.get("CARD-WATCH-01");
            if (session == null) session = activeSessions.get("MACH-PHONE-2");
        }
        if (session != null) {
            session.resume(LocalDateTime.now());
            return true;
        }
        return false;
    }

    public boolean endSession(String machineId) {
        if (machineId != null) {
            activeSessions.remove(machineId);
        }
        activeSessions.remove("MACH-PHONE-2");
        activeSessions.remove("CARD-WATCH-01");
        return true;
    }
}