package org.example.vibewall.service;

import org.example.vibewall.DTO.*;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;

import java.util.List;

public interface AdminService {
    List<UserResponse> getAll();

    TokenResponse addAdmin(UserRequest users);

    UserResponse update(String id, UserRequest user) throws AdminNotFoundException;

    void delete(String id) throws AdminNotFoundException;

    List<ReportResponse> getReport();

    ReportResponse getReportById(String id) throws ReportNotFoundException;

    List<ReportResponse> getPending();

    ReportResponse getPendingById(String id);

    ReportResponse reslove(String id, String status) throws ReportNotFoundException;

    List<SystemLogResponse> getLogs();

    void clearLogs();

    void recordClientError(ClientErrorRequest req, String userId);

    AiStatusResponse getAiStatus();

    AiStatusResponse testAiConnection();

    void unbanUser(String userId, int restorePoints);

    void banUser(String userId, String reason);

    void adjustUserPoints(String userId, int points);
}
