package org.example.vibewall.service;

import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.FeedbackNotFoundException;
import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.model.Feedback;

import java.util.List;

public interface FeedbackService {
    String giveFeedback(String id, Feedback feedback)
            throws PrincipalNotFollowException, ConfessionNotFoundException;

    List<Feedback> get(String id) throws ConfessionNotFoundException;

    String update(String id, Feedback feedback) throws PrincipalNotFollowException, FeedbackNotFoundException;

    Feedback getById(String id) throws FeedbackNotFoundException;
    String delete(String id) throws FeedbackNotFoundException;


}
