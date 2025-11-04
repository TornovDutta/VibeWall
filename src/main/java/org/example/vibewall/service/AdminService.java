package org.example.vibewall.service;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DAO.ReportRepo;
import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.encryption.Encryption;
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
@RequiredArgsConstructor
public class AdminService {
    private final UsersRepo userRepo;
    private final ReportRepo reportRepo;
    private final PasswordEncoder passwordEncoder;
    private final Encryption encryption;
    private static final Logger logger= LoggerFactory.getLogger(AdminService.class);



    public List<Users> getAll() {
        return userRepo.findAll();
    }

    public Users addAdmin(Users user) {
        user.setUsername(encryption.encode(user.getUsername()));
        user.setRole("ADMIN");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("new admin add");
        return userRepo.save(user);
    }

    public Users update(String id, Users user) throws AdminNotFoundException{
        Users existingUser = userRepo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        existingUser.setUsername(encryption.encode(user.getUsername()));
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("admin of id: "+id+" update");
        return userRepo.save(existingUser);
    }

    public void delete(String id) throws AdminNotFoundException{
        userRepo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        logger.info("admin of id: "+id+" remove");
        userRepo.removeById(id);
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

    public Report getPendingById(String id) {
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
