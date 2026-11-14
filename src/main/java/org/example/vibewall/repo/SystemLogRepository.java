package org.example.vibewall.repo;

import org.example.vibewall.model.SystemLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemLogRepository extends MongoRepository<SystemLog, String> {

    List<SystemLog> findTop100ByOrderByTimestampDesc();

    List<SystemLog> findAllByOrderByTimestampDesc();

    long countByLevel(String level);
}
