package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportDTO;
import org.example.vibewall.DTO.UsersDTO;
import org.example.vibewall.repo.ReportRepo;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.AdminService;
import org.example.vibewall.utilly.ReportMapper;
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
    private final ReportMapper reportMapper;
    private static final Logger logger= LoggerFactory.getLogger(AdminServiceImple.class);


    @Override
    public List<UsersDTO> getAll() {
        List<Users> users=userRepo.findAll();
        return usersMapper.toDtoList(users);

    }


    @Override
    public UsersDTO addAdmin(Users user) {
        user.setUsername(encryption.encode(user.getUsername()));
        user.setRole("ADMIN");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("new admin add");
        Users users=userRepo.save(user);
        return usersMapper.toDto(users);
    }

    @Override
    public UsersDTO update(String id, Users user) throws AdminNotFoundException{
        Users existingUser = userRepo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        existingUser.setUsername(encryption.encode(user.getUsername()));
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("admin of id: "+id+" update");
        Users realUsers=userRepo.save(existingUser);
        return usersMapper.toDto(realUsers);
    }

    @Override
    public void delete(String id) throws AdminNotFoundException{
        userRepo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        logger.info("admin of id: "+id+" remove");
        userRepo.removeById(id);
    }

    @Override
    public List<ReportDTO> getReport() {
        List<Report> reports=reportRepo.findAll();
        return reportMapper.toDtoList(reports);
    }

    @Override

    public ReportDTO getReportById(String id) throws ReportNotFoundException{
        Report report=reportRepo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
        return reportMapper.toDto(report);
    }

    @Override
    public List<ReportDTO> getPending() {
        List<Report> reports=reportRepo.findByStatus("PENDING");
        return reportMapper.toDtoList(reports);
    }
    @Override
    public ReportDTO getPendingById(String id) {
        Report report=reportRepo.findByStatusAndId("PENDING",id);
        return reportMapper.toDto(report);
    }


    @Override
    public ReportDTO reslove(String id, String status) throws ReportNotFoundException {
        Report report=reportRepo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
        logger.info("id: "+id +" , reslove by admin");
        report.setStatus(status);
        Report report1=reportRepo.save(report);
        return reportMapper.toDto(report1);
    }
}
