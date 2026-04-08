package dev.farhan.codereview.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "review_patterns")
public class ReviewPattern {

    @Id
    private String id;
    private String name;
    private String description;
    private String language;
    private Severity severity;
    private String category;
    private String exampleBadCode;
    private String exampleGoodCode;
    private String explanation;
    @JsonIgnore
    private float[] embedding;
    @Transient
    private double searchScore;

    public ReviewPattern() {
    }

    public ReviewPattern(String name, String description, String language, Severity severity,
                         String category, String exampleBadCode, String exampleGoodCode,
                         String explanation) {
        this.name = name;
        this.description = description;
        this.language = language;
        this.severity = severity;
        this.category = category;
        this.exampleBadCode = exampleBadCode;
        this.exampleGoodCode = exampleGoodCode;
        this.explanation = explanation;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getExampleBadCode() { return exampleBadCode; }
    public void setExampleBadCode(String exampleBadCode) { this.exampleBadCode = exampleBadCode; }

    public String getExampleGoodCode() { return exampleGoodCode; }
    public void setExampleGoodCode(String exampleGoodCode) { this.exampleGoodCode = exampleGoodCode; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }

    public double getSearchScore() { return searchScore; }
    public void setSearchScore(double searchScore) { this.searchScore = searchScore; }

    public String buildEmbeddingText() {
        return description + " " + exampleBadCode + " " + explanation;
    }
}
