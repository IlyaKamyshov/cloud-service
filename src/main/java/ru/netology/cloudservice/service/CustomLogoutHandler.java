package ru.netology.cloudservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import ru.netology.cloudservice.exception.InvalidCredentialsException;

@Service
public class CustomLogoutHandler implements LogoutHandler {

    @Value("${token.header}")
    private String tokenHeader;

    private final TokenService tokenService;

    public CustomLogoutHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        String token = request.getHeader(tokenHeader).substring(7);
        if (!tokenService.deleteToken(token)) {
            throw new InvalidCredentialsException("Требуется авторизация");
        }

    }

}
