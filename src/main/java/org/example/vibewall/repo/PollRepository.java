package org.example.vibewall.repo;

import org.example.vibewall.model.Poll;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollRepository extends MongoRepository<Poll, String> {
    List<Poll> findAllByOrderByCreatedAtDesc();
    List<Poll> findByUserIdOrderByCreatedAtDesc(String userId);
}
