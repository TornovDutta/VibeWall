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
public class EnhanceResponse {

    private String originalText;
    private String polishedText;
    private List<String> suggestions;
    private List<String> suggestedOptions; // populated for POLL type
}
