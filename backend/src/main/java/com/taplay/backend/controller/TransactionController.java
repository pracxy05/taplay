package com.taplay.backend.controller;

import com.taplay.backend.dto.MachineSessionStatusResponse;
import com.taplay.backend.dto.StartTransactionRequest;
import com.taplay.backend.dto.TransactionResponse;
import com.taplay.backend.entity.TransactionEntity;
import com.taplay.backend.repository.TransactionRepository;
import com.taplay.backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping("/transaction/start")
    public ResponseEntity<TransactionResponse> startTransaction(@RequestBody StartTransactionRequest req) {
        TransactionResponse response = transactionService.processTransaction(req);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transaction/status")
    public ResponseEntity<MachineSessionStatusResponse> getTransactionStatus(@RequestParam String machineId) {
        MachineSessionStatusResponse status = transactionService.getSessionStatus(machineId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/transaction/end")
    public ResponseEntity<Boolean> endTransaction(
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String machineId) {
        transactionService.endSession(machineId);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/user/transactions")
    public ResponseEntity<List<TransactionEntity>> getUserTransactions(@RequestParam(defaultValue = "1") Long userId) {
        List<TransactionEntity> list = transactionRepository.findByUserIdOrderByTimestampDesc(userId);
        return ResponseEntity.ok(list);
    }
}
