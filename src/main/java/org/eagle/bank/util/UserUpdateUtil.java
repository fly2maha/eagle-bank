package org.eagle.bank.util;

import org.eagle.bank.dto.UpdateUserRequest;
import org.eagle.bank.model.User;

public class UserUpdateUtil {


    public  static void updateUser( UpdateUserRequest updateUserRequest, User authenticatedUser) {

        if (updateUserRequest.getName() != null && !updateUserRequest.getName().trim().isEmpty()) {
            authenticatedUser.setName(updateUserRequest.getName());
        }

        if (updateUserRequest.getPhoneNumber() != null && !updateUserRequest.getPhoneNumber().trim().isEmpty()) {
            authenticatedUser.setPhoneNumber(updateUserRequest.getPhoneNumber());
        }

        if (updateUserRequest.getEmail() != null && !updateUserRequest.getEmail().trim().isEmpty()) {
            authenticatedUser.setEmail(updateUserRequest.getEmail());
        }

    }
}