package ru.netology.cloudservice.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidCredentialsException extends AuthenticationException {

    public InvalidCredentialsException(String msg) {
        super(msg);
    }

}