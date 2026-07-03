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

    // Trang danh sach + quan ly toan bo user
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // ===== TRANG SUA DAY DU THONG TIN USER =====
    @GetMapping("/users/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user id=" + id));

        AdminUpdateUserRequest form = new AdminUpdateUserRequest();
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setRole(user.getRole());
        form.setEnabled(user.isEnabled());

        model.addAttribute("targetUser", user);
        model.addAttribute("adminUpdateUserRequest", form);
        return "admin/edit-user";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                              @Valid @ModelAttribute("adminUpdateUserRequest") AdminUpdateUserRequest request,
                              BindingResult bindingResult,
                              Authentication currentUser,
                              Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user id=" + id));

        boolean isEditingSelf = user.getUsername().equals(currentUser.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("targetUser", user);
            return "admin/edit-user";
        }

        // Khong cho tu doi username cua chinh minh: username la subject cua JWT,
        // doi xong token cu se khong tim thay user -> bi dang xuat "bat ngo" giua chung.
        if (isEditingSelf && !request.getUsername().equals(user.getUsername())) {
            model.addAttribute("targetUser", user);
            model.addAttribute("errorMessage", "Khong the tu doi username cua chinh minh");
            return "admin/edit-user";
        }

        // Khong cho tu ha quyen hoac tu khoa chinh minh (giu nhat quan voi cac action rieng le)
        if (isEditingSelf && (request.getRole() != Role.ADMIN || !request.isEnabled())) {
            model.addAttribute("targetUser", user);
            model.addAttribute("errorMessage", "Khong the tu ha quyen hoac tu khoa chinh minh");
            return "admin/edit-user";
        }

        // Kiem tra trung username/email voi user khac (loai tru chinh user dang sua)
        boolean usernameTaken = userRepository.findByUsername(request.getUsername())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .isPresent();
        if (usernameTaken) {
            model.addAttribute("targetUser", user);
            model.addAttribute("errorMessage", "Username nay da duoc su dung boi tai khoan khac");
            return "admin/edit-user";
        }

        boolean emailTaken = userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .isPresent();
        if (emailTaken) {
            model.addAttribute("targetUser", user);
            model.addAttribute("errorMessage", "Email nay da duoc su dung boi tai khoan khac");
            return "admin/edit-user";
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setEnabled(request.isEnabled());
        userRepository.save(user);

        return "redirect:/admin/users?updated=true";
    }

    // Khoa / mo tai khoan
    @PostMapping("/users/{id}/toggle-enabled")
    public String toggleEnabled(@PathVariable Long id, Authentication currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user id=" + id));

        // Khong cho admin tu khoa chinh minh -> tranh tinh huong bi khoa het quyen truy cap
        if (user.getUsername().equals(currentUser.getName())) {
            return "redirect:/admin/users?error=cannot_disable_self";
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        return "redirect:/admin/users";
    }

    // Doi role giua USER va ADMIN
    @PostMapping("/users/{id}/change-role")
    public String changeRole(@PathVariable Long id,
                              @RequestParam Role role,
                              Authentication currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user id=" + id));

        // Khong cho admin tu ha quyen chinh minh
        if (user.getUsername().equals(currentUser.getName()) && role != Role.ADMIN) {
            return "redirect:/admin/users?error=cannot_demote_self";
        }

        user.setRole(role);
        userRepository.save(user);

        return "redirect:/admin/users";
    }

    // Xoa user
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Authentication currentUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay user id=" + id));

        if (user.getUsername().equals(currentUser.getName())) {
            return "redirect:/admin/users?error=cannot_delete_self";
        }

        userRepository.delete(user);
        return "redirect:/admin/users";
    }
}
