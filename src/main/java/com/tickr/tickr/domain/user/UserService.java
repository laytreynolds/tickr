package com.tickr.tickr.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tickr.tickr.dto.CreateUserRequest;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public User createUser(CreateUserRequest request) {

        User user = User.builder()
        .phoneNumber(request.getPhoneNumber())
        .timezone(request.getTimezone())
        .build();

        return userRepository.save(user);
    }
}

