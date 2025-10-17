package org.example.vibewall.service;

import org.example.vibewall.DAO.ConfessionRepo;
import org.example.vibewall.DAO.FeedbackRepo;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Feedback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final ConfessionRepo confessionRepo;
    private final FeedbackRepo feedbackRepo;

    public FeedbackService(ConfessionRepo confessionRepo, FeedbackRepo feedbackRepo) {
        this.confessionRepo = confessionRepo;
        this.feedbackRepo = feedbackRepo;
    }

    public void giveFeedback(String id, Feedback feedback) {
        Optional<Confession> confession=confessionRepo.findById(id);
        if(confession.isPresent()){
            Confession con=confession.get();
            con.getFeedbacks().add(feedback);
            confessionRepo.save(con);
        }else{
            System.out.println("Error");
        }

    }

    public List<Feedback> get(String id) {
        Optional<Confession> confessionOptional= confessionRepo.findById(id);
        if(confessionOptional.isPresent()){
            Confession confession=confessionOptional.get();

            return new ArrayList<>(confession.getFeedbacks());
        }else{
            System.out.println("Error");
            return new ArrayList<>();
        }
    }

    public String update(String id,String contest) {
        Optional<Feedback> feedbackOptional=feedbackRepo.findById(id);
        if(feedbackOptional.isPresent()){
            Feedback feedback=feedbackOptional.get();
            feedback.setFeedback(contest);
            feedbackRepo.save(feedback);
            return "Update";
        }
        return null;
    }

    public Feedback getById(String id) {
        Optional<Feedback> feedback=feedbackRepo.findById(id);
        if(feedback.isPresent()){
            return  feedback.get();
        }
        return new Feedback();
    }

    public void delete(String id) {
        Optional<Feedback> feedback=feedbackRepo.findById(id);
        if(feedback.isPresent()){
            feedbackRepo.removeById(id);
        }
    }
}
