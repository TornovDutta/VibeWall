package org.example.vibewall.repo;

import org.example.vibewall.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

    List<Question> findByTargetUserIdOrderByCreatedAtDesc(String targetUserId);

    List<Question> findByTargetUserIdAndStatusAndIsPublicTrueOrderByAnsweredAtDesc(String targetUserId, String status);

    List<Question> findByTargetUserIdAndStatusOrderByCreatedAtDesc(String targetUserId, String status);

    List<Question> findByAskerUserIdOrderByCreatedAtDesc(String askerUserId);

    long countByTargetUserIdAndStatus(String targetUserId, String status);
}
