package org.example.vibewall.service;

import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;

import java.util.List;

public interface AdminService {
    List<UsersResponse> getAll();

    UsersResponse addAdmin(UsersRequested users);

    UsersResponse update(String id, UsersRequested user) throws AdminNotFoundException;

    void delete(String id) throws  AdminNotFoundException;

    List<ReportResponse> getReport();

    ReportResponse getReportById(String id) throws ReportNotFoundException;

    List<ReportResponse> getPending();

    ReportResponse getPendingById(String id);

    ReportResponse reslove(String id, String status) throws ReportNotFoundException ;
}
