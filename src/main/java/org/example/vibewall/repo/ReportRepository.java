package org.example.vibewall.repo;

import org.example.vibewall.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReportRepository extends MongoRepository<Report,String> {
    String removeById(String id);
    List<Report> findByStatus(String status);
    Report findByStatusAndId(String status, String id);
    List<Report> findByUserId(String userId);
}
