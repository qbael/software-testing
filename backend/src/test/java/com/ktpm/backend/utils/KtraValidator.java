package com.ktpm.backend.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KtraValidator {

    @ParameterizedTest
    @NullAndEmptySource
    void isBlankNullOrEmptyTrue(String str) {
        assertTrue(Validator.isBlank(str));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\n"})
    void isBlankWhiteSpaceReturnTrue(String str) {
        assertTrue(Validator.isBlank(str));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", " ok"})
    void isBlankNotBlankReturnFalse(String str) {
        assertFalse(Validator.isBlank(str));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", " \n"})
    void isUsernameNullOrEmptyOrBlankReturnFalse(String username) {
        assertFalse(Validator.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {"username"})
    void isUsernameValidReturnTrue(String username) {
        assertTrue(Validator.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ab",
            "abc123@!",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
    void isUsernameNotValidReturnFalse(String username) {
        assertFalse(Validator.isValidUsername(username));
    }
}
