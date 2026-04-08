package dev.farhan.codereview.dto;

public record ReviewRequest(
        String code,
        String language,
        String fileName
) {
}
