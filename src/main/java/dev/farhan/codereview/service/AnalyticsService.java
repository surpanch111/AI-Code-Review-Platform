package dev.farhan.codereview.service;

import dev.farhan.codereview.dto.CategoryCount;
import dev.farhan.codereview.dto.PatternFrequency;
import dev.farhan.codereview.dto.SeverityCount;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;

    public AnalyticsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<CategoryCount> getCategoryCounts() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("category").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "count")
        );
        return mongoTemplate.aggregate(aggregation, "review_findings", CategoryCount.class)
                .getMappedResults();
    }

    public List<SeverityCount> getSeverityDistribution() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("severity").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "count")
        );
        return mongoTemplate.aggregate(aggregation, "review_findings", SeverityCount.class)
                .getMappedResults();
    }

    public List<PatternFrequency> getTopPatterns() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("matchedPatternId").ne(null)),
                Aggregation.group("matchedPatternId").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(10),
                Aggregation.lookup("review_patterns", "_id", "_id", "pattern"),
                Aggregation.unwind("pattern"),
                Aggregation.project()
                        .and("pattern.name").as("patternName")
                        .and("count").as("count")
        );
        return mongoTemplate.aggregate(aggregation, "review_findings", PatternFrequency.class)
                .getMappedResults();
    }
}
