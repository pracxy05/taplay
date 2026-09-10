package com.taplay.backend.dto;

public class TopUpRequest {
    private Long userId = 1L;
    private Double amount;
    private String paymentMethod = "RAZORPAY_TEST";
    private String paymentId = "pay_test_" + System.currentTimeMillis();

    public TopUpRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
}
