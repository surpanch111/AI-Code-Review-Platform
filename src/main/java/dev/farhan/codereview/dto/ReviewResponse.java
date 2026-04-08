package dev.farhan.codereview.dto;

import dev.farhan.codereview.model.CodeSubmission;
import dev.farhan.codereview.model.ReviewFinding;

import java.util.List;

public record ReviewResponse(
        CodeSubmission submission,
        List<ReviewFinding> findings
) {
}
