package ru.netology.cloudservice.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.netology.cloudservice.logger.CloudServiceLogger;
import ru.netology.cloudservice.repository.TokenRepository;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final CloudServiceLogger logger;

    public TokenService(TokenRepository tokenRepository, CloudServiceLogger logger) {
        this.tokenRepository = tokenRepository;
        this.logger = logger;
    }

    public String generateToken(UserDetails user) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[128/*32*/];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String token = encoder.encodeToString(bytes);
        tokenRepository.addToken(token, user);
        return token;
    }

    public boolean deleteToken(String token) {

        boolean flag = false;

        String user = tokenRepository.getUserByToken(token).getUsername();

        if (user != null) {
            logger.logInfo("Пользователь " + user + " успешно вышел из системы");
            tokenRepository.deleteToken(token);
            flag = true;
        }

        return flag;

    }

}