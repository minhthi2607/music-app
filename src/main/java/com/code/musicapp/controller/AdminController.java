package com.code.musicapp.controller;

import com.code.musicapp.dto.AdminUpdateUserRequest;
import com.code.musicapp.entity.Role;
import com.code.musicapp.entity.User;
import com.code.musicapp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Chi ADMIN moi vao duoc cac endpoint nay (da chan o SecurityConfig: /admin/** -> hasRole("ADMIN"))
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    // Trang dashboard admin - so lieu tong quan
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long totalAdmins = allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count();
        long activeUsers = allUsers.stream().filter(User::isEnabled).count();
        long lockedUsers = totalUsers - activeUsers;

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("lockedUsers", lockedUsers);
        // 5 user moi nhat de hien thi nhanh tren dashboard
        model.addAttribute("recentUsers", allUsers.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .toList());

        return "admin/dashboard";
    }


}
