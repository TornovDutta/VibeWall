package org.example.vibewall.service;

import org.example.vibewall.DTO.ReportRequested;
import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.exception.ReportNotFoundException;

public interface ReportService {
    ReportResponse create(ReportRequested requested);
    ReportResponse update(String reportId, ReportRequested requested) throws ReportNotFoundException;

    void delete(String reportId) throws ReportNotFoundException;
}
