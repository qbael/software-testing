package com.ktpm.backend.controller;

import com.ktpm.backend.dto.LoginRequestDTO;
import com.ktpm.backend.dto.LoginResponseDTO;
import com.ktpm.backend.dto.RegisterRequestDTO;
import com.ktpm.backend.exception.UserNotFoundException;
import com.ktpm.backend.exception.UsernameExistedException;
import com.ktpm.backend.exception.VerifyPasswordNotMatch;
import com.ktpm.backend.exception.WrongPassWordException;
import com.ktpm.backend.service.AuthService;
import com.ktpm.backend.utils.JwtUtil;
import com.ktpm.backend.utils.Validator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(
            @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse response
    ) {
        String username = loginRequestDTO.getUsername();
        String password = loginRequestDTO.getPassword();

        try {
            LoginResponseDTO loginResponseDTO = authService.authenticate(username, password);
            String token = jwtUtil.generateToken(loginResponseDTO.getId(), loginResponseDTO.getUsername());

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60 * 3);
            response.addCookie(cookie);

            return ResponseEntity.ok(loginResponseDTO);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (WrongPassWordException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        String username = registerRequestDTO.getUsername();
        String password = registerRequestDTO.getPassword();
        String verifyPassword = registerRequestDTO.getVerifyPassword();

        if (Validator.isBlank(username) || Validator.isBlank(password) || Validator.isBlank(verifyPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!Validator.isValidUsername(username) || !Validator.isValidPassword(password)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            boolean success = authService.register(registerRequestDTO);
            if (!success) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (UsernameExistedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (VerifyPasswordNotMatch e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/current")
    public ResponseEntity<LoginResponseDTO> getCurrentUser(
            @CookieValue(value = "token", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(authService.getCurrentUser(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}
