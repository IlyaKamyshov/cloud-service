package ru.netology.cloudservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudservice.exception.InvalidCredentialsException;
import ru.netology.cloudservice.dto.LoginRequestDTO;
import ru.netology.cloudservice.dto.LoginResponseDTO;
import ru.netology.cloudservice.logger.CloudServiceLogger;
import ru.netology.cloudservice.service.TokenService;

@RestController
@RequestMapping("/")
public class AuthenticationController {

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final CloudServiceLogger logger;

    public AuthenticationController(TokenService tokenService, AuthenticationManager authenticationManager, CloudServiceLogger logger) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.logger = logger;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@RequestBody @Valid LoginRequestDTO request) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.login(), request.password()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Неверные имя или пароль");
        }

        String token = tokenService.generateToken((UserDetails) authentication.getPrincipal());
        logger.logInfo("Пользователь " + ((UserDetails) authentication.getPrincipal()).getUsername()
        + " успешно аутентифирован");

        return ResponseEntity.ok(new LoginResponseDTO(token));

    }

}