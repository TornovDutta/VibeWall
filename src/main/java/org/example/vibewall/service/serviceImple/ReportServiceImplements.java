package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportRequested;
import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.repo.ReportRepo;
import org.example.vibewall.service.ReportService;
import org.example.vibewall.utility.ReportMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImplements implements ReportService {
    private  final ReportRepo reportRepo;
    private final ReportMapper mapper;

    @Override
    public ReportResponse create(ReportRequested requested) {
        Report report = new Report(requested.getContent());
        Report saveReport=reportRepo.save(report);

        return mapper.toDTO(saveReport);
    }
    @Override
    public ReportResponse update(String reportId, ReportRequested requested) throws ReportNotFoundException {

        Report report = reportRepo.findById(reportId)
                .orElseThrow(() ->
                        new ReportNotFoundException("Report not found with id: " + reportId)
                );

        report.setReportContent(requested.getContent());

        Report updatedReport = reportRepo.save(report);

        return mapper.toDTO(updatedReport);
    }

    @Override
    public void delete(String reportId) throws ReportNotFoundException {

        if (!reportRepo.existsById(reportId)) {
            throw new ReportNotFoundException("Report not found with id: " + reportId);
        }

        reportRepo.deleteById(reportId);
    }
}
