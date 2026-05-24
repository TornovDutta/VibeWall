package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.DTO.TokenResponse;
import org.example.vibewall.DTO.UserRequest;
import org.example.vibewall.DTO.UserResponse;
import org.example.vibewall.model.RefreshToken;
import org.example.vibewall.security.JwtUtil;
import org.example.vibewall.repo.ReportRepository;
import org.example.vibewall.repo.UserRepository;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.AdminService;
import org.example.vibewall.service.RefreshTokenService;
import org.example.vibewall.utility.ReportMapper;
import org.example.vibewall.utility.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepo;
    private final ReportRepository reportRepo;
    private final PasswordEncoder passwordEncoder;
    private final Encryption encryption;
    private final UserMapper mapper;
    private final ReportMapper reportMapper;
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;


    @Override
    public List<UserResponse> getAll() {
        List<Users> users = userRepo.findAll();
        return mapper.toDTO(users);
    }

    @Override
    public TokenResponse addAdmin(UserRequest request) {

        String encodedUsername = encryption.encode(request.getName());

        if (userRepo.existsByUsername(encodedUsername)) {
            throw new RuntimeException("Admin already exists");
        }

        Users admin = new Users();
        admin.setUsername(encodedUsername);
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole("ADMIN");

        Users savedAdmin = userRepo.save(admin);

        String accessToken = jwtUtil.generateToken(
                savedAdmin.getId(),
                savedAdmin.getUsername(),
                savedAdmin.getRole(),
                request.getName()
        );

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(savedAdmin.getId());

        return TokenResponse.builder()
                .jwt(accessToken)
                .refresh(refreshToken.getToken())
                .build();
    }


    @Override
    public UserResponse update(String id, UserRequest user) throws AdminNotFoundException {
        Users existingUser = userRepo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        existingUser.setUsername(encryption.encode(user.getName()));
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));

        Users saveUser = userRepo.save(existingUser);

        return mapper.toDTO(saveUser);
    }


    @Override
    public void delete(String id) throws AdminNotFoundException {
        userRepo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        userRepo.removeById(id);
    }

    @Override
    public List<ReportResponse> getReport() {
        List<Report> reports = reportRepo.findAll();
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
        return reportMapper.toDTO(reportRepo.findByStatusAndId("PENDING", id));
    }

    @Override
    public ReportResponse reslove(String id, String status) throws ReportNotFoundException {
        Report report = reportRepo.findById(id).orElseThrow(()->
                new ReportNotFoundException());

        report.setStatus(status);
        return reportMapper.toDTO(reportRepo.save(report));
    }
}
