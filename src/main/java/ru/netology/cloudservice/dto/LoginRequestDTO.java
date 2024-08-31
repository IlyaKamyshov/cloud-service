package ru.netology.cloudservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record LoginRequestDTO(

        @NotBlank(message = "Значение login не может быть пустым")
        @NotNull(message = "Значение login не может быть null")
        String login,

        @NotBlank(message = "Значение password не может быть пустым")
        @NotNull(message = "Значение password не может быть null")
        String password

) {
}