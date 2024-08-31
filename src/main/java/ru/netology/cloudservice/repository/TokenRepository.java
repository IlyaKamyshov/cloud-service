package ru.netology.cloudservice.repository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TokenRepository {

    private final Map<String, UserDetails> tokenStorage = new ConcurrentHashMap<>();

    public void addToken(String token, UserDetails user) {
        tokenStorage.put(token, user);
    }

    public UserDetails getUserByToken(String token) {
        return tokenStorage.get(token);
    }

    public void deleteToken(String token) {
        tokenStorage.remove(token);
    }

}