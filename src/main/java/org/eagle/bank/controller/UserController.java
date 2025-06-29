package org.eagle.bank.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.eagle.bank.dto.CreateUserRequest;
import org.eagle.bank.dto.UpdateUserRequest;
import org.eagle.bank.dto.UserResponse;
import org.eagle.bank.model.User;
import org.eagle.bank.service.UserService;
import org.eagle.bank.util.MapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1/users")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);



    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {

        logger.info("New user creation request received: {}", request.getName());
        User user = MapperUtil.toUser(request);
        User created = userService.createUser(user);
        UserResponse userResponse = MapperUtil.toUserResponse(created);
        logger.info("New user created: {}, {}", request.getName(), created.getId());
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }



    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> getUser(@PathVariable Long userId, HttpServletRequest request) {

        logger.info("Fetching user details: {}", userId);

        if (request.getAttribute("authenticatedUserId") == null) {

            logger.warn("No authenticated user found in request: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User authenticatedUser = fetchUser((Long) request.getAttribute("authenticatedUserId"));

        if (authenticatedUser == null) {
            logger.warn("User not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if (!authenticatedUser.getId().equals(userId)) {
            logger.warn("User {} attempted to access user {} data", authenticatedUser.getId(), userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden - Can only access own user data");
        }
        UserResponse userResponse = MapperUtil.toUserResponse(authenticatedUser);
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

        MapperUtil.updateUser(updateUserRequest, authenticatedUser);
        User  updatedUser = userService.updateUser(authenticatedUser);
        UserResponse userResponse = MapperUtil.toUserResponse(updatedUser);
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