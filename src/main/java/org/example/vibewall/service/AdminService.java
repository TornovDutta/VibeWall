package org.example.vibewall.service;

import org.example.vibewall.DAO.ReportRepo;
import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AdminService {
    private final UsersRepo repo;
    private final ReportRepo reportRepo;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger= LoggerFactory.getLogger(AdminService.class);

    public AdminService(UsersRepo repo, PasswordEncoder passwordEncoder,ReportRepo reportRepo) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.reportRepo=reportRepo;
    }

    public List<Users> getAll() {
        return repo.findAll();
    }

    public Users addAdmin(Users user) {
        user.setUsername(passwordEncoder.encode(user.getUsername()));
        user.setRole("ADMIN");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("new admin add");
        return repo.save(user);
    }

    public Users update(String id, Users user) throws AdminNotFoundException{
        Users existingUser = repo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        existingUser.setUsername(passwordEncoder.encode(user.getUsername()));
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("admin of id: "+id+" update");
        return repo.save(existingUser);
    }

    public void delete(String id) throws AdminNotFoundException{
        repo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        logger.info("admin of id: "+id+" remove");
        repo.removeById(id);
    }

    public List<Report> getReport() {
        return reportRepo.findAll();
    }

    public Report getReportById(String id) throws ReportNotFoundException{
        return reportRepo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
    }

    public List<Report> getPending() {
        return reportRepo.findByStatus("PENDING");
    }

    public List<Report> getPendingById(String id) {
        return reportRepo.findByStatusAndId("PENDING",id);
    }

    public Report reslove(String id, String status) throws ReportNotFoundException {
        Report report=reportRepo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
        logger.info("id: "+id +" , reslove by admin");
        report.setStatus(status);
        return reportRepo.save(report);
    }
}
