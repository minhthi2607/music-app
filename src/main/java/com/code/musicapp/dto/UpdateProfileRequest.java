package com.code.musicapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    private String email;

    // Bat buoc nhap dung mat khau hien tai moi cho phep sua - tranh truong hop
    // ai do chiem duoc phien dang nhap (session/cookie) roi doi thong tin tai khoan
    @NotBlank(message = "Vui long nhap mat khau hien tai de xac nhan")
    private String currentPassword;

    // Optional - de trong neu khong muon doi mat khau
    private String newPassword;
}
