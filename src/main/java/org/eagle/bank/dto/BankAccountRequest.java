package org.eagle.bank.dto;

import java.math.BigDecimal;
import java.util.List;


public class BankAccountRequest {
    private String accountNumber;
    private BigDecimal balance;
    private List<Long> transactionIds;
    private String accountType;

    public BankAccountRequest() {}

    public BankAccountRequest(String accountNumber, BigDecimal balance, List<Long> transactionIds,
                              String accountType, String currency) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.transactionIds = transactionIds;
        this.accountType = accountType;
    }

    // Getters and setters
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountNumber = accountNumber; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public List<Long> getTransactionIds() { return transactionIds; }
    public void setTransactionIds(List<Long> transactionIds) { this.transactionIds = transactionIds; }
} 