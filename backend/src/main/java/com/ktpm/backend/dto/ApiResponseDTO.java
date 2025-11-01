package com.ktpm.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponseDTO<T> {
    private int status;
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponseDTO(int status, boolean success, String message, T data) {
        this.status = status;
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return new ApiResponseDTO<>(200, true, message, data);
    }

    public static <T> ApiResponseDTO<T> error(int status, String message) {
        return new ApiResponseDTO<>(status, false, message, null);
    }
}
