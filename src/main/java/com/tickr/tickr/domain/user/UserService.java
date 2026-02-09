package com.tickr.tickr.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tickr.tickr.dto.CreateUserRequest;
import com.tickr.tickr.util.Timezone;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public User createUser(CreateUserRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String timezone = request.getTimezone();
        Timezone.validateTimezone(timezone);

        return userRepository.save(User.builder()
                .phoneNumber(request.getPhoneNumber())
                .timezone(timezone)
                .passwordHash(passwordEncoder.encode(request.getPassword())).build());
    }

}
