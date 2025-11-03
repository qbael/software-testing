package com.ktpm.backend.utils;

public class Validator {

    public static boolean isBlank(String str) {
        str = str == null ? null : str.trim();
        return str == null || str.trim().isEmpty();
    }

    public static boolean isSizeInRange(String str, int min, int max) {
        if (str == null) return false;
        int length = str.length();
        return length >= min && length <= max;
    }

    public static boolean isValidUsername(String username) {
        String usernameRegex = "^[a-zA-Z0-9._-]{3,50}$";
        username = username == null ? null : username.trim();
        return username != null && !username.isEmpty() && username.matches(usernameRegex);
    }

    public static boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,100}$";
        password = password == null ? null : password.trim();
        return password != null && !password.isEmpty() && password.matches(passwordRegex);
    }
}
