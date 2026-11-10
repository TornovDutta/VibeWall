package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VibeCheckResponse {
    private String vibe;
    private String emoji;
    private String sentiment;
    private String comfortingTip;
    private List<String> tags;
}
