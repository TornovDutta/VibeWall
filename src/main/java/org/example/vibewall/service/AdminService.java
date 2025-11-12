package org.example.vibewall.service;

import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;

import java.util.List;

public interface AdminService {
    List<Users> getAll();
    Users addAdmin(Users user);
    Users update(String id, Users user) throws AdminNotFoundException;
    void delete(String id) throws AdminNotFoundException;
    List<Report> getReport();
    Report getReportById(String id) throws ReportNotFoundException;
    List<Report> getPending();

    Report getPendingById(String id);

    Report reslove(String id, String status) throws ReportNotFoundException;


}

