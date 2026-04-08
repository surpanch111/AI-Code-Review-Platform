package dev.farhan.codereview.service;

import dev.farhan.codereview.dto.ReviewRequest;
import dev.farhan.codereview.dto.ReviewResponse;
import dev.farhan.codereview.model.CodeSubmission;
import dev.farhan.codereview.model.ReviewFinding;
import dev.farhan.codereview.model.ReviewPattern;
import dev.farhan.codereview.repository.CodeSubmissionRepository;
import dev.farhan.codereview.repository.ReviewFindingRepository;
import org.bson.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private final MongoTemplate mongoTemplate;
    private final EmbeddingModel embeddingModel;
    private final ChatClient chatClient;
    private final CodeSubmissionRepository submissionRepository;
    private final ReviewFindingRepository findingRepository;

    public ReviewService(MongoTemplate mongoTemplate,
                         EmbeddingModel embeddingModel,
                         ChatClient.Builder chatClientBuilder,
                         CodeSubmissionRepository submissionRepository,
                         ReviewFindingRepository findingRepository) {
        this.mongoTemplate = mongoTemplate;
        this.embeddingModel = embeddingModel;
        this.chatClient = chatClientBuilder.build();
        this.submissionRepository = submissionRepository;
        this.findingRepository = findingRepository;
    }

    public ReviewResponse reviewCode(ReviewRequest request) {
        if (request.code() == null || request.code().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code must not be empty");
        }

        // Step 1: Save the submission
        CodeSubmission submission = new CodeSubmission();
        submission.setCode(request.code());
        submission.setLanguage(request.language());
        submission.setFileName(request.fileName());
        submission.setSubmittedAt(Instant.now());

        // Step 2: Embed the submitted code and find similar patterns
        float[] codeEmbedding = embeddingModel.embed(request.code());
        List<ReviewPattern> matchedPatterns = findSimilarPatterns(codeEmbedding, 5);

        // Step 3: Build the prompt and call the LLM
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request.code(), matchedPatterns);

        List<ReviewFinding> findings = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(new ParameterizedTypeReference<>() {});

        // Step 4: Save findings and update the submission
        submission = submissionRepository.save(submission);

        for (ReviewFinding finding : findings) {
            finding.setSubmissionId(submission.getId());
        }
        List<ReviewFinding> savedFindings = findingRepository.saveAll(findings);
        List<String> findingIds = savedFindings.stream()
                .map(ReviewFinding::getId)
                .toList();

        submission.setFindingIds(findingIds);
        submissionRepository.save(submission);

        return new ReviewResponse(submission, savedFindings);
    }

    public ReviewResponse getReview(String submissionId) {
        CodeSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Submission not found: " + submissionId));
        List<ReviewFinding> findings = findingRepository.findBySubmissionId(submissionId);
        return new ReviewResponse(submission, findings);
    }

    public List<ReviewFinding> getFindings(String submissionId) {
        return findingRepository.findBySubmissionId(submissionId);
    }

    private List<ReviewPattern> findSimilarPatterns(float[] queryVector, int limit) {
        List<Double> queryVectorList = new ArrayList<>();
        for (float f : queryVector) {
            queryVectorList.add((double) f);
        }

        Document vectorSearchStage = new Document("$vectorSearch",
                new Document("index", "vector_index")
                        .append("path", "embedding")
                        .append("queryVector", queryVectorList)
                        .append("numCandidates", 50)
                        .append("limit", limit));

        AggregationOperation vectorSearch = context -> vectorSearchStage;

        AggregationOperation addScore = context ->
                new Document("$addFields",
                        new Document("searchScore",
                                new Document("$meta", "vectorSearchScore")));

        AggregationOperation excludeEmbedding = context ->
                new Document("$project",
                        new Document("embedding", 0));

        Aggregation aggregation = Aggregation.newAggregation(vectorSearch, addScore, excludeEmbedding);

        AggregationResults<ReviewPattern> results =
                mongoTemplate.aggregate(aggregation, "review_patterns", ReviewPattern.class);

        return results.getMappedResults();
    }

    private String buildSystemPrompt() {
        return """
                You are a senior Java code reviewer. Analyze the submitted code and identify issues.
                You will receive a code snippet and a set of known anti-patterns that matched semantically.
                For each issue you find, return a JSON array of findings. Each finding must have these fields:
                - startLine (int): the line number where the issue starts
                - endLine (int): the line number where the issue ends
                - severity (string): one of "CRITICAL", "WARNING", or "INFO"
                - category (string): one of "security", "performance", "maintainability", "error-handling"
                - message (string): a concise description of the issue
                - suggestion (string): how to fix the issue, with a brief code example if helpful
                - confidence (double): your confidence in this finding, from 0.0 to 1.0
                - matchedPatternId (string or null): if this issue matches one of the provided patterns, include its ID; otherwise null

                Focus on real issues. Do not flag stylistic preferences or minor formatting.
                If you find issues beyond the provided patterns, include them with matchedPatternId as null.
                Return ONLY the JSON array, no additional text.
                """;
    }

    private String buildUserPrompt(String code, List<ReviewPattern> patterns) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("## Code to review\n\n```java\n");
        prompt.append(code);
        prompt.append("\n```\n\n");
        prompt.append("## Known anti-patterns to check against (ranked by similarity)\n\n");

        for (int i = 0; i < patterns.size(); i++) {
            ReviewPattern pattern = patterns.get(i);
            prompt.append(String.format("%d. **%s** (ID: %s, similarity: %.3f)\n",
                    i + 1, pattern.getName(), pattern.getId(), pattern.getSearchScore()));
            prompt.append("   Description: ").append(pattern.getDescription()).append("\n");
            prompt.append("   Example of the problem:\n   ```java\n   ");
            prompt.append(pattern.getExampleBadCode());
            prompt.append("\n   ```\n");
            prompt.append("   Why this is an issue: ").append(pattern.getExplanation()).append("\n\n");
        }

        return prompt.toString();
    }
}
