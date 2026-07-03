package com.code.musicapp.controller;

import com.code.musicapp.dto.LoginRequest;
import com.code.musicapp.dto.RegisterRequest;
import com.code.musicapp.exception.DuplicateResourceException;
import com.code.musicapp.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.cookie-name}")
    private String cookieName;

    @Value("${jwt.expiration}")
    private long expirationMs;

    // ===== TRANG DANG KY =====
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(request);
        } catch (DuplicateResourceException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }

        return "redirect:/auth/login?registered=true";
    }

    // ===== TRANG DANG NHAP =====
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request,
                         Model model,
                         HttpServletResponse response) {
        try {
            String token = authService.login(request);

            Cookie cookie = new Cookie(cookieName, token);
            cookie.setHttpOnly(true); // JS khong doc duoc -> chong XSS lay token
            cookie.setPath("/");
            cookie.setMaxAge((int) (expirationMs / 1000));
            // cookie.setSecure(true); // BAT len khi deploy that voi HTTPS
            response.addCookie(cookie);

            return "redirect:/";
        } catch (BadCredentialsException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
        }
    }

    // ===== DANG XUAT =====
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // xoa cookie ngay lap tuc
        response.addCookie(cookie);

        return "redirect:/auth/login?logout=true";
    }
}
