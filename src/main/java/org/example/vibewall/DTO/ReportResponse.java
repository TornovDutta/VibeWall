package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse implements Serializable {
    private String id;
    private String reportContent;
    private String status;

}