package com.ktpm.backend.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class LoginRequestDTO {
    private String username;
    private String password;
}
