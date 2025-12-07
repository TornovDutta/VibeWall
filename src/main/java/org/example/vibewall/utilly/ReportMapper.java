package org.example.vibewall.utilly;

import org.example.vibewall.DTO.ReportDTO;
import org.example.vibewall.model.Report;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportMapper {
    public ReportDTO toDto(Report report){
        return new ReportDTO(
                report.getId(),
                report.getReportContent(),
                report.getStatus()
        );

    }
    public List<ReportDTO> toDtoList(List<Report> reports) {
        return reports.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    public Report toEntity(ReportDTO reportDTO){
        return new Report(
                reportDTO.id(),
                reportDTO.reportContent(),
                reportDTO.status()
        );
    }
    public List<Report> toEntityList(List<ReportDTO> reportDTOS){
        return reportDTOS.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
