package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLogResponse {
    private String id;
    private String level;
    private String source;
    private String endpoint;
    private Integer statusCode;
    private String userId;
    private String userAlias;
    private String message;
    private String details;
    private Date timestamp;
}
