package dev.farhan.codereview.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "review_findings")
public class ReviewFinding {

    @Id
    private String id;
    @Indexed
    private String submissionId;
    private String matchedPatternId;
    private int startLine;
    private int endLine;
    private Severity severity;
    private String category;
    private String message;
    private String suggestion;
    private double confidence;

    public ReviewFinding() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }

    public String getMatchedPatternId() { return matchedPatternId; }
    public void setMatchedPatternId(String matchedPatternId) { this.matchedPatternId = matchedPatternId; }

    public int getStartLine() { return startLine; }
    public void setStartLine(int startLine) { this.startLine = startLine; }

    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}
