package com.taplay.backend.controller;

import com.taplay.backend.entity.ArcadeMachineEntity;
import com.taplay.backend.repository.ArcadeMachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/machines")
@CrossOrigin(origins = "*")
public class MachineController {

    @Autowired
    private ArcadeMachineRepository machineRepository;

    @GetMapping("/{tagId}")
    public ResponseEntity<ArcadeMachineEntity> getMachineByTagId(@PathVariable String tagId) {
        Optional<ArcadeMachineEntity> machineOpt = machineRepository.findByTagId(tagId);
        if (machineOpt.isPresent()) {
            return ResponseEntity.ok(machineOpt.get());
        } else {
            ArcadeMachineEntity defaultMachine = new ArcadeMachineEntity();
            defaultMachine.setMachineId("MACH-A14");
            defaultMachine.setName("Air Hockey Blitz");
            defaultMachine.setTagId(tagId);
            defaultMachine.setCostCredits(10);
            defaultMachine.setCostAmount(10.0);
            defaultMachine.setSessionDuration(300);
            return ResponseEntity.ok(defaultMachine);
        }
    }
}
