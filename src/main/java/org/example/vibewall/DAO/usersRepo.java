package org.example.vibewall.DAO;

import org.example.vibewall.model.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface UsersRepo extends MongoRepository<Users,String> {
    void removeById(String id);

    Optional<Users> findByUsername(String username);

    @Query(value = "{ 'username': ?0 }", fields = "{ '_id': 1 }")
    String findUserIdByUsername(String username);

}
