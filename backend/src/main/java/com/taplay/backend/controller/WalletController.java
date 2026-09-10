package com.taplay.backend.controller;

import com.taplay.backend.dto.TopUpRequest;
import com.taplay.backend.entity.UserEntity;
import com.taplay.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/wallet")
    public ResponseEntity<UserEntity> getWalletInfo(@RequestParam(defaultValue = "1") Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        UserEntity user = userOpt.orElseGet(() -> 
            userRepository.save(new UserEntity(1L, "Praharsh", "praharsh@taplay.app", 540.0, 54, 120))
        );
        return ResponseEntity.ok(user);
    }

    @PostMapping("/topup")
    public ResponseEntity<UserEntity> topUpWallet(@RequestBody TopUpRequest req) {
        Long userId = req.getUserId() != null ? req.getUserId() : 1L;
        UserEntity user = userRepository.findById(userId).orElseGet(() -> 
            userRepository.save(new UserEntity(1L, "Praharsh", "praharsh@taplay.app", 540.0, 54, 120))
        );

        double newBalance = user.getBalance() + (req.getAmount() != null ? req.getAmount() : 100.0);
        user.setBalance(newBalance);
        user.setCredits((int) (newBalance / 10));
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }
}
