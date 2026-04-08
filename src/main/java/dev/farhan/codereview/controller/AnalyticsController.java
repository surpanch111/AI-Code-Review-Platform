package dev.farhan.codereview.controller;

import dev.farhan.codereview.dto.CategoryCount;
import dev.farhan.codereview.dto.PatternFrequency;
import dev.farhan.codereview.dto.SeverityCount;
import dev.farhan.codereview.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/categories")
    public List<CategoryCount> getCategoryCounts() {
        return analyticsService.getCategoryCounts();
    }

    @GetMapping("/severity")
    public List<SeverityCount> getSeverityDistribution() {
        return analyticsService.getSeverityDistribution();
    }

    @GetMapping("/top-patterns")
    public List<PatternFrequency> getTopPatterns() {
        return analyticsService.getTopPatterns();
    }
}
