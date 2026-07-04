package com.code.musicapp.config;

import com.code.musicapp.security.CustomUserDetailsService;
import com.code.musicapp.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // cho phep dung @PreAuthorize tren service/controller neu can
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // dang dung JWT qua cookie, khong dung session-based form -> tam tat CSRF cho don gian trong scope demo
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Tai nguyen tinh + trang public
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        .requestMatchers("/", "/auth/**").permitAll()

                        // Song: XEM/NGHE la public, con TAO/SUA/XOA bat buoc dang nhap
                        .requestMatchers(HttpMethod.GET, "/songs", "/songs/detail/**").permitAll()
                        .requestMatchers("/songs/upload", "/songs/edit/**", "/songs/delete/**", "/songs/update/**")
                        .authenticated()

                        // Category: XEM la public, quan ly thi loai chi ADMIN
                        .requestMatchers(HttpMethod.GET, "/categories").permitAll()
                        .requestMatchers("/categories/create", "/categories/edit/**",
                                "/categories/delete/**", "/categories/update/**").hasRole("ADMIN")

                        // Khu vuc rieng cho ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Khu vuc can dang nhap (USER hoac ADMIN deu duoc)
                        .requestMatchers("/playlists/**", "/profile/**").authenticated()
                        // Con lai mac dinh cho phep, cac module khac (Playlist cua nguoi 3) se tu sua lai matcher rieng
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider())
                // Khi truy cap trang can quyen ma chua dang nhap / khong du quyen -> redirect ve login thay vi 403 trang trang
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect("/auth/login"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendRedirect("/auth/login?error=access_denied"))
                );

        return http.build();
    }
}