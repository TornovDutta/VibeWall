package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelplineResource {
    private String name;
    private String contact;
    private String availability;
    private String description;
    private String region;
    private String website;
}
