package org.eagle.bank.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.eagle.bank.JsonReader;
import org.eagle.bank.dto.CreateUserRequest;
import org.eagle.bank.dto.UpdateUserRequest;
import org.eagle.bank.dto.UserResponse;
import org.eagle.bank.exception.UserAlreadyExistsException;
import org.eagle.bank.model.User;
import org.eagle.bank.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness =  Strictness.LENIENT)
public class UserControllerTest {


    @InjectMocks
    UserController userController;

    @Mock
    UserService userService;

    @Mock
    HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void attempt_to_create_existing_user() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("john");
        req.setPassword("pass");
        req.setEmail("john@example.com");

        when(userService.createUser(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("User already exists"));

        assertThrows(DataIntegrityViolationException.class, ()->userController.createUser(req));

    }


    @Test
    void createUser_success() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("john");
        req.setPassword("pass");
        req.setEmail("john@example.com");

        User user = new User();
        user.setUsername("john");
        User created = new User();
        created.setId(1L);
        created.setUsername("john");

        when(userService.createUser(any(User.class))).thenReturn(created);

        ResponseEntity<?> response = userController.createUser(req);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof UserResponse);
        assertEquals("john", ((UserResponse) response.getBody()).getUsername());
    }

    @Test
    void getUser_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.getUser(1L, httpServletRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof UserResponse);
        assertEquals("john", ((UserResponse) response.getBody()).getUsername());
    }

    @Test
    void getUser_notFound() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUser(1L, httpServletRequest);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getUser_forbidden() {
        User user = new User();
        user.setId(2L);

        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(2L);
        when(userService.getUserById(2L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.getUser(1L, httpServletRequest);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateUser_success() {
        UpdateUserRequest updateReq = new UpdateUserRequest();
        updateReq.setEmail("new@mail.com");

        User user = new User();
        user.setId(1L);

        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(userService.updateUser(any(User.class))).thenReturn(user);

        ResponseEntity<?> response = userController.updateUser(1L, updateReq, httpServletRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof UserResponse);
    }

    @Test
    void updateUser_notFound() {
        UpdateUserRequest updateReq = new UpdateUserRequest();

        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.updateUser(1L, updateReq, httpServletRequest);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateUser_forbidden() {
        UpdateUserRequest updateReq = new UpdateUserRequest();
        User user = new User();
        user.setId(2L);

        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(2L);
        when(userService.getUserById(2L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.updateUser(1L, updateReq, httpServletRequest);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deleteUser_success() {
        User user = new User();
        user.setId(1L);
        user.setAccounts(Collections.emptyList());

        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.deleteUser(1L, httpServletRequest);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_notFound() {
        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.deleteUser(1L, httpServletRequest);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteUser_forbidden() {
        User user = new User();
        user.setId(2L);

        when(httpServletRequest.getAttribute("authenticatedUserId")).thenReturn(2L);
        when(userService.getUserById(2L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.deleteUser(1L, httpServletRequest);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }



}
