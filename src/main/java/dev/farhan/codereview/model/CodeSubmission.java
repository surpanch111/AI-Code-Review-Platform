package dev.farhan.codereview.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "code_submissions")
public class CodeSubmission {

    @Id
    private String id;
    private String code;
    private String language;
    private String fileName;
    private String submittedBy;
    private Instant submittedAt;
    private List<String> findingIds;

    public CodeSubmission() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public List<String> getFindingIds() { return findingIds; }
    public void setFindingIds(List<String> findingIds) { this.findingIds = findingIds; }
}
