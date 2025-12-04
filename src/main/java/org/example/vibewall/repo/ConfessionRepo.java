package org.example.vibewall.repo;

import org.example.vibewall.model.Confession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfessionRepo extends MongoRepository<Confession,Integer> {


    Optional<Confession> findById(String id);

    void removeById(String id);
}
