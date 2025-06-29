package org.eagle.bank.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.eagle.bank.dto.BankAccountResponse;
import org.eagle.bank.dto.CreateBankAccountRequest;
import org.eagle.bank.dto.UpdateBankAccountRequest;
import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.User;
import org.eagle.bank.service.BankAccountService;
import org.eagle.bank.service.UserService;
import org.eagle.bank.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    @Autowired
    BankAccountService accountService;
    @Autowired
    UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateBankAccountRequest createAccountRequest, HttpServletRequest request) {

        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        BankAccount account = MapperUtil.getBankAccount(createAccountRequest, authenticatedUser);
        BankAccount createdAccount = accountService.createAccount(account);
        BankAccountResponse response = MapperUtil.toBankAccountResponse(createdAccount);
        response.setAccountType(BankAccountResponse.AccountTypeEnum.valueOf(createdAccount.getAccountType().toUpperCase()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber, HttpServletRequest request) {

        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        BankAccount acc = getUserAccount(accountNumber, authenticatedUser);

        if (acc == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This account does not exist or you do not have access to it");
        }

        BankAccountResponse response = MapperUtil.toBankAccountResponse(acc);
        response.setAccountType(BankAccountResponse.AccountTypeEnum.valueOf(acc.getAccountType().toUpperCase()));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> listAccounts(HttpServletRequest request) {

        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        List<BankAccount> bankAccounts = accountService.getAccountsByUser(authenticatedUser);
        List<BankAccountResponse> responses = MapperUtil.toBankAccountResponseList(bankAccounts);
        return ResponseEntity.ok(responses);
    }


    @PatchMapping("/{accountNumber}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> updateAccount(@PathVariable String accountNumber, @RequestBody UpdateBankAccountRequest updateAccountRequest, HttpServletRequest request) {

        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        BankAccount acc = getUserAccount(accountNumber, authenticatedUser);
        if (acc == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account not found or forbidden");
        }

        MapperUtil.updateAccount(updateAccountRequest, acc);
        BankAccount updatedAcc = accountService.updateAccount(acc);
        BankAccountResponse response = MapperUtil.toBankAccountResponse(updatedAcc);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{accountNumber}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> deleteAccount(@PathVariable String accountNumber, HttpServletRequest request) {
        User authenticatedUser = getAuthenticatedUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        BankAccount acc = getUserAccount(accountNumber, authenticatedUser);

        if (acc == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This account does not exist or you do not have access to it");
        }
        accountService.deleteAccount(acc.getId());
        return ResponseEntity.noContent().build();
    }


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


}