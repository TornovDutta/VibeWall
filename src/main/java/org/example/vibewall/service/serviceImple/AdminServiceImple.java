package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UsersDTO;
import org.example.vibewall.repo.ReportRepo;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.AdminService;
import org.example.vibewall.utilly.UsersMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminServiceImple implements AdminService {
    private final UsersRepo userRepo;
    private final ReportRepo reportRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Encryption encryption;
    private final UsersMapper usersMapper;
    private static final Logger logger= LoggerFactory.getLogger(AdminServiceImple.class);


    @Override
    public List<UsersDTO> getAll() {
        List<Users> users=userRepo.findAll();
        return usersMapper.toDtoList(users);

    }


    @Override
    public Users addAdmin(Users user) {
        user.setUsername(encryption.encode(user.getUsername()));
        user.setRole("ADMIN");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("new admin add");
        return userRepo.save(user);
    }

    @Override
    public Users update(String id, Users user) throws AdminNotFoundException{
        Users existingUser = userRepo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        existingUser.setUsername(encryption.encode(user.getUsername()));
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("admin of id: "+id+" update");
        return userRepo.save(existingUser);
    }

    @Override
    public void delete(String id) throws AdminNotFoundException{
        userRepo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        logger.info("admin of id: "+id+" remove");
        userRepo.removeById(id);
    }

    @Override
    public List<Report> getReport() {
        return reportRepo.findAll();
    }

    @Override

    public Report getReportById(String id) throws ReportNotFoundException{
        return reportRepo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
    }

    @Override
    public List<Report> getPending() {
        return reportRepo.findByStatus("PENDING");
    }
    @Override
    public Report getPendingById(String id) {
        return reportRepo.findByStatusAndId("PENDING",id);
    }


    @Override
    public Report reslove(String id, String status) throws ReportNotFoundException {
        Report report=reportRepo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
        logger.info("id: "+id +" , reslove by admin");
        report.setStatus(status);
        return reportRepo.save(report);
    }
}
