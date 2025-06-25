package org.eagle.bank.controller;

import org.eagle.bank.dto.TransactionRequest;
import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.Transaction;
import org.eagle.bank.model.User;
import org.eagle.bank.service.BankAccountService;
import org.eagle.bank.service.TransactionService;
import org.eagle.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
public class TransactionController {

    @Autowired
    TransactionService transactionService;
    @Autowired
    BankAccountService accountService;
    @Autowired
    UserService userService;

    private User getAuthenticatedUser(Principal principal) {
        return userService.getUserByUsername(principal.getName()).orElse(null);
    }

    private BankAccount getUserAccount(String accountNumber, User user) {
        Optional<BankAccount> accOpt = accountService.getAccountByAccountNumber(accountNumber);
        if (accOpt.isEmpty()) {
            return null;
        }
        BankAccount acc = accOpt.get();
        if (!acc.getUser().getId().equals(user.getId())) {
            return null;
        }
        return acc;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> createTransaction(@PathVariable String accountNumber, @RequestBody TransactionRequest request, Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        BankAccount acc = getUserAccount(accountNumber, user);
        if (acc == null) {
            return ResponseEntity.status(404).body("Account not found or forbidden");
        }
        // Business logic: deposit/withdrawal
        Transaction.TransactionType type;
        try {
            type = Transaction.TransactionType.valueOf(request.getType().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid transaction type");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("Amount must be non-negative");
        }
        if (type == Transaction.TransactionType.WITHDRAWAL && acc.getBalance().compareTo(request.getAmount()) < 0) {
            return ResponseEntity.unprocessableEntity().body("Insufficient funds");
        }
        Transaction tx = new Transaction();
        tx.setType(type);
        tx.setAmount(request.getAmount());
        tx.setTimestamp(Instant.now());
        tx.setAccount(acc);
        // Update balance before saving
        if (type == Transaction.TransactionType.DEPOSIT) {
            acc.setBalance(acc.getBalance().add(request.getAmount()));
        } else if (type == Transaction.TransactionType.WITHDRAWAL) {
            acc.setBalance(acc.getBalance().subtract(request.getAmount()));
        }
        Transaction created = transactionService.applyTransaction(acc, tx);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> listTransactions(@PathVariable String accountNumber, Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        BankAccount acc = getUserAccount(accountNumber, user);
        if (acc == null) {
            return ResponseEntity.status(404).body("Account not found or forbidden");
        }
        List<Transaction> transactions = transactionService.getTransactionsByAccount(acc);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> getTransaction(@PathVariable String accountNumber, @PathVariable Long transactionId, Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        BankAccount acc = getUserAccount(accountNumber, user);
        if (acc == null) {
            return ResponseEntity.status(404).body("Account not found or forbidden");
        }
        Optional<Transaction> txOpt = transactionService.getTransactionByIdAndAccount(transactionId, acc);
        if (txOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Transaction not found");
        }
        Transaction tx = txOpt.get();
        return ResponseEntity.ok(tx);
    }
} 