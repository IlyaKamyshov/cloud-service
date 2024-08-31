package ru.netology.cloudservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDTO(@JsonProperty("auth-token") String token) {
}