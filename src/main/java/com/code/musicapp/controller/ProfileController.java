package com.code.musicapp.controller;

import com.code.musicapp.dto.UpdateProfileRequest;
import com.code.musicapp.entity.User;
import com.code.musicapp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Trang ho so ca nhan - danh cho USER (va ca ADMIN) tu quan ly thong tin cua minh.
 * Da duoc bao ve boi SecurityConfig: "/profile/**" -> authenticated().
 *
 * Luu y quan trong: KHONG cho doi username o day, vi username la subject cua JWT
 * (xem JwtUtil.generateToken). Neu doi username ma khong bat user dang nhap lai,
 * token cu se tro toi username khong con ton tai -> loi xac thuc kho hieu.
 * Neu sau nay muon cho doi username, phai revoke token cu va bat dang nhap lai.
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);

        UpdateProfileRequest form = new UpdateProfileRequest();
        form.setEmail(user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("updateProfileRequest", form);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@Valid @ModelAttribute("updateProfileRequest") UpdateProfileRequest request,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 Model model) {
        User user = getCurrentUser(authentication);

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "profile/edit";
        }

        // 1. Xac nhan mat khau hien tai truoc khi cho sua bat ky thu gi
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", "Mat khau hien tai khong dung");
            return "profile/edit";
        }

        // 2. Doi email (neu co thay doi) - kiem tra trung voi user khac
        if (!request.getEmail().equalsIgnoreCase(user.getEmail())) {
            boolean emailTaken = userRepository.findByEmail(request.getEmail())
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .isPresent();

            if (emailTaken) {
                model.addAttribute("user", user);
                model.addAttribute("errorMessage", "Email nay da duoc su dung boi tai khoan khac");
                return "profile/edit";
            }
            user.setEmail(request.getEmail());
        }

        // 3. Doi mat khau moi - chi khi nguoi dung co nhap (de trong = giu nguyen)
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getNewPassword().length() < 6) {
                model.addAttribute("user", user);
                model.addAttribute("errorMessage", "Mat khau moi phai it nhat 6 ky tu");
                return "profile/edit";
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);

        // Tao lai form voi email moi nhat, khong echo lai mat khau ra form
        UpdateProfileRequest freshForm = new UpdateProfileRequest();
        freshForm.setEmail(user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("updateProfileRequest", freshForm);
        model.addAttribute("successMessage", "Cap nhat ho so thanh cong");

        return "profile/edit";
    }

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Khong tim thay user dang dang nhap: " + username));
    }
}
