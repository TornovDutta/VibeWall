package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientErrorRequest {
    private String endpoint;
    private Integer statusCode;
    private String message;
    private String details;
}
