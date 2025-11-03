package com.ktpm.backend.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private UUID id;
    private String username;
}
