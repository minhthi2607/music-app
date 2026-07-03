package com.code.musicapp.config;

import com.code.musicapp.entity.Role;
import com.code.musicapp.entity.User;
import com.code.musicapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Tu dong tao 1 tai khoan ADMIN mac dinh khi app khoi dong lan dau,
 * de nhom co the dang nhap ngay ma khong can insert tay vao DB.
 * LUU Y: doi mat khau nay ngay sau khi dang nhap lan dau, hoac xoa doan nay truoc khi nop bai / deploy that.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@musicapp.local");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);
            System.out.println(">>> Da tao tai khoan admin mac dinh: username=admin / password=admin123");
        }
    }
}
