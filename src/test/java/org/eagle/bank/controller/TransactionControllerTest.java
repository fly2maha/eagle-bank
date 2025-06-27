package org.eagle.bank.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.eagle.bank.dto.CreateTransactionRequest;
import org.eagle.bank.dto.TransactionResponse;
import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.Transaction;
import org.eagle.bank.model.User;
import org.eagle.bank.service.TransactionService;
import org.eagle.bank.service.UserService;
import org.eagle.bank.util.MapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness =  Strictness.LENIENT)
public class TransactionControllerTest {


    @InjectMocks
    TransactionController transactionController;

    @Mock
    TransactionService transactionService;

    @Mock
    UserService userService;

    @Mock
    HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTransaction_unauthorized_returns401() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(null);

        CreateTransactionRequest req = new CreateTransactionRequest();
        ResponseEntity<?> response = transactionController.createTransaction("123", req, httpServletRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createTransaction_accountNotFound_returns404() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        user.setAccounts(List.of());
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        CreateTransactionRequest req = new CreateTransactionRequest();
        ResponseEntity<?> response = transactionController.createTransaction("123", req, httpServletRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createTransaction_invalidType_returns400() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        BankAccount acc = new BankAccount();
        acc.setAccountNumber("123");
        user.setAccounts(List.of(acc));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        CreateTransactionRequest req = mock(CreateTransactionRequest.class);
        when(req.getType()).thenReturn(null);

        ResponseEntity<?> response = transactionController.createTransaction("123", req, httpServletRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid transaction type"));
    }

    @Test
    void createTransaction_negativeAmount_returns400() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        BankAccount acc = new BankAccount();
        acc.setAccountNumber("123");
        user.setAccounts(List.of(acc));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        CreateTransactionRequest req = mock(CreateTransactionRequest.class);
        when(req.getType()).thenReturn(CreateTransactionRequest.TypeEnum.DEPOSIT);
        when(req.getAmount()).thenReturn(BigDecimal.valueOf(-100));

        ResponseEntity<?> response = transactionController.createTransaction("123", req, httpServletRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Amount must be more than zero"));
    }

    @Test
    void createTransaction_withdrawalInsufficientFunds_returns400() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        BankAccount acc = new BankAccount();
        acc.setAccountNumber("123");
        acc.setBalance(BigDecimal.valueOf(50));
        user.setAccounts(List.of(acc));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        CreateTransactionRequest req = mock(CreateTransactionRequest.class);
        when(req.getType()).thenReturn(CreateTransactionRequest.TypeEnum.WITHDRAWAL);
        when(req.getAmount()).thenReturn(BigDecimal.valueOf(100));

        ResponseEntity<?> response = transactionController.createTransaction("123", req, httpServletRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Insufficient funds"));
    }

    @Test
    void createTransaction_success_returns201() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        BankAccount acc = new BankAccount();
        acc.setAccountNumber("123");
        acc.setBalance(BigDecimal.valueOf(1000));
        user.setAccounts(List.of(acc));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        CreateTransactionRequest req = mock(CreateTransactionRequest.class);
        when(req.getType()).thenReturn(CreateTransactionRequest.TypeEnum.DEPOSIT);
        when(req.getAmount()).thenReturn(BigDecimal.valueOf(100));

        Transaction tx = new Transaction();
        Transaction createdTx = new Transaction();

        try (MockedStatic<MapperUtil> mu = mockStatic(MapperUtil.class)) {
            mu.when(() -> MapperUtil.getTransaction(any(), any(), any())).thenReturn(tx);
            when(transactionService.applyTransaction(acc, tx)).thenReturn(createdTx);
            TransactionResponse txResp = new TransactionResponse();
            mu.when(() -> MapperUtil.toTransactionResponse(createdTx)).thenReturn(txResp);

            ResponseEntity<?> response = transactionController.createTransaction("123", req, httpServletRequest);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertSame(txResp, response.getBody());
        }
    }

    // --- listTransactions ---

    @Test
    void listTransactions_unauthorized_returns401() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(null);

        ResponseEntity<?> response = transactionController.listTransactions("123", httpServletRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void listTransactions_accountNotFound_returns404() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        user.setAccounts(List.of());
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = transactionController.listTransactions("123", httpServletRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void listTransactions_success_returns200() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        BankAccount acc = new BankAccount();
        acc.setAccountNumber("123");
        user.setAccounts(List.of(acc));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        List<Transaction> txList = List.of(new Transaction());
        when(transactionService.getTransactionsByAccount(acc)).thenReturn(txList);

        try (MockedStatic<MapperUtil> mu = mockStatic(MapperUtil.class)) {
            List<TransactionResponse> respList = List.of(new TransactionResponse());
            mu.when(() -> MapperUtil.toTransactionResponseList(txList)).thenReturn(respList);

            ResponseEntity<?> response = transactionController.listTransactions("123", httpServletRequest);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertSame(respList, response.getBody());
        }
    }

    // --- getTransaction ---

    @Test
    void getTransaction_unauthorized_returns401() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(null);

        ResponseEntity<?> response = transactionController.getTransaction("123", 1L, httpServletRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getTransaction_accountNotFound_returns404() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        user.setAccounts(List.of());
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = transactionController.getTransaction("123", 1L, httpServletRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getTransaction_transactionNotFound_returns404() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        BankAccount acc = new BankAccount();
        acc.setAccountNumber("123");
        user.setAccounts(List.of(acc));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(transactionService.getTransactionByIdAndAccount(1L, acc)).thenReturn(Optional.empty());

        ResponseEntity<?> response = transactionController.getTransaction("123", 1L, httpServletRequest);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getTransaction_success_returns200() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        BankAccount acc = new BankAccount();
        acc.setAccountNumber("123");
        user.setAccounts(List.of(acc));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        Transaction tx = new Transaction();
        when(transactionService.getTransactionByIdAndAccount(1L, acc)).thenReturn(Optional.of(tx));

        try (MockedStatic<MapperUtil> mu = mockStatic(MapperUtil.class)) {
            TransactionResponse txResp = new TransactionResponse();
            mu.when(() -> MapperUtil.toTransactionResponse(tx)).thenReturn(txResp);

            ResponseEntity<?> response = transactionController.getTransaction("123", 1L, httpServletRequest);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertSame(txResp, response.getBody());
        }
    }
}