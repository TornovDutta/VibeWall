package org.example.vibewall.service;

import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.DTO.TokenResponse;
import org.example.vibewall.DTO.UserRequest;
import org.example.vibewall.DTO.UserResponse;
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
}
