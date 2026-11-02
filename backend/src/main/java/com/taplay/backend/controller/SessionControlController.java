package com.taplay.backend.controller;

import com.taplay.backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Session control endpoints used by the Galaxy Watch app.
 * Pause/resume do not exist in the vanilla transactional flow —
 * they freeze the countdown on the backend side by recording a pause offset.
 */
@RestController
@RequestMapping("/api/v1/session")
@CrossOrigin(origins = "*")
public class SessionControlController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/pause")
    public ResponseEntity<Map<String, Object>> pauseSession(
            @RequestParam(defaultValue = "CARD-WATCH-01") String machineId) {
        boolean result = transactionService.pauseSession(machineId);
        return ResponseEntity.ok(Map.of(
                "success", result,
                "machineId", machineId,
                "action", "PAUSED"
        ));
    }

    @PostMapping("/resume")
    public ResponseEntity<Map<String, Object>> resumeSession(
            @RequestParam(defaultValue = "CARD-WATCH-01") String machineId) {
        boolean result = transactionService.resumeSession(machineId);
        return ResponseEntity.ok(Map.of(
                "success", result,
                "machineId", machineId,
                "action", "RESUMED"
        ));
    }
}
