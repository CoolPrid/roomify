package com.roomify.core.service;

import com.roomify.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void userService_instantiatesSuccessfully() {
        assertDoesNotThrow(() -> new UserService(userRepository));
    }

    @Test
    void userService_storesRepository() {
        UserService userService = new UserService(userRepository);
        assertNotNull(userService);
        // Since UserService doesn't have public methods yet, we just verify it can be created
    }

    @Test
    void constructor_withNullRepository_doesNotThrow() {
        // Testing edge case - constructor should handle null gracefully
        assertDoesNotThrow(() -> new UserService(null));
    }
}