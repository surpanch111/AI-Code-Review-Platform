package dev.farhan.codereview.dto;

import org.springframework.data.annotation.Id;

public class SeverityCount {

    @Id
    private String severity;
    private long count;

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
