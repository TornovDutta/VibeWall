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
public class AiStatusResponse {
    private String status; // HEALTHY, DEGRADED, OFFLINE, UNCONFIGURED
    private String provider; // Groq, OpenRouter, Hugging Face, etc.
    private String model;
    private String endpoint;
    private Long latencyMs;
    private Date lastChecked;
    private String lastError;
    private boolean activeFallback;
    private String message;
}
