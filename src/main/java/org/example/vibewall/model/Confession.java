package org.example.vibewall.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
@Data
public class Confession {
    @Id
    private String id;
    private String content;
    @CreatedDate
    private Date time;

    public Confession(){
        this.time=new Date();
    }
    public Confession(String content){
        this.content=content;
        this.time=new Date();
    }
}
