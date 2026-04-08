package dev.farhan.codereview.repository;

import dev.farhan.codereview.model.ReviewPattern;
import dev.farhan.codereview.model.Severity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewPatternRepository extends MongoRepository<ReviewPattern, String> {

    List<ReviewPattern> findByLanguage(String language);

    List<ReviewPattern> findByCategory(String category);

    List<ReviewPattern> findByLanguageAndCategory(String language, String category);

    List<ReviewPattern> findBySeverity(Severity severity);
}
