package com.code.musicapp.dto;

import com.code.musicapp.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {

    @NotBlank(message = "Username khong duoc de trong")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    private String email;

    private Role role;

    private boolean enabled;
}
