package com.taplay.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "arcade_machines")
public class ArcadeMachineEntity {

    @Id
    @Column(name = "machine_id")
    private String machineId;

    private String name;

    @Column(name = "tag_id", unique = true)
    private String tagId;

    @Column(name = "cost_credits")
    private Integer costCredits;

    @Column(name = "cost_amount")
    private Double costAmount;

    private String genre;
    private String difficulty;
    private String emoji;

    @Column(name = "session_duration")
    private Integer sessionDuration = 300;

    public ArcadeMachineEntity() {}

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }

    public Integer getCostCredits() { return costCredits; }
    public void setCostCredits(Integer costCredits) { this.costCredits = costCredits; }

    public Double getCostAmount() { return costAmount; }
    public void setCostAmount(Double costAmount) { this.costAmount = costAmount; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public Integer getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(Integer sessionDuration) { this.sessionDuration = sessionDuration; }
}
