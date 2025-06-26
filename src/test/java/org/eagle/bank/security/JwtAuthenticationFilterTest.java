package org.eagle.bank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eagle.bank.model.User;
import org.eagle.bank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness =  Strictness.LENIENT)
class JwtAuthenticationFilterTest {

    @Mock
    JwtUtil jwtUtil;

    @Mock
    UserService userService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_publicEndpoint_skipsJwtProcessing() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/v1/users");
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userService);
    }

    @Test
    void doFilterInternal_noAuthHeader_callsNextFilter() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/v1/protected");
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidAuthHeader_callsNextFilter() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/v1/protected");
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validJwt_userFound_setsAuthAndUserId() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/v1/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("john");
        when(jwtUtil.validateToken("valid.jwt.token", "john")).thenReturn(true);

        User user = new User();
        user.setId(42L);
        user.setUsername("john");
        when(userService.getUserByUsername("john")).thenReturn(Optional.of(user));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("authenticatedUserId", 42L);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validJwt_userNotFound_setsAuthButNoUserId() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/v1/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("john");
        when(jwtUtil.validateToken("valid.jwt.token", "john")).thenReturn(true);
        when(userService.getUserByUsername("john")).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request, never()).setAttribute(eq("authenticatedUserId"), any());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidJwt_doesNotSetAuth() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/v1/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.jwt.token");
        when(jwtUtil.extractUsername("invalid.jwt.token")).thenReturn("john");
        when(jwtUtil.validateToken("invalid.jwt.token", "john")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}