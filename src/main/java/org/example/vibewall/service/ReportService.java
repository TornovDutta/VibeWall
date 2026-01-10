package org.example.vibewall.service;

import org.example.vibewall.DTO.ReportRequested;
import org.example.vibewall.DTO.ReportResponse;

public interface ReportService {
    ReportResponse create(ReportRequested requested, String confessionId);
    ReportResponse update(String reportId, ReportRequested requested);

    void delete(String reportId);
}
