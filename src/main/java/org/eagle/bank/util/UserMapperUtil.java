package org.eagle.bank.util;

import org.eagle.bank.dto.BankAccountResponse;
import org.eagle.bank.dto.CreateUserRequest;
import org.eagle.bank.dto.UserResponse;
import org.eagle.bank.model.BankAccount;
import org.eagle.bank.model.User;
import org.modelmapper.ModelMapper;

public class UserMapperUtil {
    private static final ModelMapper modelMapper = new ModelMapper();

    public static User toUser(CreateUserRequest request) {
        return modelMapper.map(request, User.class);
    }

    public static UserResponse toUserResponse(User user) {
        return modelMapper.map(user, UserResponse.class);
    }

    public static BankAccountResponse toBankAccountResponse(BankAccount bankAccount) {
        return modelMapper.map(bankAccount, BankAccountResponse.class);
    }
}