package org.example.vibewall.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Confession {
    @Id
    private String id;
    @NotBlank
    private String content;

    private LocalDateTime create;
    private LocalDateTime update;

    public Confession(String content) {
        this.content = content;
        if(this.create==null){
            this.create = LocalDateTime.now();
        }
        this.update = LocalDateTime.now();
    }


}
