package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.config.JwtUtil;
import org.example.vibewall.repo.ReportRepo;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.AdminService;
import org.example.vibewall.utility.ReportMapper;
import org.example.vibewall.utility.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminServiceImplements implements AdminService {
    private final UsersRepo userRepo;
    private final ReportRepo reportRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Encryption encryption;
    private final UserMapper mapper;
    private final ReportMapper reportMapper;
    private static final Logger logger= LoggerFactory.getLogger(AdminServiceImplements.class);
    private final JwtUtil jwtUtil;


    @Override
    public List<UsersResponse> getAll() {
        List<Users> users=userRepo.findAll();
        return mapper.toDTO(users);
    }
    @Override
    public UsersResponse addAdmin(UsersRequested user) {


        Users saveUser=new Users();
        saveUser.setUsername(encryption.encode(user.getName()));
        saveUser.setPassword(passwordEncoder.encode(user.getPassword()));
        saveUser.setRole("ADMIN");

        userRepo.save(saveUser);

        logger.info("new admin add");

        String token = jwtUtil.generateToken(
                saveUser.getId(),
                saveUser.getUsername(),
                saveUser.getRole()
        );


        System.out.println("JWT TOKEN (DEV ONLY): " + token);

        return mapper.toDTO(saveUser);


    }

    @Override
    public UsersResponse update(String id, UsersRequested user) throws AdminNotFoundException {
        Users existingUser = userRepo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        existingUser.setUsername(encryption.encode(user.getName()));
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("admin of id: "+id+" update");
        Users saveUser=userRepo.save(existingUser);

        return mapper.toDTO(saveUser);
    }



    @Override
    public void delete(String id) throws AdminNotFoundException{
        userRepo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        logger.info("admin of id: "+id+" remove");
        userRepo.removeById(id);
    }

    @Override
    public List<ReportResponse> getReport() {
        List<Report> reports=reportRepo.findAll();
        return reportMapper.toDTO(reports);
    }

    @Override
    public ReportResponse getReportById(String id) throws ReportNotFoundException {
        return reportMapper.toDTO(reportRepo.findById(id).orElseThrow(()->
                new ReportNotFoundException()));
    }
    @Override
    public List<ReportResponse> getPending() {
        return reportMapper.toDTO(reportRepo.findByStatus("PENDING"));
    }
    @Override
    public ReportResponse getPendingById(String id) {
        return reportMapper.toDTO(reportRepo.findByStatusAndId("PENDING",id));
    }
    @Override
    public ReportResponse reslove(String id, String status) throws ReportNotFoundException {
        Report report=reportRepo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
        logger.info("id: "+id +" , reslove by admin");
        report.setStatus(status);
        return reportMapper.toDTO(reportRepo.save(report));
    }
}