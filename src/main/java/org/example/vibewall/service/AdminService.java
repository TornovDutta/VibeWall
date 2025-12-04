package org.example.vibewall.service;

import org.example.vibewall.DTO.ReportDTO;
import org.example.vibewall.DTO.UsersDTO;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;

import java.util.List;

public interface AdminService {
    List<UsersDTO> getAll();
    UsersDTO addAdmin(Users user);
    UsersDTO update(String id, Users user) throws AdminNotFoundException;
    void delete(String id) throws AdminNotFoundException;
    List<ReportDTO> getReport();
    ReportDTO getReportById(String id) throws ReportNotFoundException;
    List<ReportDTO> getPending();

    ReportDTO getPendingById(String id);

    ReportDTO reslove(String id, String status) throws ReportNotFoundException;


}

