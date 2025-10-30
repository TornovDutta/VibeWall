package org.example.vibewall.DAO;

import org.example.vibewall.model.RequestUpdate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestUpdateRepo extends MongoRepository<RequestUpdate,String> {
}
