package org.eagle.bank.controller;

import org.eagle.bank.dto.CreateUserRequest;
import org.eagle.bank.dto.UpdateUserRequest;
import org.eagle.bank.dto.UserRequest;
import org.eagle.bank.dto.UserResponse;
import org.eagle.bank.model.Address;
import org.eagle.bank.model.User;
import org.eagle.bank.model.BankAccount;
import org.eagle.bank.service.UserService;
import org.eagle.bank.util.UserMapperUtil;
import org.eagle.bank.util.UserUpdateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;

import javax.validation.Valid;
import java.util.ArrayList;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    @Autowired
    UserService userService;


    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {

        User user = UserMapperUtil.toUser(request);
        User created = userService.createUser(user);
        UserResponse userResponse = UserMapperUtil.toUserResponse(created);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }



    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> getUser(@PathVariable Long userId, HttpServletRequest request) {

        User authenticatedUser = fetchUser((Long) request.getAttribute("authenticatedUserId"));
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden - Can only access own user data");
        }
        UserResponse userResponse = UserMapperUtil.toUserResponse(authenticatedUser);
        return ResponseEntity.ok(userResponse);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest updateUserRequest, HttpServletRequest request) {

        User authenticatedUser = fetchUser((Long) request.getAttribute("authenticatedUserId"));
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden - Can only update own user data");
        }

        UserUpdateUtil.updateUser(updateUserRequest, authenticatedUser);
        User  updatedUser = userService.updateUser(authenticatedUser);
        UserResponse userResponse = UserMapperUtil.toUserResponse(updatedUser);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpServletRequest request) {

        User authenticatedUser = fetchUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden - Can only delete own user data");
        }

        if( authenticatedUser.getAccounts() != null && !authenticatedUser.getAccounts().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot delete user with existing accounts");
        }
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    private User fetchUser(Long authenticatedUserId) {
        Optional<User> userOpt = userService.getUserById(authenticatedUserId);
        return userOpt.orElse(null);
    }
} 