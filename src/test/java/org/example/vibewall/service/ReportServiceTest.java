package org.example.vibewall.service;

import org.example.vibewall.DTO.ReportRequested;
import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.repo.ReportRepo;
import org.example.vibewall.service.serviceImple.ReportServiceImplements;
import org.example.vibewall.utility.ReportMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepo reportRepo;

    @Mock
    private ReportMapper mapper;

    @InjectMocks
    private ReportServiceImplements service;

    private static final String USER_ID = "user-1";

    @Test
    void create_shouldSaveAndReturnReportResponse() {
        ReportRequested request = new ReportRequested("report content");
        Report savedReport = new Report(USER_ID, "report content");
        ReportResponse response = new ReportResponse("1", "report content", "PENDING", null);

        when(reportRepo.save(any(Report.class))).thenReturn(savedReport);
        when(mapper.toDTO(savedReport)).thenReturn(response);

        ReportResponse result = service.create(USER_ID, request);

        assertNotNull(result);
        assertEquals("report content", result.getReportContent());
        verify(reportRepo).save(any(Report.class));
        verify(mapper).toDTO(savedReport);
    }

    @Test
    void getByUser_shouldReturnUserReports() {
        Report r = new Report(USER_ID, "content");
        ReportResponse dto = new ReportResponse("1", "content", "PENDING");

        when(reportRepo.findByUserId(USER_ID)).thenReturn(List.of(r));
        when(mapper.toDTO(List.of(r))).thenReturn(List.of(dto));

        List<ReportResponse> result = service.getByUser(USER_ID);

        assertEquals(1, result.size());
        verify(reportRepo).findByUserId(USER_ID);
    }

    @Test
    void update_shouldUpdateAndReturnResponse_whenReportExists() throws ReportNotFoundException {
        String reportId = "123";
        ReportRequested request = new ReportRequested("updated content");
        Report existingReport = new Report(USER_ID, "old content");
        existingReport.setId(reportId);
        ReportResponse response = new ReportResponse(reportId, "updated content", "PENDING", null);

        when(reportRepo.findById(reportId)).thenReturn(Optional.of(existingReport));
        when(reportRepo.save(existingReport)).thenReturn(existingReport);
        when(mapper.toDTO(existingReport)).thenReturn(response);

        ReportResponse result = service.update(USER_ID, reportId, request);

        assertEquals("updated content", result.getReportContent());
        verify(reportRepo).findById(reportId);
        verify(reportRepo).save(existingReport);
    }

    @Test
    void update_shouldThrowAccessDenied_whenNotOwner() {
        String reportId = "123";
        Report existingReport = new Report("other-user", "content");
        existingReport.setId(reportId);

        when(reportRepo.findById(reportId)).thenReturn(Optional.of(existingReport));

        assertThrows(
                AccessDeniedException.class,
                () -> service.update(USER_ID, reportId, new ReportRequested("content"))
        );
    }

    @Test
    void update_shouldThrowException_whenReportNotFound() {
        when(reportRepo.findById("404")).thenReturn(Optional.empty());

        assertThrows(
                ReportNotFoundException.class,
                () -> service.update(USER_ID, "404", new ReportRequested("content"))
        );

        verifyNoInteractions(mapper);
    }

    @Test
    void delete_shouldDelete_whenOwnerAndReportExists() throws ReportNotFoundException {
        Report report = new Report(USER_ID, "content");
        report.setId("1");

        when(reportRepo.findById("1")).thenReturn(Optional.of(report));

        service.delete(USER_ID, "1");

        verify(reportRepo).deleteById("1");
    }

    @Test
    void delete_shouldThrowAccessDenied_whenNotOwner() {
        Report report = new Report("other-user", "content");
        report.setId("1");

        when(reportRepo.findById("1")).thenReturn(Optional.of(report));

        assertThrows(
                AccessDeniedException.class,
                () -> service.delete(USER_ID, "1")
        );

        verify(reportRepo, never()).deleteById(anyString());
    }

    @Test
    void delete_shouldThrowException_whenReportNotFound() {
        when(reportRepo.findById("404")).thenReturn(Optional.empty());

        assertThrows(
                ReportNotFoundException.class,
                () -> service.delete(USER_ID, "404")
        );

        verify(reportRepo, never()).deleteById(anyString());
    }
}
