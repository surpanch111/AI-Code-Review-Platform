package dev.farhan.codereview.repository;

import dev.farhan.codereview.model.CodeSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CodeSubmissionRepository extends MongoRepository<CodeSubmission, String> {
}
