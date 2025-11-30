package org.example.vibewall.service.serviceImple;

import org.example.vibewall.repo.ReportRepo;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportServiceImple implements ReportService {
    private final ReportRepo repo;
    private final static Logger logger = LoggerFactory.getLogger(ReportServiceImple.class);
    public ReportServiceImple(ReportRepo repo) {
        this.repo = repo;
    }

    @Override
    public String create(Report report) {
        Report r=new Report(report.getReportContent());
        logger.info(report +" coming at "+ LocalDateTime.now());
        return repo.save(r).getId();
    }

    @Override
    public String remove(String id) throws ReportNotFoundException{
        repo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
        return repo.removeById(id);
    }

    @Override
    public String update(String id, Report report) throws ReportNotFoundException {
        Report r=repo.findById(id).orElseThrow(()->
                new ReportNotFoundException());
        repo.removeById(id);
        Report newReport=new Report(report.getReportContent());
        return repo.save(newReport).getId();
    }
}
