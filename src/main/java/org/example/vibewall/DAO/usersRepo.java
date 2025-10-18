package org.example.vibewall.DAO;

import org.example.vibewall.model.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepo extends MongoRepository<Users,String> {
    void removeById(String id);
}
