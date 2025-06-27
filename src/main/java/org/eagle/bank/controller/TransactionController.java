package org.eagle.bank.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.eagle.bank.dto.CreateTransactionRequest;
import org.eagle.bank.dto.TransactionResponse;
import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.Transaction;
import org.eagle.bank.model.User;
import org.eagle.bank.service.BankAccountService;
import org.eagle.bank.service.TransactionService;
import org.eagle.bank.service.UserService;
import org.eagle.bank.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> createTransaction(@PathVariable String accountNumber, @RequestBody CreateTransactionRequest transactionRequest,
                                               HttpServletRequest request) {

        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        BankAccount authenticatedUserAcc = getAuthenticatedUserAccount(accountNumber, authenticatedUser);
        if (authenticatedUserAcc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account does not exist");
        }
        Transaction.TransactionType type;
        try {
            type = Transaction.TransactionType.valueOf(transactionRequest.getType().name());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid transaction type");
        }
        if (transactionRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Amount must be more than zero");
        }
        if (type == Transaction.TransactionType.WITHDRAWAL &&
                authenticatedUserAcc.getBalance().compareTo(transactionRequest.getAmount()) < 0) {
            return ResponseEntity.badRequest().body("Insufficient funds");
        }
        Transaction tx = MapperUtil.getTransaction(transactionRequest, authenticatedUserAcc, authenticatedUser);
        Transaction created = transactionService.applyTransaction(authenticatedUserAcc, tx);
        TransactionResponse response = MapperUtil.toTransactionResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> listTransactions(@PathVariable String accountNumber, HttpServletRequest request) {
        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        BankAccount acc = getAuthenticatedUserAccount(accountNumber, authenticatedUser);
        if (acc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found or forbidden");
        }
        List<Transaction> transactions = transactionService.getTransactionsByAccount(acc);
        List<TransactionResponse> response = MapperUtil.toTransactionResponseList(transactions);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> getTransaction(@PathVariable String accountNumber, @PathVariable Long transactionId,
                                            HttpServletRequest request) {
        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        BankAccount acc = getAuthenticatedUserAccount(accountNumber, authenticatedUser);
        if (acc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found or forbidden");
        }
        Optional<Transaction> txOpt = transactionService.getTransactionByIdAndAccount(transactionId, acc);
        if (txOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Transaction not found");
        }
        Transaction tx = txOpt.get();
        TransactionResponse response = MapperUtil.toTransactionResponse(tx);
        return ResponseEntity.ok(response);
    }


    private User getAuthenticatedUser(Long authenticatedUserId) {
        Optional<User> userOpt = userService.getUserById(authenticatedUserId);
        return userOpt.orElse(null);
    }

    private BankAccount getAuthenticatedUserAccount(String accountNumber, User user) {

        List<BankAccount> userAccounts = user.getAccounts();
        for (BankAccount acc : userAccounts) {
            if (acc.getAccountNumber().equals(accountNumber)) {
                return acc;
            }
        }
        return null;
    }

} 