package com.eclecticsassignment.cards.service;

import org.springframework.stereotype.Service;

import com.eclecticsassignment.cards.entity.User;
import com.eclecticsassignment.cards.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User insertUser(User user) {
        return userRepository.save(user);
    }
}

