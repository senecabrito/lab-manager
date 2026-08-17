package com.seneca_brito.lab_manager.infrastructure.security.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void continuesWithoutAuthenticationWhenHeaderIsAbsent() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenProvider, userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void ignoresMalformedAuthorizationHeader() throws Exception {
        request.addHeader("Authorization", "Basic credentials");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenProvider, userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doesNotAuthenticateInvalidBearerToken() throws Exception {
        request.addHeader("Authorization", "Bearer invalid-token");
        when(tokenProvider.isTokenValid("invalid-token")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenProvider).isTokenValid("invalid-token");
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void authenticatesValidBearerTokenAndContinuesChain() throws Exception {
        UserDetails user = User.withUsername("usuario@example.com")
                .password("encoded")
                .authorities("USUARIO")
                .build();
        request.addHeader("Authorization", "Bearer valid-token");
        when(tokenProvider.isTokenValid("valid-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("valid-token")).thenReturn("usuario@example.com");
        when(userDetailsService.loadUserByUsername("usuario@example.com")).thenReturn(user);

        filter.doFilter(request, response, filterChain);

        assertSame(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals("usuario@example.com",
                SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }
}
