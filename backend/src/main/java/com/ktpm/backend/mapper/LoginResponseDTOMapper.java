package com.ktpm.backend.mapper;

import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.entity.User;

public class LoginResponseDTOMapper {
    public static LoginResponseDTO toLoginResponseDTO(User user) {
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setId(user.getId());
        loginResponseDTO.setUsername(user.getUsername());

        return loginResponseDTO;
    }
}
