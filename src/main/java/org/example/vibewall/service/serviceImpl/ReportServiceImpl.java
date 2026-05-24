package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportRequest;
import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.repo.ReportRepository;
import org.example.vibewall.service.ReportService;
import org.example.vibewall.utility.ReportMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepo;
    private final ReportMapper mapper;

    @Override
    public ReportResponse create(String userId, ReportRequest requested) {
        Report report = new Report(userId, requested.getContent());
        Report savedReport = reportRepo.save(report);
        return mapper.toDTO(savedReport);
    }

    @Override
    public List<ReportResponse> getByUser(String userId) {
        return mapper.toDTO(reportRepo.findByUserId(userId));
    }

    @Override
    public ReportResponse update(String userId, String reportId, ReportRequest requested) throws ReportNotFoundException {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with id: " + reportId));

        if (!userId.equals(report.getUserId())) {
            throw new AccessDeniedException("You do not own this report");
        }

        report.setReportContent(requested.getContent());
        return mapper.toDTO(reportRepo.save(report));
    }

    @Override
    public void delete(String userId, String reportId) throws ReportNotFoundException {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with id: " + reportId));

        if (!userId.equals(report.getUserId())) {
            throw new AccessDeniedException("You do not own this report");
        }

        reportRepo.deleteById(reportId);
    }
}
