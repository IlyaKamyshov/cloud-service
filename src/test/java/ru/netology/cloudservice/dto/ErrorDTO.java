package ru.netology.cloudservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorDTO(@JsonProperty("id") String id, @JsonProperty("message") String message) {
}
