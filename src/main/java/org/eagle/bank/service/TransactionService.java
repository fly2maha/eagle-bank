package org.eagle.bank.service;

import org.eagle.bank.model.Transaction;
import org.eagle.bank.model.BankAccount;
import org.eagle.bank.repository.TransactionRepository;
import org.eagle.bank.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final BankAccountRepository accountRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, BankAccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public Transaction createTransaction(Transaction transaction) {
        // TODO: Add validation, business logic for deposit/withdrawal, etc.
        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getTransactionsByAccount(BankAccount account) {
        return transactionRepository.findByAccount(account);
    }

    public Optional<Transaction> getTransactionByIdAndAccount(Long id, BankAccount account) {
        return transactionRepository.findByIdAndAccount(id, account);
    }

    @Transactional
    public Transaction applyTransaction(BankAccount account, Transaction transaction) {
        // Save the transaction
        Transaction savedTx = transactionRepository.save(transaction);
        // Update the account balance
        accountRepository.save(account);
        // If any exception occurs, both the transaction and balance update are rolled back
        return savedTx;
    }
} 