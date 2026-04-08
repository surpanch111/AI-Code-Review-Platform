package dev.farhan.codereview.repository;

import dev.farhan.codereview.model.ReviewFinding;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewFindingRepository extends MongoRepository<ReviewFinding, String> {

    List<ReviewFinding> findBySubmissionId(String submissionId);
}
