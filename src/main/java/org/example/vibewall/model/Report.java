package org.example.vibewall.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    private String id;
    private String reportContent;
    private LocalDateTime dateTime;
    private String status="PENDING";


    public Report( String reportContent) {

        this.reportContent = reportContent;
        this.dateTime = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Report(String id, String reportContent,String status ) {
        this.id = id;
        this.reportContent = reportContent;
        this.status=status;

    }
}
