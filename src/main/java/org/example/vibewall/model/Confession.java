package org.example.vibewall.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "confessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Confession {
    @Id
    private String id;
    @NotBlank
    private String content;
    private String userId;
    @CreatedDate
    private Date time;

    private List<Feedback> feedbacks=new ArrayList<>();

    public Confession(String content){
        this.content=content;

        this.time=new Date();
    }

}
