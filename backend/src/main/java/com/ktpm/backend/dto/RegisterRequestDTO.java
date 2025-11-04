package com.ktpm.backend.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String verifyPassword;
}
