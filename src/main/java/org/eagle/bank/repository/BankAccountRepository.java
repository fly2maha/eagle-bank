package org.eagle.bank.repository;

import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    List<BankAccount> findByUser(User user);
} 