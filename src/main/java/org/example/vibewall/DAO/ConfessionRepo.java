package org.example.vibewall.DAO;

import org.example.vibewall.model.Confession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfessionRepo extends MongoRepository<Confession,Integer> {
    void removeById(Integer id);
}
