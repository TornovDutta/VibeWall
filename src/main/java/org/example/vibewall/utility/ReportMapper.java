package org.example.vibewall.utility;

import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportMapper {
    public ReportResponse toDTO(Report report) {

        return new ReportResponse(report.getId(), report.getReportContent(), report.getStatus());
    }

    public List<ReportResponse> toDTO(List<Report> reports) {
        return reports.stream()
                .map(this::toDTO)
                .toList();
    }
}
