package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformStatsResponse {
    private long activeConfessions;
    private long totalFeedbacks;
    private String communityVibe;
    private String safetyScore;
    private long timestamp;
}
