package com.tickr.tickr.domain.user.auth;

import com.tickr.tickr.domain.user.UserRepository;
import com.tickr.tickr.domain.user.User;

import org.springframework.security.authentication.BadCredentialsException;
import com.tickr.tickr.dto.AuthResponse;
import com.tickr.tickr.dto.AuthRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import com.tickr.tickr.security.JwtService;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse login(AuthRequest request) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    request.phoneNumber(), request.password());
            authenticationManager.authenticate(authToken);

            User user = userRepository.findByPhoneNumber(request.phoneNumber())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            String token = jwtService.generateToken(user);
            AuthResponse response = new AuthResponse(token, "Bearer", jwtService.getExpirationMs());
            return response;
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}