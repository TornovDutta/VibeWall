package org.example.vibewall.service;

import org.example.vibewall.repo.ReportRepo;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportService {
    private final ReportRepo repo;
    private final static Logger logger = LoggerFactory.getLogger(ReportService.class);
    public ReportService(ReportRepo repo) {
        this.repo = repo;
    }

    public String create(Report report) {
        Report r=new Report(report.getReportContent());
        logger.info(report +" coming at "+ LocalDateTime.now());
        return repo.save(r).getId();
    }

    public String remove(String id) throws ReportNotFoundException{
        repo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
        return repo.removeById(id);
    }

    public String update(String id, Report report) throws ReportNotFoundException {
        Report r=repo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
        repo.removeById(id);
        Report newReport=new Report(report.getReportContent());
        return repo.save(newReport).getId();
    }
}
