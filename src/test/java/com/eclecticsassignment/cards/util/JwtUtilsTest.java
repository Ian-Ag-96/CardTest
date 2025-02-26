package com.eclecticsassignment.cards.util;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class JwtUtilTest {

    @Mock
    private UserDetails mockUserDetails;
    
    @Autowired
    private JwtUtil jwtUtil;

    // Tests the process of token generation
    @Test
    void testGenerateToken() {
    	log.info("Generate token test begin..........");
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());

        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(token.startsWith("eyJ"));
        
        log.info("Generate token test end..........");
    }

    //Tests for username extraction from a generated token
    @Test
    void testExtractUsername() {
    	log.info("Extract username test begin..........");
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);

        String username = jwtUtil.extractUsername(token);

        assertEquals("test@example.com", username);
        log.info("Extract username test end..........");
    }

    //Tests whether a generated token is valid
    @Test
    void testIsTokenValid() {
    	log.info(" Token validity test begin..........");
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);

        assertTrue(jwtUtil.isTokenValid(token, userDetails));
        log.info("Token validity test end..........");
    }

    //Tests whether a token is expired
    @Test
    void testIsTokenExpired() {
    	log.info("Token expired test begin..........");
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);

        assertFalse(jwtUtil.isTokenExpired(token));
        log.info("Token expired test end..........");
    }
}
