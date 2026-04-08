package dev.farhan.codereview.dto;

public class PatternFrequency {

    private String patternName;
    private long count;

    public String getPatternName() { return patternName; }
    public void setPatternName(String patternName) { this.patternName = patternName; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
