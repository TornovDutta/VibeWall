package org.example.vibewall.service;

import org.example.vibewall.DTO.SystemLogResponse;

import java.util.List;

public interface SystemLogService {

    void recordLog(String level, String source, String endpoint, Integer statusCode, String userId, String message, String details);

    List<SystemLogResponse> getRecentLogs();

    void clearLogs();

    long getErrorCount();
}
