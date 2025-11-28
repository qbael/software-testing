package com.ktpm.backend.service;

import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.dto.RegisterRequestDTO;
import com.ktpm.backend.entity.User;
import com.ktpm.backend.exception.UserNotFoundException;
import com.ktpm.backend.exception.UsernameExistedException;
import com.ktpm.backend.exception.VerifyPasswordNotMatch;
import com.ktpm.backend.exception.WrongPassWordException;
import com.ktpm.backend.repository.UserRepository;
import com.ktpm.backend.utils.JwtUtil;
import com.ktpm.backend.utils.Validator;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponseDTO authenticate(String username, String password) {
        if (!Validator.isValidUsername(username) || !Validator.isValidPassword(password)) {
            throw new IllegalArgumentException("Username hoặc mật khẩu không hợp lệ");
        }

        username = Validator.sanitizeInput(username);
        password = Validator.sanitizeInput(password);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new WrongPassWordException("Sai mật khẩu");
        }

        return new LoginResponseDTO(user.getId(), username);
    }

    public LoginResponseDTO getCurrentUser(String token) {
        UUID id = jwtUtil.extractId(token);
        String username = jwtUtil.extractUsername(token);

        return new LoginResponseDTO(id, username);
    }

    public boolean register(RegisterRequestDTO registerRequestDTO) {
        if (userRepository.findByUsername(registerRequestDTO.getUsername()).isPresent()) {
            throw new UsernameExistedException("Tên đăng nhập đã tồn tại");
        }
        if (!registerRequestDTO.getPassword().equals(registerRequestDTO.getVerifyPassword())) {
            throw new VerifyPasswordNotMatch("Mật khẩu xác nhận không khớp");
        }

        User user = new User();
        user.setUsername(registerRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));

        try {
            userRepository.save(user);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void deleteUserById(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));
        userRepository.delete(user);
    }
}
