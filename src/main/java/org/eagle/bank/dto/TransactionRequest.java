package org.eagle.bank.dto;

import java.math.BigDecimal;

public class TransactionRequest {
    private String type;
    private BigDecimal amount;

    public TransactionRequest() {}

    public TransactionRequest(String type, BigDecimal amount) {
        this.type = type;
        this.amount = amount;
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
} 