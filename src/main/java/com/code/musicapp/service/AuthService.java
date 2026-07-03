package com.code.musicapp.service;

import com.code.musicapp.dto.LoginRequest;
import com.code.musicapp.dto.RegisterRequest;
import com.code.musicapp.entity.Role;
import com.code.musicapp.entity.User;
import com.code.musicapp.exception.DuplicateResourceException;
import com.code.musicapp.repository.UserRepository;
import com.code.musicapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Dang ky user moi. Mac dinh role = USER, khong cho tu chon ADMIN tu form.
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username da ton tai: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email da duoc su dung: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    // Dang nhap: xac thuc bang AuthenticationManager, neu dung -> tra ve JWT
    public String login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Sai username hoac mat khau");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Sai username hoac mat khau"));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Tai khoan da bi khoa, lien he admin");
        }

        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }
}
