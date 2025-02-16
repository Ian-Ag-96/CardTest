package com.eclecticsassignment.cards.repository;

import com.eclecticsassignment.cards.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGetUserByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        userRepository.save(user);

        User fetchedUser = userRepository.getUserByEmail("test@example.com");

        assertNotNull(fetchedUser);
        assertEquals("test@example.com", fetchedUser.getEmail());
    }
}
