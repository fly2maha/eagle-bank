package org.eagle.bank.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.eagle.bank.controller.AccountController;
import org.eagle.bank.controller.UserController;
import org.eagle.bank.dto.BankAccountResponse;
import org.eagle.bank.dto.CreateBankAccountRequest;
import org.eagle.bank.dto.UpdateBankAccountRequest;
import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.User;
import org.eagle.bank.service.BankAccountService;
import org.eagle.bank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness =  Strictness.LENIENT)
public class AccountControllerTest {


    @InjectMocks
    AccountController accountController;

    @Mock
    BankAccountService accountService;

    @Mock
    UserService userService;

    @Mock
    HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_unauthorized_returns401() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(null);

        CreateBankAccountRequest req = new CreateBankAccountRequest();
        ResponseEntity<?> response = accountController.createAccount(req, httpServletRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    @Test
    void getAccount_unauthorized_returns401() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(null);

        ResponseEntity<?> response = accountController.getAccount("123", httpServletRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getAccount_forbidden_returns403() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(accountService.getAccountByAccountNumber("123")).thenReturn(Optional.empty());

        ResponseEntity<?> response = accountController.getAccount("123", httpServletRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getAccount_success_returns200() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        BankAccount acc = new BankAccount();
        acc.setUser(user);
        acc.setAccountType("PERSONAL");
        when(accountService.getAccountByAccountNumber("123")).thenReturn(Optional.of(acc));

        ResponseEntity<?> response = accountController.getAccount("123", httpServletRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof BankAccountResponse);
    }

    @Test
    void listAccounts_unauthorized_returns401() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(null);

        ResponseEntity<?> response = accountController.listAccounts(httpServletRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void listAccounts_success_returns200() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(accountService.getAccountsByUser(user)).thenReturn(List.of());

        ResponseEntity<?> response = accountController.listAccounts(httpServletRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
    }

    @Test
    void updateAccount_unauthorized_returns401() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(null);

        UpdateBankAccountRequest req = new UpdateBankAccountRequest();
        ResponseEntity<?> response = accountController.updateAccount("123", req, httpServletRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void updateAccount_forbidden_returns403() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(accountService.getAccountByAccountNumber("123")).thenReturn(Optional.empty());

        UpdateBankAccountRequest req = new UpdateBankAccountRequest();
        ResponseEntity<?> response = accountController.updateAccount("123", req, httpServletRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateAccount_success_returns200() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        BankAccount acc = new BankAccount();
        acc.setUser(user);
        acc.setAccountType("PERSONAL");
        when(accountService.getAccountByAccountNumber("123")).thenReturn(Optional.of(acc));
        when(accountService.updateAccount(any())).thenReturn(acc);

        UpdateBankAccountRequest req = new UpdateBankAccountRequest();
        ResponseEntity<?> response = accountController.updateAccount("123", req, httpServletRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof BankAccountResponse);
    }

    @Test
    void deleteAccount_unauthorized_returns401() {

        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(null);
        ResponseEntity<?> response = accountController.deleteAccount("123", httpServletRequest);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deleteAccount_forbidden_returns403() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(accountService.getAccountByAccountNumber("123")).thenReturn(Optional.empty());
        ResponseEntity<?> response = accountController.deleteAccount("123", httpServletRequest);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteAccount_success_returns204() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        BankAccount acc = new BankAccount();
        acc.setId(10L);
        acc.setUser(user);
        when(accountService.getAccountByAccountNumber("123")).thenReturn(Optional.of(acc));
        ResponseEntity<?> response = accountController.deleteAccount("123", httpServletRequest);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
