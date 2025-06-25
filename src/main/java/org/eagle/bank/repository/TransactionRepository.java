package org.eagle.bank.repository;

import org.eagle.bank.model.Transaction;
import org.eagle.bank.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(BankAccount account);
    Optional<Transaction> findByIdAndAccount(Long id, BankAccount account);
} 