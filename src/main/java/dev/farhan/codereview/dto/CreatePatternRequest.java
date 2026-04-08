package dev.farhan.codereview.dto;

import dev.farhan.codereview.model.Severity;

public record CreatePatternRequest(
        String name,
        String description,
        String language,
        Severity severity,
        String category,
        String exampleBadCode,
        String exampleGoodCode,
        String explanation
) {
}
