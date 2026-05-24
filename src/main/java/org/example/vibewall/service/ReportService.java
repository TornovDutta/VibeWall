package org.example.vibewall.service;

import org.example.vibewall.DTO.ReportRequest;
import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.exception.ReportNotFoundException;

import java.util.List;

public interface ReportService {
    ReportResponse create(String userId, ReportRequest requested);
    List<ReportResponse> getByUser(String userId);
    ReportResponse update(String userId, String reportId, ReportRequest requested) throws ReportNotFoundException;
    void delete(String userId, String reportId) throws ReportNotFoundException;
}
