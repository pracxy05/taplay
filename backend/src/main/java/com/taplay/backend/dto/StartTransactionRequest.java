package com.taplay.backend.dto;

public class StartTransactionRequest {
    private String tagId;
    private Long userId = 1L;
    private Long timestamp = System.currentTimeMillis();

    public StartTransactionRequest() {}

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
