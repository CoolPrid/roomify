package com.roomify.core.repository;

import com.roomify.core.dto.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);
}
