package org.eagle.bank.service;

import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.User;
import org.eagle.bank.repository.BankAccountRepository;
import org.eagle.bank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BankAccountService {

    private final BankAccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public BankAccountService(BankAccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public BankAccount createAccount(BankAccount account) {
        return accountRepository.save(account);
    }

    public Optional<BankAccount> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public Optional<BankAccount> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public List<BankAccount> getAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }

    public BankAccount updateAccount(BankAccount account) {
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }
} 