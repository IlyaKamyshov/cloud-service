package ru.netology.cloudservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FileRenameDTO(
        @NotBlank(message = "Значение filename не может быть пустым")
        @NotNull(message = "Значение filename не может быть пустым")
        @JsonProperty("filename")
        String fileName
) {
}