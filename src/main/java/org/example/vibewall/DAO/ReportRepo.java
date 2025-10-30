package org.example.vibewall.DAO;

import org.example.vibewall.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReportRepo extends MongoRepository<Report,String> {
    String removeById(String id);
    List<Report> findByStatus(String status);
    List<Report> findByStatusAndId(String status, String id);
}

