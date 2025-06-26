package org.eagle.bank.service;

import org.eagle.bank.model.User;
import org.eagle.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness =  Strictness.LENIENT)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("rawpass");
        user.setEmail("john@example.com");
    }

    @Test
    void createUser_encodesPasswordAndSaves() {
        when(passwordEncoder.encode("rawpass")).thenReturn("encodedpass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(user);

        assertEquals("encodedpass", created.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserById(1L);
        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }

    @Test
    void getUserByUsername_returnsUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserByUsername("john");
        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }

    @Test
    void getUserByEmail_returnsUser() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserByEmail("john@example.com");
        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
    }

    @Test
    void updateUser_savesUser() {
        when(userRepository.save(user)).thenReturn(user);
        User updated = userService.updateUser(user);
        assertEquals(user, updated);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_deletesById() {
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void authenticateUser_success() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawpass", "rawpass")).thenReturn(true);

        Optional<User> result = userService.authenticateUser("john", "rawpass");
        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
    }

    @Test
    void authenticateUser_wrongPassword_returnsEmpty() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "rawpass")).thenReturn(false);

        Optional<User> result = userService.authenticateUser("john", "wrongpass");
        assertFalse(result.isPresent());
    }

    @Test
    void authenticateUser_userNotFound_returnsEmpty() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        Optional<User> result = userService.authenticateUser("unknown", "any");
        assertFalse(result.isPresent());
    }
}