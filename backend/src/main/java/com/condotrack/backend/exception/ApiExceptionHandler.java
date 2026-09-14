// package com.condotrack.backend.exception;

// import org.springframework.http.HttpStatus;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.ResponseStatus;
// import org.springframework.web.bind.annotation.RestControllerAdvice;

// import java.util.Map;

// @RestControllerAdvice
// public class ApiExceptionHandler {
//     @ExceptionHandler(IllegalArgumentException.class)
//     @ResponseStatus(HttpStatus.NOT_FOUND)
//     public Map<String, String> handleIllegalArgument(IllegalArgumentException exception) {
//         return Map.of("error", exception.getMessage());
//     }
// }


//----------------- milestone 1 -------------------
// package com.condotrack.backend.exception;

// import org.springframework.http.HttpStatus;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.ResponseStatus;
// import org.springframework.web.bind.annotation.RestControllerAdvice;

// import java.util.Map;

// @RestControllerAdvice
// public class ApiExceptionHandler {

//     @ExceptionHandler(AuthenticationException.class)
//     @ResponseStatus(HttpStatus.UNAUTHORIZED)
//     public Map<String, String> handleAuthenticationException(AuthenticationException exception) {
//         return Map.of("error", "Invalid email or password");
//     }
//     @ExceptionHandler(IllegalArgumentException.class)
//     @ResponseStatus(HttpStatus.NOT_FOUND)
//     public Map<String, String> handleIllegalArgument(IllegalArgumentException exception) {
//         return Map.of("error", exception.getMessage());
//     }
// }

//----------------- milestone 2 -------------------
package com.condotrack.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleAuthenticationException(AuthenticationException exception) {
        return Map.of("error", "Invalid email or password");
    }
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleIllegalState(IllegalStateException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid request");

        return Map.of("error", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleIllegalArgument(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }
}
