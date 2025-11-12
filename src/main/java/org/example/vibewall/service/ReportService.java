package org.example.vibewall.service;

import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;

public interface ReportService {
    public String create(Report report);
    String remove(String id) throws ReportNotFoundException;
    String update(String id, Report report) throws ReportNotFoundException;
}
