package org.eagle.bank.service;

import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.User;
import org.eagle.bank.repository.BankAccountRepository;
import org.eagle.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness =  Strictness.LENIENT)
class BankAccountServiceTest {

    @InjectMocks
    BankAccountService bankAccountService;

    @Mock
    BankAccountRepository bankAccountRepository;

    @Mock
    UserRepository userRepository;

    User user;
    BankAccount bankAccount;

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
    }


    @Test
    void createAccount_savesAndReturnsAccount() {
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(bankAccount);

        BankAccount result = bankAccountService.createAccount(bankAccount);

        assertNotNull(result);
        assertEquals(bankAccount, result);
        verify(bankAccountRepository).save(bankAccount);
    }

    @Test
    void getAccountById_returnsOptionalAccount() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount));

        Optional<BankAccount> result = bankAccountService.getAccountById(1L);

        assertTrue(result.isPresent());
        assertEquals(bankAccount, result.get());
        verify(bankAccountRepository).findById(1L);
    }

    @Test
    void getAccountByAccountNumber_returnsOptionalAccount() {
        when(bankAccountRepository.findByAccountNumber("123456789")).thenReturn(Optional.of(bankAccount));

        Optional<BankAccount> result = bankAccountService.getAccountByAccountNumber("123456789");

        assertTrue(result.isPresent());
        assertEquals(bankAccount, result.get());
        verify(bankAccountRepository).findByAccountNumber("123456789");
    }

    @Test
    void getAccountsByUser_returnsListOfAccounts() {
        List<BankAccount> accounts = List.of(bankAccount);
        when(bankAccountRepository.findByUser(user)).thenReturn(accounts);

        List<BankAccount> result = bankAccountService.getAccountsByUser(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bankAccount, result.get(0));
        verify(bankAccountRepository).findByUser(user);
    }

    @Test
    void updateAccount_savesAndReturnsAccount() {
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(bankAccount);

        BankAccount result = bankAccountService.updateAccount(bankAccount);

        assertNotNull(result);
        assertEquals(bankAccount, result);
        verify(bankAccountRepository).save(bankAccount);
    }

    @Test
    void deleteAccount_deletesById() {
        doNothing().when(bankAccountRepository).deleteById(1L);

        bankAccountService.deleteAccount(1L);

        verify(bankAccountRepository).deleteById(1L);
    }
}