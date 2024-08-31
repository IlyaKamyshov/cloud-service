package ru.netology.cloudservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FileInfoDTO(@JsonProperty("filename") String fileName, long size) {
}
