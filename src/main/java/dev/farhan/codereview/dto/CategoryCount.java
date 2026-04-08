package dev.farhan.codereview.dto;

import org.springframework.data.annotation.Id;

public class CategoryCount {

    @Id
    private String category;
    private long count;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
