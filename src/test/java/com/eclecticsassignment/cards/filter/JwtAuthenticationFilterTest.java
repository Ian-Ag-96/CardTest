package com.eclecticsassignment.cards.filter;

import com.eclecticsassignment.cards.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.*;

import java.util.Collections;

class JwtAuthenticationFilterTest {

    @Mock
    private UserDetailsService userDetailsService;
    
    @Mock
    private JwtUtil jwtUtil;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(userDetailsService, jwtUtil);
    }

    @Test
    void testDoFilterInternal_ValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());

        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtUtil.extractUsername("valid-token")).thenReturn("test@example.com");
        when(jwtUtil.isTokenValid("valid-token", userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, (req, res) -> {});

        verify(userDetailsService, times(1)).loadUserByUsername("test@example.com");
    }
}
