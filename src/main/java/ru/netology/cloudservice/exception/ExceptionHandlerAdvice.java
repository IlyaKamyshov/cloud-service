package ru.netology.cloudservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.cloudservice.logger.CloudServiceLogger;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    private final CloudServiceLogger logger;

    public ExceptionHandlerAdvice(CloudServiceLogger logger) {
        this.logger = logger;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException e) {
        UUID id = UUID.randomUUID();
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            errors.put("message", errorMessage);
            errors.put("id", String.valueOf(id));
        });
        logger.logError(errors.get("id") + " " + errors.get("message"));
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidInputDataException.class)
    public Map<String, String> handleInputDataException(InvalidInputDataException e) {
        return getErrorMessage(e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public Map<String, String> handleAuthenticationException(AuthenticationException e) {
        return getErrorMessage(e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UsernameNotFoundException.class)
    public Map<String, String> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return getErrorMessage(e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({NoSuchFileException.class, IOException.class})
    public Map<String, String> handleClientException() {
        return getErrorMessage("Внутренняя ошибка сервера");
    }

    private Map<String, String> getErrorMessage(String message) {
        UUID id = UUID.randomUUID();
        Map<String, String> errors = new HashMap<>();
        errors.put("message", message);
        errors.put("id", String.valueOf(id));
        logger.logError(id + " " + message);
        return errors;
    }

}