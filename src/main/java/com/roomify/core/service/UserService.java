package com.roomify.core.service;

import com.roomify.core.repository.UserRepository;

public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) { this.userRepository = userRepository; }
}
