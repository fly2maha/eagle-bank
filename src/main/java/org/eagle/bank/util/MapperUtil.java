package org.eagle.bank.util;

import org.eagle.bank.dto.*;
import org.eagle.bank.model.Address;
import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.Transaction;
import org.eagle.bank.model.User;
import org.hibernate.tuple.UpdateTimestampGeneration;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MapperUtil {
    private static final ModelMapper modelMapper = new ModelMapper();
    static {
        // Register Instant → OffsetDateTime converter
        Converter<Instant, OffsetDateTime> instantToOffsetConverter = ctx ->
                ctx.getSource() == null ? null : ctx.getSource().atOffset(ZoneOffset.UTC);
        modelMapper.addConverter(instantToOffsetConverter);
    }
    public static User toUser(CreateUserRequest request) {
        return modelMapper.map(request, User.class);
    }

    public static UserResponse toUserResponse(User user) {
        return modelMapper.map(user, UserResponse.class);
    }

    public static BankAccountResponse toBankAccountResponse(BankAccount bankAccount) {
        BankAccountResponse response = modelMapper.map(bankAccount, BankAccountResponse.class);
        response.setUserId(bankAccount.getUser().getId().toString());
        response.setAccountType(BankAccountResponse.AccountTypeEnum.valueOf(bankAccount.getAccountType().toUpperCase()));
        return response;
    }

    public static TransactionResponse toTransactionResponse(Transaction transaction) {
        TransactionResponse tx = modelMapper.map(transaction, TransactionResponse.class);
        tx.setAccountId(transaction.getAccount().getId().toString());
        tx.setAccountNumber(transaction.getAccount().getAccountNumber());
        tx.setUserId(transaction.getAccount().getUser().getId().toString());
        tx.setCreatedTimestamp(transaction.getTimestamp().atOffset(ZoneOffset.UTC));
        return tx;

    }

    public static List<TransactionResponse> toTransactionResponseList(List<Transaction> transactions) {
        return transactions.stream()
                .map(MapperUtil::toTransactionResponse)
                .collect(Collectors.toList());
    }

    public static BankAccount updateAccount(UpdateBankAccountRequest request, BankAccount bankAccount) {
        if (request.getName() != null) {
            bankAccount.setName(request.getName());
        }
        return bankAccount;
    }


    public  static void updateUser(UpdateUserRequest updateUserRequest, User authenticatedUser) {


        if (updateUserRequest.getName() != null && !updateUserRequest.getName().trim().isEmpty()) {
            authenticatedUser.setName(updateUserRequest.getName());
        }

        if (updateUserRequest.getPhoneNumber() != null && !updateUserRequest.getPhoneNumber().trim().isEmpty()) {
            authenticatedUser.setPhoneNumber(updateUserRequest.getPhoneNumber());
        }

        if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().trim().isEmpty()) {
            authenticatedUser.setEmail(updateUserRequest.getEmail());
        }

        UpdateUserRequestAddress addressFrom = updateUserRequest.getAddress();
        Address addressTo = authenticatedUser.getAddress();
        if(addressFrom != null && addressFrom.getLine1() != null && !addressFrom.getLine1().trim().isEmpty()) {
            addressTo.setLine1(addressFrom.getLine1());
        }
        if(addressFrom != null && addressFrom.getTown() != null && !addressFrom.getTown().trim().isEmpty()) {
            addressTo.setTown(addressFrom.getTown());
        }
        if(addressFrom != null && addressFrom.getCounty() != null && !addressFrom.getCounty().trim().isEmpty()) {
            addressTo.setTown(addressFrom.getCounty());
        }
        if(addressFrom != null && addressFrom.getPostcode() != null && !addressFrom.getPostcode().trim().isEmpty()) {
            addressTo.setPostcode(addressFrom.getPostcode());
        }

    }

    public static BankAccount getBankAccount(CreateBankAccountRequest createAccountRequest, User authenticatedUser) {
        BankAccount account = new BankAccount();
        account.setName(createAccountRequest.getName());
        account.setAccountNumber(accountNoGenerator());
        account.setSortCode(sortCodeGenerator());
        account.setBalance(createAccountRequest.getBalance());
        account.setAccountType(createAccountRequest.getAccountType().toString());
        account.setUser(authenticatedUser);
        return account;
    }

    public static Transaction getTransaction(CreateTransactionRequest transactionRequest, BankAccount bankAccount, User user) {
        Transaction.TransactionType type = Transaction.TransactionType.valueOf(transactionRequest.getType().name());
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setReference(transactionRequest.getReference());
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setAccount(bankAccount);
        transaction.setTimestamp(Instant.now());
        if (type == Transaction.TransactionType.DEPOSIT) {
            bankAccount.setBalance(bankAccount.getBalance().add(transactionRequest.getAmount()));
        } else if (type == Transaction.TransactionType.WITHDRAWAL) {
            bankAccount.setBalance(bankAccount.getBalance().subtract(transactionRequest.getAmount()));
        }
        return transaction;
    }

    private static String accountNoGenerator() {
        // Simple account number generator: 01 + 6 random digits
        int random = (int)(Math.random() * 1_000_000);
        return String.format("01%06d", random);
    }

    private static String sortCodeGenerator() {
        // Simple sort code generator: 12 + 2 random digits
        Random random = new Random();
        int part1 = random.nextInt(100); // 00 to 99
        int part2 = random.nextInt(100);
        int part3 = random.nextInt(100);

        return String.format("%02d-%02d-%02d", part1, part2, part3);
    }
}