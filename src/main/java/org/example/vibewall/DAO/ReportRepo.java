package org.example.vibewall.DAO;

import org.example.vibewall.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepo extends MongoRepository<Report,String> {
    String removeById(String id);
}
