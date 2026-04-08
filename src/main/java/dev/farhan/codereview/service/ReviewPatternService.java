package dev.farhan.codereview.service;

import dev.farhan.codereview.dto.CreatePatternRequest;
import dev.farhan.codereview.model.ReviewPattern;
import dev.farhan.codereview.repository.ReviewPatternRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewPatternService {

    private final ReviewPatternRepository patternRepository;
    private final EmbeddingModel embeddingModel;

    public ReviewPatternService(ReviewPatternRepository patternRepository,
                                EmbeddingModel embeddingModel) {
        this.patternRepository = patternRepository;
        this.embeddingModel = embeddingModel;
    }

    public ReviewPattern createPattern(CreatePatternRequest request) {
        ReviewPattern pattern = new ReviewPattern(
                request.name(),
                request.description(),
                request.language(),
                request.severity(),
                request.category(),
                request.exampleBadCode(),
                request.exampleGoodCode(),
                request.explanation()
        );

        pattern.setEmbedding(embeddingModel.embed(pattern.buildEmbeddingText()));

        return patternRepository.save(pattern);
    }

    public List<ReviewPattern> listPatterns(String language, String category) {
        if (language != null && category != null) {
            return patternRepository.findByLanguageAndCategory(language, category);
        }
        if (language != null) {
            return patternRepository.findByLanguage(language);
        }
        if (category != null) {
            return patternRepository.findByCategory(category);
        }
        return patternRepository.findAll();
    }

    public Optional<ReviewPattern> getPattern(String id) {
        return patternRepository.findById(id);
    }
}
