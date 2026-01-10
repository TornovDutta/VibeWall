package org.example.vibewall.service;

import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.config.JwtUtil;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;
import org.example.vibewall.repo.ReportRepo;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.service.serviceImple.AdminServiceImplements;
import org.example.vibewall.utility.ReportMapper;
import org.example.vibewall.utility.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock
    private UsersRepo userRepo;

    @Mock
    private ReportRepo reportRepo;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private Encryption encryption;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AdminServiceImplements adminService;

    private Users adminUser;
    private Report report;

    @BeforeEach
    void setUp() {
        adminUser = new Users("1", "admin", "pass", "ADMIN");
        report = new Report();
        report.setId("r1");
        report.setStatus("PENDING");
    }

    // USERS

    @Test
    void getAll_shouldReturnAllUsers() {
        when(userRepo.findAll()).thenReturn(List.of(adminUser));
        when(userMapper.toDTO(anyList()))
                .thenReturn(List.of(new UsersResponse("1", "admin")));

        List<UsersResponse> result = adminService.getAll();

        assertEquals(1, result.size());
        verify(userRepo).findAll();
        verify(userMapper).toDTO(anyList());
    }

    @Test
    void addAdmin_shouldCreateAdminSuccessfully() {
        UsersRequested request = new UsersRequested("admin", "password");

        when(encryption.encode("admin")).thenReturn("encryptedAdmin");
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("jwt-token");
        when(userMapper.toDTO(any(Users.class)))
                .thenReturn(new UsersResponse("1", "encryptedAdmin"));

        UsersResponse response = adminService.addAdmin(request);

        assertNotNull(response);
        verify(userRepo).save(any(Users.class));
        verify(jwtUtil).generateToken(any(), any(), any());
    }

    @Test
    void update_shouldUpdateAdminSuccessfully() throws AdminNotFoundException {
        UsersRequested request = new UsersRequested("newAdmin", "newPass");

        when(userRepo.findById("1")).thenReturn(Optional.of(adminUser));
        when(encryption.encode("newAdmin")).thenReturn("encAdmin");
        when(passwordEncoder.encode("newPass")).thenReturn("encPass");
        when(userRepo.save(adminUser)).thenReturn(adminUser);
        when(userMapper.toDTO(adminUser))
                .thenReturn(new UsersResponse("1", "encAdmin"));

        UsersResponse response = adminService.update("1", request);

        assertEquals("1", response.id());
        verify(userRepo).save(adminUser);
    }

    @Test
    void update_shouldThrowException_whenAdminNotFound() {
        when(userRepo.findById("99")).thenReturn(Optional.empty());

        assertThrows(
                AdminNotFoundException.class,
                () -> adminService.update("99", new UsersRequested("a", "b"))
        );
    }

    @Test
    void delete_shouldDeleteAdminSuccessfully() throws AdminNotFoundException {
        when(userRepo.findById("1")).thenReturn(Optional.of(adminUser));

        adminService.delete("1");

        verify(userRepo).removeById("1");
    }

    @Test
    void delete_shouldThrowException_whenAdminNotFound() {
        when(userRepo.findById("2")).thenReturn(Optional.empty());

        assertThrows(
                AdminNotFoundException.class,
                () -> adminService.delete("2")
        );
    }

    //  REPORTS

    @Test
    void getReport_shouldReturnAllReports() {
        when(reportRepo.findAll()).thenReturn(List.of(report));
        when(reportMapper.toDTO(anyList()))
                .thenReturn(List.of(mock(ReportResponse.class)));

        List<ReportResponse> responses = adminService.getReport();

        assertEquals(1, responses.size());
    }

    @Test
    void getReportById_shouldReturnReport() throws ReportNotFoundException {
        when(reportRepo.findById("r1")).thenReturn(Optional.of(report));
        when(reportMapper.toDTO(report))
                .thenReturn(mock(ReportResponse.class));

        assertNotNull(adminService.getReportById("r1"));
    }

    @Test
    void getReportById_shouldThrowException_whenNotFound() {
        when(reportRepo.findById("r2")).thenReturn(Optional.empty());

        assertThrows(
                ReportNotFoundException.class,
                () -> adminService.getReportById("r2")
        );
    }

    @Test
    void getPending_shouldReturnPendingReports() {
        when(reportRepo.findByStatus("PENDING")).thenReturn(List.of(report));
        when(reportMapper.toDTO(anyList()))
                .thenReturn(List.of(mock(ReportResponse.class)));

        assertEquals(1, adminService.getPending().size());
    }

    @Test
    void reslove_shouldUpdateReportStatus() throws ReportNotFoundException {
        when(reportRepo.findById("r1")).thenReturn(Optional.of(report));
        when(reportRepo.save(report)).thenReturn(report);
        when(reportMapper.toDTO(report))
                .thenReturn(mock(ReportResponse.class));

        ReportResponse response = adminService.reslove("r1", "RESOLVED");

        assertNotNull(response);
        assertEquals("RESOLVED", report.getStatus());
    }

}