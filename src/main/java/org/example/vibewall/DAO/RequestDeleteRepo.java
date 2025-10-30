package org.example.vibewall.DAO;

import org.example.vibewall.model.RequestDelete;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestDeleteRepo extends MongoRepository<RequestDelete,String> {
}
