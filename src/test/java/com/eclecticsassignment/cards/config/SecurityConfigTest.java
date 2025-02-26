package com.eclecticsassignment.cards.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class SecurityConfigTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Tests whether an AuthenticationManager object can be created
    @Test
    void testAuthenticationManagerBean() {
    	log.info("Authentication manager test begin..........");
        assertNotNull(authenticationManager);
        log.info("Authentication manager test end..........");
    }

    //Tests whether an PasswordEncoder object can be created and is functional
    @Test
    void testPasswordEncoderBean() {
    	log.info("Password encoder test begin..........");
        String rawPassword = "password";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        log.info("Password encoder test end..........");
    }
}