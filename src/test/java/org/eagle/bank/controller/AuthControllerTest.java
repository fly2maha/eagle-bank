package org.eagle.bank.controller;

import org.eagle.bank.model.User;
import org.eagle.bank.security.JwtUtil;
import org.eagle.bank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @InjectMocks
    AuthController authController;

    @Mock
    UserService userService;

    @Mock
    JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_success() {
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.username = "testuser";
        loginRequest.password = "testpass";

        User user = new User();
        user.setUsername("testuser");

        when(userService.authenticateUser("testuser", "testpass")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("testuser")).thenReturn("mocked-jwt-token");

        ResponseEntity<Map<String, String>> response = authController.authenticate(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mocked-jwt-token", response.getBody().get("token"));
    }

    @Test
    void authenticate_failure() {
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.username = "wronguser";
        loginRequest.password = "wrongpass";

        when(userService.authenticateUser("wronguser", "wrongpass")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, String>> response = authController.authenticate(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid details", response.getBody().get("error"));
    }
}