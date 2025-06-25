package org.eagle.bank.controller;

import org.eagle.bank.security.JwtUtil;
import org.eagle.bank.service.UserService;
import org.eagle.bank.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    public static class LoginRequest {
        public String username;
        public String password;
        // Getters and setters omitted for brevity
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> authenticate(@RequestBody LoginRequest loginRequest) {

        Optional<User> optionalUser = userService.authenticateUser(loginRequest.username, loginRequest.password);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();
            String token = jwtUtil.generateToken(user.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);

            return ResponseEntity.ok(response);

        } else {

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid details");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
} 