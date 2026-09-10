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
    }

    @Transactional
    public TransactionResponse processTransaction(StartTransactionRequest req) {
        Long userId = req.getUserId() != null ? req.getUserId() : 1L;
        Optional<UserEntity> userOpt = userRepository.findById(userId);

        UserEntity user = userOpt.orElseGet(() -> {
            UserEntity newUser = new UserEntity(1L, "Praharsh", "praharsh@taplay.app", 540.0, 54, 120);
            return userRepository.save(newUser);
        });

        // Find Arcade Machine by Tag UID (e.g. 04A289C2 or MACH-PHONE-2)
        Optional<ArcadeMachineEntity> machineOpt = machineRepository.findByTagId(req.getTagId());
        if (machineOpt.isEmpty()) {
            machineOpt = machineRepository.findById(req.getTagId());
        }

        ArcadeMachineEntity machine = machineOpt.orElseGet(() -> {
            ArcadeMachineEntity defaultMachine = new ArcadeMachineEntity();
            String mId = req.getTagId().startsWith("MACH-") ? req.getTagId() : "MACH-A14";
            defaultMachine.setMachineId(mId);
            defaultMachine.setName(req.getTagId().contains("PHONE-2") ? "Cyber Strike 2077" : "Air Hockey Blitz");
            defaultMachine.setTagId(req.getTagId());
            defaultMachine.setCostCredits(10);
            defaultMachine.setCostAmount(10.0);
            defaultMachine.setSessionDuration(300);
            return machineRepository.save(defaultMachine);
        });

        // Check for fraud
        boolean isFraud = fraudDetectionService.isTransactionFraudulent(user.getId(), machine.getCostAmount(), req.getTagId());
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

        // Verify balance
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

        // Record Transaction
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

        // Register active session for real-time polling by emulator / phone 2
        ActiveMachineSession activeSession = new ActiveMachineSession(
            txnId,
            user.getUsername(),
            machine.getName(),
            LocalDateTime.now(),
            machine.getSessionDuration()
        );
        activeSessions.put(machine.getMachineId(), activeSession);
        activeSessions.put(req.getTagId(), activeSession);

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
        ActiveMachineSession session = activeSessions.get(machineId);
        if (session == null) {
            return new MachineSessionStatusResponse(machineId, "IDLE", 0, 0, null, null, null);
        }

        long elapsedSeconds = Duration.between(session.getStartTime(), LocalDateTime.now()).getSeconds();
        int remaining = (int) (session.getDurationSeconds() - elapsedSeconds);

        if (remaining > 0) {
            return new MachineSessionStatusResponse(
                machineId,
                "ACTIVE",
                session.getDurationSeconds(),
                remaining,
                session.getTransactionId(),
                session.getUserName(),
                session.getGameName()
            );
        } else {
            activeSessions.remove(machineId);
            return new MachineSessionStatusResponse(
                machineId,
                "IDLE",
                session.getDurationSeconds(),
                0,
                session.getTransactionId(),
                session.getUserName(),
                session.getGameName()
            );
        }
    }

    public boolean endSession(String machineId) {
        if (machineId != null) {
            activeSessions.remove(machineId);
        }
        return true;
    }
}

