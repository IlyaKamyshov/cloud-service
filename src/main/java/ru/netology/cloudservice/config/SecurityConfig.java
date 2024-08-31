package ru.netology.cloudservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import ru.netology.cloudservice.logger.CloudServiceLogger;
import ru.netology.cloudservice.service.CustomLogoutHandler;
import ru.netology.cloudservice.service.CustomUserDetailsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenFilter tokenFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomLogoutHandler logoutHandler;
    private final CloudServiceLogger logger;

    public SecurityConfig(TokenFilter tokenFilter,
                          CustomUserDetailsService userDetailsService,
                          CustomLogoutHandler logoutHandler,
                          CloudServiceLogger logger) {
        this.tokenFilter = tokenFilter;
        this.userDetailsService = userDetailsService;
        this.logoutHandler = logoutHandler;
        this.logger = logger;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOriginPatterns(List.of("*"));
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
                    corsConfiguration.setAllowedHeaders(List.of("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                }))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exh -> exh.authenticationEntryPoint(
                        ((request, response, authException) -> {
                            UUID id = UUID.randomUUID();
                            String message = "Требуется авторизация";
                            Map<String, String> error = new HashMap<>();
                            error.put("message", message);
                            error.put("id", String.valueOf(id));
                            logger.logError(id + " " + message);
                            String errorJson = new ObjectMapper().writeValueAsString(error);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().print(errorJson);
                        })
                ))
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .logout(logout -> logout
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.OK.value())))
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

}
