package com.eclecticsassignment.cards.repository;

import com.eclecticsassignment.cards.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGetUserByEmail() {
        String email = "test@example.com";
        User user = new User(email, "password123", "Member");
        userRepository.save(user);

        User result = userRepository.getUserByEmail(email);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getRole()).isEqualTo("Member");
        // Purpose: This test verifies that the `getUserByEmail` query correctly retrieves a user by email.
    }

    @Test
    void testGetUserByEmail_NotFound() {
        User result = userRepository.getUserByEmail("nonexistent@example.com");

        assertThat(result).isNull();
        // Purpose: This test verifies that the `getUserByEmail` query returns null when no user with the given email exists.
    }
}
