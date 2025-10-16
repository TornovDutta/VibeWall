package org.example.vibewall.service;

import org.example.vibewall.DAO.FeedbackRepo;
import org.example.vibewall.model.Feedback;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {
    private final FeedbackRepo feedbackRepo;

    public FeedbackService(FeedbackRepo feedbackRepo) {
        this.feedbackRepo = feedbackRepo;
    }

    public void giveFeedback(String id,Feedback feedback) {
        feedbackRepo.save(feedback);

    }
}
