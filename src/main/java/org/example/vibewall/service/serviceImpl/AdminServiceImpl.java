package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.*;
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
import org.example.vibewall.service.AiService;
import org.example.vibewall.service.RefreshTokenService;
import org.example.vibewall.service.SystemLogService;
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
    private final SystemLogService systemLogService;
    private final AiService aiService;
    private final org.example.vibewall.service.ReputationService reputationService;


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
        if (savedAdmin == null) {
            savedAdmin = admin;
        }

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

    @Override
    public List<SystemLogResponse> getLogs() {
        return systemLogService.getRecentLogs();
    }

    @Override
    public void clearLogs() {
        systemLogService.clearLogs();
    }

    @Override
    public void recordClientError(ClientErrorRequest req, String userId) {
        systemLogService.recordLog(
                "ERROR",
                "CLIENT",
                req.getEndpoint(),
                req.getStatusCode() != null ? req.getStatusCode() : 500,
                userId,
                req.getMessage(),
                req.getDetails()
        );
    }

    @Override
    public AiStatusResponse getAiStatus() {
        return aiService.getAiStatus();
    }

    @Override
    public AiStatusResponse testAiConnection() {
        return aiService.testAiConnection();
    }

    @Override
    public void unbanUser(String userId, int restorePoints) {
        reputationService.unbanUser(userId, restorePoints);
    }

    @Override
    public void banUser(String userId, String reason) {
        reputationService.banUser(userId, reason);
    }

    @Override
    public void adjustUserPoints(String userId, int points) {
        userRepo.findById(userId).ifPresent(u -> {
            u.setPoints(points);
            if (points >= 0 && u.isBanned()) {
                u.setBanned(false);
                u.setBanReason(null);
            }
            userRepo.save(u);
        });
    }
}
