package org.example.vibewall.service;

import org.example.vibewall.DTO.ReportRequested;
import org.example.vibewall.DTO.ReportResponse;
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

    @Test
    void create_shouldSaveAndReturnReportResponse() {
        // Arrange
        ReportRequested request = new ReportRequested("report content");
        Report savedReport = new Report("report content");

        ReportResponse response =
                new ReportResponse("1", "report content", "CREATED");

        when(reportRepo.save(any(Report.class))).thenReturn(savedReport);
        when(mapper.toDTO(savedReport)).thenReturn(response);

        // Act
        ReportResponse result = service.create(request);

        // Assert
        assertNotNull(result);
        assertEquals("report content", result.reportContent());
        assertEquals("CREATED", result.status());

        verify(reportRepo).save(any(Report.class));
        verify(mapper).toDTO(savedReport);
    }

    @Test
    void update_shouldUpdateAndReturnResponse_whenReportExists() throws ReportNotFoundException {
        // Arrange
        String reportId = "123";
        ReportRequested request = new ReportRequested("updated content");
        Report existingReport = new Report("old content");

        ReportResponse response =
                new ReportResponse(reportId, "updated content", "UPDATED");

        when(reportRepo.findById(reportId)).thenReturn(Optional.of(existingReport));
        when(reportRepo.save(existingReport)).thenReturn(existingReport);
        when(mapper.toDTO(existingReport)).thenReturn(response);

        // Act
        ReportResponse result = service.update(reportId, request);

        // Assert
        assertEquals("updated content", result.reportContent());
        assertEquals("UPDATED", result.status());

        verify(reportRepo).findById(reportId);
        verify(reportRepo).save(existingReport);
        verify(mapper).toDTO(existingReport);
    }

    @Test
    void update_shouldThrowException_whenReportNotFound() {
        when(reportRepo.findById("404")).thenReturn(Optional.empty());

        assertThrows(
                ReportNotFoundException.class,
                () -> service.update("404", new ReportRequested("content"))
        );

        verify(reportRepo).findById("404");
        verifyNoInteractions(mapper);
    }

    @Test
    void delete_shouldDelete_whenReportExists() throws ReportNotFoundException {
        when(reportRepo.existsById("1")).thenReturn(true);

        service.delete("1");

        verify(reportRepo).deleteById("1");
    }

    @Test
    void delete_shouldThrowException_whenReportNotFound() {
        when(reportRepo.existsById("404")).thenReturn(false);

        assertThrows(
                ReportNotFoundException.class,
                () -> service.delete("404")
        );

        verify(reportRepo).existsById("404");
        verify(reportRepo, never()).deleteById(anyString());
    }
}
