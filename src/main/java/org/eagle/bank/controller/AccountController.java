package org.eagle.bank.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.eagle.bank.dto.BankAccountRequest;
import org.eagle.bank.dto.BankAccountResponse;
import org.eagle.bank.dto.CreateBankAccountRequest;
import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.User;
import org.eagle.bank.service.BankAccountService;
import org.eagle.bank.service.UserService;
import org.eagle.bank.util.UserMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    @Autowired
    BankAccountService accountService;
    @Autowired
    UserService userService;

    private User getAuthenticatedUser(Long authenticatedUserId) {
        Optional<User> userOpt = userService.getUserById(authenticatedUserId);
        return userOpt.orElse(null);
    }

    private BankAccount getUserAccount(String accountNumber, User user) {

        Optional<BankAccount> accOpt = accountService.getAccountByAccountNumber(accountNumber);
        if (accOpt.isEmpty()) {
            return null;
        }
        BankAccount acc = accOpt.get();
        if (!acc.getUser().getId().equals(user.getId())) return null;
        return acc;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> createAccount(@RequestBody CreateBankAccountRequest createAccountRequest, HttpServletRequest request) {

        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

//        // Validation for mandatory fields
//       if (createAccountRequest.getAccountType() != null|| createAccountRequest.getAccountType().trim().isEmpty()) {
//            return ResponseEntity.badRequest().body("Missing required field: accountType");
//        }
//        if (createAccou ntRequest.getBalance()== null) {
//            return ResponseEntity.badRequest().body("Missing required field: initialBalance");
//        }
        if (createAccountRequest.getBalance() < 50) {
            return ResponseEntity.badRequest().body("Initial balance must be at least 50");
        }

        BigDecimal balance = new BigDecimal(Double.toString(createAccountRequest.getBalance()));
        BankAccount account = new BankAccount();
        account.setName(createAccountRequest.getName());
        account.setAccountNumber(generateAccountNumber());
        account.setSortCode(sortCodeGenerator());
        account.setBalance(balance);
        account.setAccountType(createAccountRequest.getAccountType().getValue());
        account.setUser(authenticatedUser);

        BankAccount createdAccount = accountService.createAccount(account);
        BankAccountResponse response = UserMapperUtil.toBankAccountResponse(createdAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> listAccounts(HttpServletRequest request) {

        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<BankAccount> accounts = accountService.getAccountsByUser(authenticatedUser);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber, HttpServletRequest request) {
        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        BankAccount acc = getUserAccount(accountNumber, authenticatedUser);

        if (acc == null) {
            return ResponseEntity.status(404).body("Account not found or forbidden");
        }
        BankAccountResponse response = UserMapperUtil.toBankAccountResponse(acc);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountNumber}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> updateAccount(@PathVariable String accountNumber, @RequestBody BankAccountRequest updateAccountRequest, HttpServletRequest request) {

        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        BankAccount acc = getUserAccount(accountNumber, authenticatedUser);
        if (acc == null) {
            return ResponseEntity.status(404).body("Account not found or forbidden");
        }
        if (updateAccountRequest.getBalance() != null) {
            acc.setBalance(updateAccountRequest.getBalance());
        }
        BankAccount updated = accountService.updateAccount(acc);
        return ResponseEntity.ok(updated);
    }

//    @DeleteMapping("/{accountNumber}")
//    @PreAuthorize("hasAuthority('USER')")
//    public ResponseEntity<?> deleteAccount(@PathVariable String accountNumber, Principal principal) {
//        User user = getAuthenticatedUser(principal);
//        if (user == null) {
//            return ResponseEntity.status(401).body("Unauthorized");
//        }
//        BankAccount acc = getUserAccount(accountNumber, user);
//        if (acc == null) {
//            return ResponseEntity.status(404).body("Account not found or forbidden");
//        }
//        accountService.deleteAccount(acc.getId());
//        return ResponseEntity.noContent().build();
//    }

    private String generateAccountNumber() {
        // Simple account number generator: 01 + 6 random digits
        int random = (int)(Math.random() * 1_000_000);
        return String.format("01%06d", random);
    }

    private String sortCodeGenerator() {
        // Simple sort code generator: 12 + 2 random digits
        Random random = new Random();
        int part1 = random.nextInt(100); // 00 to 99
        int part2 = random.nextInt(100);
        int part3 = random.nextInt(100);

        return String.format("%02d-%02d-%02d", part1, part2, part3);
    }

}