package org.example.vibewall.repo;

import org.example.vibewall.model.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepo extends MongoRepository<Feedback,String> {
    void removeById(String id);
}
