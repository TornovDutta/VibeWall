package org.example.vibewall.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document
@Data
public class Confession {
    @Id
    private String id;
    @NotBlank
    private String content;
    @CreatedDate
    private Date time;
    private String userId;
    private List<Feedback> feedbacks=new ArrayList<>();

    public Confession(){
        this.time=new Date();
    }
    public Confession(String content){
        this.content=content;
        this.userId=null;
        this.time=new Date();
    }

}
