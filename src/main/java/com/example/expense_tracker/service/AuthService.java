package com.example.expense_tracker.service;

import com.example.expense_tracker.dto.LoginRequest;
import com.example.expense_tracker.dto.LoginResponse;
import com.example.expense_tracker.dto.RegisterRequest;
import com.example.expense_tracker.dto.RegisterResponse;
import com.example.expense_tracker.exception.EmailAlreadyExistsException;
import com.example.expense_tracker.exception.InvalidCredentialsException;
import com.example.expense_tracker.model.AppUser;
import com.example.expense_tracker.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {

        if (appUserRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        String passwordHash = passwordEncoder.encode(request.password());

        AppUser appUser = new AppUser(
                null,
                request.email(),
                passwordHash
        );

        AppUser savedUser = appUserRepository.save(appUser);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail()
        );
    }

    public LoginResponse login(LoginRequest request) {

        AppUser user = appUserRepository
                .findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                token
        );
    }

}