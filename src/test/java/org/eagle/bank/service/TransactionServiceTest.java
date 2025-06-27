package org.eagle.bank.service;

import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.Transaction;
import org.eagle.bank.model.User;
import org.eagle.bank.repository.BankAccountRepository;
import org.eagle.bank.repository.TransactionRepository;
import org.eagle.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness =  Strictness.LENIENT)
public class TransactionServiceTest {

    @InjectMocks
    TransactionService transactionService;

    @Mock
    BankAccountRepository bankAccountRepository;
    @Mock
    TransactionRepository transactionRepository;

    BankAccount bankAccount;
    Transaction transaction;
    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("rawpass");
        user.setEmail("john@example.com");

        bankAccount = new BankAccount();
        bankAccount.setId(1L);
        bankAccount.setAccountNumber("123456789");
        bankAccount.setBalance(BigDecimal.valueOf(1000.0));
        bankAccount.setUser(user);
        bankAccount.setAccountType("PERSONAL");

        transaction = new Transaction();
        transaction.setReference("test transaction");
        transaction.setId(1L);
        transaction.setAmount(BigDecimal.valueOf(100.0));
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setAccount(bankAccount);

    }

    @Test
    void createTransaction_savesAndReturnsTransaction() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.createTransaction(transaction);

        assertNotNull(result);
        assertEquals(transaction, result);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void getTransactionById_returnsOptionalTransaction() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Optional<Transaction> result = transactionService.getTransactionById(1L);

        assertTrue(result.isPresent());
        assertEquals(transaction, result.get());
        verify(transactionRepository).findById(1L);
    }

    @Test
    void getTransactionsByAccount_returnsList() {
        List<Transaction> txList = List.of(transaction);
        when(transactionRepository.findByAccount(bankAccount)).thenReturn(txList);

        List<Transaction> result = transactionService.getTransactionsByAccount(bankAccount);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transaction, result.get(0));
        verify(transactionRepository).findByAccount(bankAccount);
    }

    @Test
    void getTransactionByIdAndAccount_returnsOptionalTransaction() {
        when(transactionRepository.findByIdAndAccount(1L, bankAccount)).thenReturn(Optional.of(transaction));

        Optional<Transaction> result = transactionService.getTransactionByIdAndAccount(1L, bankAccount);

        assertTrue(result.isPresent());
        assertEquals(transaction, result.get());
        verify(transactionRepository).findByIdAndAccount(1L, bankAccount);
    }

    @Test
    void applyTransaction_savesTransactionAndAccount() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(bankAccount);

        Transaction result = transactionService.applyTransaction(bankAccount, transaction);

        assertNotNull(result);
        assertEquals(transaction, result);
        verify(transactionRepository).save(transaction);
        verify(bankAccountRepository).save(bankAccount);
    }

}
